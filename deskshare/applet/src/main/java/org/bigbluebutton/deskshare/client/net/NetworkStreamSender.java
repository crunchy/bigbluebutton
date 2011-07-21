/**
 *
 * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
 *
 * Copyright (c) 2010 BigBlueButton Inc. and by respective authors (see below).
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 *
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
 *
 **/
package org.bigbluebutton.deskshare.client.net;

import net.jcip.annotations.ThreadSafe;

import org.bigbluebutton.deskshare.client.ExitCode;
import org.bigbluebutton.deskshare.client.QueueListener;
import org.bigbluebutton.deskshare.client.ScreenShareInfo;
import org.bigbluebutton.deskshare.client.blocks.BlockManager;
import org.bigbluebutton.deskshare.common.Dimension;
import org.bigbluebutton.deskshare.client.logging.PerformanceStats;

import java.util.*;
import java.util.Arrays.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.atomic.AtomicLong;

@ThreadSafe
public class NetworkStreamSender implements NextBlockRetriever, NetworkStreamListener {

    public static final String NAME = "NETWORKSTREAMSENDER: ";

    private ExecutorService executor;
    private LinkedBlockingQueue<Message> blockDataQ;
    private final int numThreads;
    private final String host;
    private final int port;
    private final String room;
    private final boolean httpTunnel;
    private NetworkSocketStreamSender[] socketSenders;
    private NetworkHttpStreamSender[] httpSenders;
    private boolean tunneling = false;
    private boolean stopped = true;
    private int numRunningThreads = 0;
    private Dimension screenDim;
    private Dimension blockDim;
    private BlockManager blockManager;
    private NetworkConnectionListener listener;
    private QueueListener queueListener;
    private final SequenceNumberGenerator seqNumGenerator = new SequenceNumberGenerator();
    private PerformanceStats perfStats = PerformanceStats.getInstance();
    
    public NetworkStreamSender(BlockManager blockManager, String host, int port,
        String room, Dimension screenDim, Dimension blockDim, boolean httpTunnel) {
        blockDataQ = new LinkedBlockingQueue<Message>(ScreenShareInfo.MAX_QUEUED_MESSAGES);

        this.blockManager = blockManager;
        this.host = host;
        this.port = port;
        this.room = room;
        this.screenDim = screenDim;
        this.blockDim = blockDim;
        this.httpTunnel = httpTunnel;


        numThreads = ScreenShareInfo.NETWORK_SENDER_COUNT;
        System.out.println(NAME + "Starting up " + numThreads + " sender threads.");
        executor = Executors.newFixedThreadPool(numThreads);
    }

    public void addNetworkConnectionListener(NetworkConnectionListener listener) {
        this.listener = listener;
    }
    
    public void addQueueListener(QueueListener queueListener) {
        this.queueListener = queueListener;
    }

    private void notifyNetworkConnectionListener(ExitCode reason) {
        if (listener != null) {
            listener.networkConnectionException(reason);
        }
    }
    
    private void notifyQueueListener(int queueSize) {
        if (queueListener != null) {
            queueListener.onQueueBackedup(queueSize);
        }
    }

