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

import org.bigbluebutton.deskshare.client.ExitCode;
import org.bigbluebutton.deskshare.client.ScreenShareInfo;
import org.bigbluebutton.deskshare.common.Dimension;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkSocketStreamSender implements Runnable {
    private NetworkSocket socket = null;

    private String room;
    private Dimension screenDim;
    private Dimension blockDim;
    private final NextBlockRetriever retriever;
    private volatile boolean processMessages = false;
    private final int id;
    private NetworkStreamListener listener;
    private final SequenceNumberGenerator seqNumGenerator;

    private ScreenShareInfo ssi;

    public NetworkSocketStreamSender(int id, NextBlockRetriever retriever, String room, Dimension screenDim,
				     Dimension blockDim, SequenceNumberGenerator seqNumGenerator) {
        this.id = id;
        this.retriever = retriever;
        this.room = room;
        this.screenDim = screenDim;
        this.blockDim = blockDim;
        this.seqNumGenerator = seqNumGenerator;

        ssi = ScreenShareInfo.getInstance();
    }
    
    public void addListener(NetworkStreamListener listener) {
        this.listener = listener;
    }
    
    private void notifyNetworkStreamListener(ExitCode reason) {
        if (listener != null) {
            listener.networkException(id,reason);
        }
    }
    
    public void connect(String host, int port) throws ConnectionException {
        socket = new NetworkSocket(host, port);
    }

    public void connect(NetworkSocket networkSocket) {
    	socket = networkSocket;
    }
    
    public void sendStartStreamMessage() throws ConnectionException {
        try {
            ByteArrayOutputStream dataToSend = new ByteArrayOutputStream();
            dataToSend.reset();
            BlockStreamProtocolEncoder.encodeStartStreamMessage(room, screenDim, blockDim, dataToSend, seqNumGenerator.getNext());
            sendHeader(BlockStreamProtocolEncoder.encodeHeaderAndLength(dataToSend));
            sendToStream(dataToSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCursor(Point mouseLoc, String room) throws IOException {
        ByteArrayOutputStream dataToSend = new ByteArrayOutputStream();
        dataToSend.reset();
        BlockStreamProtocolEncoder.encodeMouseLocation(mouseLoc, room, dataToSend, seqNumGenerator.getNext());
        sendHeader(BlockStreamProtocolEncoder.encodeHeaderAndLength(dataToSend));
        sendToStream(dataToSend);
    }
    
    private void sendHeader(byte[] header) throws IOException {
	int size = socket.write(header);
	ssi.incrBytesSent(size);
    }
    
    private void sendToStream(ByteArrayOutputStream dataToSend) throws IOException {
	int size = socket.write(dataToSend);
	ssi.incrBytesSent(size);
    }
                    
    public void disconnect() throws ConnectionException {
        System.out.println("Disconnecting socket stream");
        if (!processMessages) {
            return;
        }
        try {
            ByteArrayOutputStream dataToSend = new ByteArrayOutputStream();
            dataToSend.reset();
            BlockStreamProtocolEncoder.encodeEndStreamMessage(room, dataToSend, seqNumGenerator.getNext());
            sendHeader(BlockStreamProtocolEncoder.encodeHeaderAndLength(dataToSend));
            sendToStream(dataToSend);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            processMessages = false;
        }
    }

    private void processCursorMessage(CursorMessage message) throws IOException {
        sendCursor(message.getMouseLocation(), message.getRoom());
    }

    private void processBlockMessage(BlockMessage message) throws IOException {
        ByteArrayOutputStream dataToSend = new ByteArrayOutputStream();
        dataToSend.reset();
        BlockStreamProtocolEncoder.encodeRoomAndSequenceNumber(room, seqNumGenerator.getNext(), dataToSend);

        Integer[] changedBlocks = message.getBlocks();

        BlockStreamProtocolEncoder.numBlocksChanged(changedBlocks.length, dataToSend);

        for (Integer changedBlock : changedBlocks) {
            EncodedBlockData block = retriever.getBlockToSend(changedBlock);
            BlockVideoData bv = new BlockVideoData(room, block.getPosition(), block.getVideoData(), message.getForceKeyFrame());
            BlockStreamProtocolEncoder.encodeBlock(bv, dataToSend);
            retriever.blockSent(changedBlock);
        }

        ssi.incrBlocksSent(changedBlocks.length);

        sendHeader(BlockStreamProtocolEncoder.encodeHeaderAndLength(dataToSend));
        sendToStream(dataToSend);
    }

    public void run() {
        processMessages = true;

        while (processMessages) {
            Message message;
            try {
                message = retriever.getNextMessageToSend();
                ssi.incrMessagesSent(1);
                switch(message.getMessageType()) {
                    case BLOCK:
                        processBlockMessage((BlockMessage)message);
                        break;
                    case CURSOR:
                        processCursorMessage((CursorMessage)message);
                        break;
                    default:
                        // ignore
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                processMessages = false;
                notifyNetworkStreamListener(ExitCode.CONNECTION_TO_DESKSHARE_SERVER_DROPPED);
            }
        }
            
        try {
            socket.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