    public boolean connect() {
	int failedAttempts = 0;

	socketSenders = new NetworkSocketStreamSender[numThreads];

	try {
		NetworkSocket socket = new NetworkSocket(host, port);

	    for (int i = 0; i < numThreads; i++) {
		try {

		    createSender(i, socket);
		    numRunningThreads++;
		} catch (ConnectionException e) {
		    failedAttempts++;
		}
	    }

	} catch (ConnectionException e) {
	    e.printStackTrace();
	    failedAttempts = numThreads;
	}

        if ((failedAttempts == numThreads) && httpTunnel) {
            System.out.println(NAME + "Trying http tunneling");
            failedAttempts = 0;
            numRunningThreads = 0;
            if (tryHttpTunneling()) {
                tunneling = true;
                System.out.println(NAME + "Will use http tunneling");
                httpSenders = new NetworkHttpStreamSender[numThreads];
                for (int i = 0; i < numThreads; i++) {
                    try {
                        createHttpSender(i);
                        numRunningThreads++;
                    } catch (ConnectionException e) {
                        failedAttempts++;
                    }
                }
                return failedAttempts != numThreads;
            }
        } else {
            if (numRunningThreads != numThreads) {
                try {
                    stop();
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
                return false;
            } else {
                return true;
            }
        }
        System.out.println(NAME + "Http tunneling failed.");
        return false;
    }

    private void createSender(int i, NetworkSocket socket) throws ConnectionException {
        socketSenders[i] = new NetworkSocketStreamSender(i, this, room, screenDim, blockDim, seqNumGenerator);
        socketSenders[i].addListener(this);
        socketSenders[i].connect(socket);
    }

    private void createHttpSender(int i) throws ConnectionException {
        httpSenders[i] = new NetworkHttpStreamSender(i, this, room, screenDim, blockDim, seqNumGenerator);
        httpSenders[i].addListener(this);
        httpSenders[i].connect(host);
    }

    /**
     * this examines the block data queue for messages that may include the same blocks
     * and discards any dupe blocks
     * todo: refactor this nastiness
    */
    private synchronized void purgeBlockDataQ() {
        Message message, nextMessage;
        BlockMessage blockMessage, nextBlockMessage;
        Integer[] blocks, nextBlocks;
        boolean skipMessage = false;
        int currentMessage = 0;
        LinkedBlockingQueue<Message> cloneQ = new LinkedBlockingQueue<Message>(ScreenShareInfo.MAX_QUEUED_MESSAGES);
        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

        Iterator it = blockDataQ.iterator();
        while (it.hasNext()) {
            message = (Message)it.next();
            skipMessage = false;
            ++currentMessage;
            
            // we only care about block messages that are keyframes
            // and have another block message after them
            // to dedupe against
            if (!(   message.isBlockMessage() && 
                     ((BlockMessage)message).getForceKeyFrame()) &&
                     it.hasNext()) {
                continue;
            }

            blockMessage = (BlockMessage)message;
            blocks = blockMessage.getBlocks();
            
            // look at the next block message to see if it overlaps with this one
            nextMessage = null;
            while (it.hasNext()) {
                nextMessage = (Message)it.next();
                if (!nextMessage.isBlockMessage()) {
                    nextMessage = null;
                    continue;
                }
            } 

            // now compare the blocks between this msg and the next block message
            if (nextMessage != null) {
                nextBlockMessage = (BlockMessage)nextMessage;
                nextBlocks = nextBlockMessage.getBlocks();
                    
                // exactly the same? ditch the whole message
                if (nextBlockMessage.hasSameBlocksAs(blockMessage)) {
                    skipMessage = true;
                } else { 
                    // not exactly the same? See if we can prune dupe blocks
                    blockMessage.discardBlocksSharedWith(nextBlockMessage);
                    skipMessage = (nextBlockMessage.isEmpty());
                }
            }
            
            // if the current message must be retained, put it on the clone queue
            if (skipMessage) {
                continue;
            }
            try {
                cloneQ.put(message);
            } catch (InterruptedException e) {
                // System.out.println("Can't put message on clone queue");
            }
        }
        
        // we've looked at the whole queue, so time to swap in clone for real queue
        try {
            blockDataQ.clear();
            for (Message cloneMessage: cloneQ) {
                blockDataQ.put(cloneMessage);
            } 
        } catch(InterruptedException e) {
            // System.out.println("Can't dump clone queue into queue");
        }

    }
    
    public void send(Message message) {
        try {
            // force keyframe? ditch all the other messages on the queue 
            // since this message will overwrite everything
            if (message.isBlockMessage() && ((BlockMessage)message).getForceKeyFrame()) {
                blockDataQ.clear();
            }
            // stick message on queue
            blockDataQ.put(message);
            
            // if the queue is backed up, purge it of duplicate blocks
            if (getQueueSizeInBlocks() > ScreenShareInfo.MAX_QUEUE_SIZE_FOR_PAUSE) {
                notifyQueueListener(blockDataQ.size());
                if (ScreenShareInfo.getPurgeBackedUpQueue()) {
                    purgeBlockDataQ();
                }
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println(NAME + "Starting network sender.");
        if (tunneling) {
            for (int i = 0; i < numRunningThreads; i++) {
                httpSenders[i].sendStartStreamMessage();
                executor.execute(httpSenders[i]);
            }
        } else {
            for (int i = 0; i < numRunningThreads; i++) {
                try {
                    socketSenders[i].sendStartStreamMessage();
                    executor.execute(socketSenders[i]);
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
        }
        stopped = false;
    }

    public void stop() throws ConnectionException {
        stopped = true;
        System.out.println(NAME + "Stopping network sender");
        for (int i = 0; i < numRunningThreads; i++) {
            if (tunneling) {
                httpSenders[i].disconnect();
            } else {
                socketSenders[i].disconnect();
            }
        }
        executor.shutdownNow();
        httpSenders = null;
        socketSenders = null;
    }

    private boolean tryHttpTunneling() {
        NetworkHttpStreamSender httpSender = new NetworkHttpStreamSender(0, this, room, screenDim, blockDim, seqNumGenerator);
        try {
            httpSender.connect(host);
            return true;
        } catch (ConnectionException e) {
            System.out.println(NAME + "Problem connecting to " + host);
        }
        return false;
    }

    public void blockSent(int position) {
        blockManager.blockSent(position);
    }

    public EncodedBlockData getBlockToSend(int position) {
        return blockManager.getBlock(position).encode();
    }

    public Message getNextMessageToSend() throws InterruptedException {
        try {
            return blockDataQ.take();
        } catch (InterruptedException e) {
            if (!stopped) {
                e.printStackTrace();
            }
            throw e;
        }
    }

    @Override
    public void networkException(int id, ExitCode reason) {
        try {
            numRunningThreads--;
            if (tunneling) {
                // httpSenders[id].disconnect();
                System.out.println(NAME + "Failed to use http tunneling. Stopping.");
                stop();
                notifyNetworkConnectionListener(reason);
            } else {
                socketSenders[id].disconnect();
            }
            if (numRunningThreads < 1) {
                System.out.println(NAME + "No more sender threads. Stopping.");
                stop();
                notifyNetworkConnectionListener(reason);
            } else {
                System.out.println(NAME + "Sender thread stopped. " + numRunningThreads + " sender threads remaining.");
            }
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if (numRunningThreads < 1) {
                System.out.println(NAME + "No more sender threads. Stopping.");
                notifyNetworkConnectionListener(reason);
            } else {
                System.out.println(NAME + "Sender thread stopped. " + numRunningThreads + " sender threads remaining.");
            }
        }
    }
    
    // utility function, remove me
    private int getQueueSizeInBlocks() {
        int numBlocks = 0;
        for (Message message: blockDataQ) {
            if (message.isBlockMessage()) {
                numBlocks += ((BlockMessage)message).size();
            }
        }
        return numBlocks;
    }
}
