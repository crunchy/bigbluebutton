/**
 * ===License Header===
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
 * ===License Header===
 */
package org.bigbluebutton.deskshare.client;

import org.bigbluebutton.deskshare.client.PerformanceListener;
import org.bigbluebutton.deskshare.client.QueueListener;
import org.bigbluebutton.deskshare.client.blocks.BlockManager;
import org.bigbluebutton.deskshare.client.blocks.ChangedBlocksListener;
import org.bigbluebutton.deskshare.client.image_filters.ChangeTypeFilter;
import org.bigbluebutton.deskshare.client.image_filters.ScalingFilter;
import org.bigbluebutton.deskshare.client.image_filters.SimpleFilter;
import org.bigbluebutton.deskshare.client.logging.PerformanceStats;
import org.bigbluebutton.deskshare.client.net.ConnectionException;
import org.bigbluebutton.deskshare.client.net.NetworkConnectionListener;
import org.bigbluebutton.deskshare.client.net.NetworkStreamSender;
import org.bigbluebutton.deskshare.common.Dimension;

import java.awt.image.BufferedImage;

public class ScreenSharerRunner implements ScreenCaptureListener {
    private static final String LICENSE_HEADER = 
        "This program is free software: you can redistribute it and/or modify\n" +
        "it under the terms of the GNU Lesser General Public License as published by\n" +
        "the Free Software Foundation, either version 3 of the License, or\n" +
        "(at your option) any later version.\n\n" +
        "This program is distributed in the hope that it will be useful,\n" +
        "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
        "GNU General Public License for more details.\n\n" +
        "You should have received a copy of the GNU Lesser General Public License\n" +
        "along with this program.  If not, see <http://www.gnu.org/licenses/>.\n\n" +
        "Copyright 2010 BigBlueButton. All Rights Reserved.\n\n";
    public static final String NAME = "SCREENSHARERUNNER: ";

    private static final int DEFAULT_TYPE = BufferedImage.TYPE_USHORT_555_RGB;

    private ScreenCaptureTaker captureTaker;
    private BlockManager blockManager;
    boolean connected = false;
    private boolean started = false;
    private NetworkStreamSender sender;

    private static ScreenShareInfo ssi;
    private static PerformanceStats perfStats;

    private DeskshareSystemTray tray = new DeskshareSystemTray();
    private ClientListener listener;
    private MouseLocationTaker mouseLocTaker;

    public ScreenSharerRunner() {
        ssi = ScreenShareInfo.getInstance();
        
        ScreenCapture capture = new ScreenCapture(ssi.x, ssi.y, ssi.captureWidth, ssi.captureHeight);

        captureTaker = new ScreenCaptureTaker(capture);
        captureTaker.addListener(this);

        if (ssi.contentPane != null) {
            captureTaker.addListener(new ScreenPreview(ssi.contentPane));
        }
        
        SimpleFilter filter = new ScalingFilter(ssi.scaleWidth,
            ssi.scaleHeight, ssi.quality);
        captureTaker.addFilter(filter);

        if (!ssi.quality) {
            filter = new ChangeTypeFilter(ssi.getColorDepth());
            captureTaker.addFilter(filter);
        }

        if (ssi.trackMouse) {
            mouseLocTaker = new MouseLocationTaker(ssi.captureWidth, ssi.captureHeight, ssi.scaleWidth, ssi.scaleHeight, ssi.x, ssi.y);
        }

        // Use the scaleWidth and scaleHeight as the dimension we pass to the BlockManager.
        // If there is no scaling required, the scaleWidth and scaleHeight will be the same as
        // captureWidth and captureHeight (ritzalam 05/27/2010)
        Dimension screenDim = new Dimension(ssi.scaleWidth, ssi.scaleHeight);
        Dimension tileDim = ssi.blockSize;
        blockManager = new BlockManager();
        blockManager.initialize(screenDim, tileDim);

        sender = new NetworkStreamSender(blockManager, ssi.host, ssi.port, ssi.room, screenDim, tileDim, ssi.httpTunnel);
        
        sender.addQueueListener(new QueueListener(captureTaker));
        
        if (ssi.statsLogging) {
            sender.addPerformanceListener(new PerformanceListener(captureTaker));
        }
        
        // log stats
        perfStats = PerformanceStats.getInstance();
    }

    public void startSharing() {
        connected = sender.connect();
        if (connected) {
            
            // set listeners
            ChangedBlocksListener changedBlocksListener = new ChangedBlockListenerImp(sender);
            blockManager.addListener(changedBlocksListener);

            ScreenCaptureListener screenCapListener = new ScreenCaptureListenerImp(blockManager);
            captureTaker.addListener(screenCapListener);
            
            captureTaker.start();

            sender.start();

	    if(mouseLocTaker != null) {
            	MouseLocationListenerImp mouseLocListener = new MouseLocationListenerImp(sender, ssi.room);
            	mouseLocTaker.addListener(mouseLocListener);
            	mouseLocTaker.start();
	    }

	    if(ScreenShareInfo.statsLogging) {
                perfStats.start();
	    }

            started = true;

        } else {
            notifyListener(ExitCode.DESKSHARE_SERVICE_UNAVAILABLE);
        }
    }

    public void stopSharing() {
        System.out.println(NAME + "Stopping");
        // System.out.println(NAME + "Removing icon from system tray.");
        tray.removeIconFromSystemTray();
        captureTaker.stop();
        mouseLocTaker.stop();
        perfStats.stop();
        // stop logging
        if (connected && started) {
            try {
                sender.stop();
                started = false;
                connected = false;
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCaptureCoordinates(int x, int y) {
        captureTaker.setCaptureCoordinates(x, y);
        mouseLocTaker.setCaptureCoordinates(x, y);
    }

    private void notifyListener(ExitCode reason) {
        if (listener != null) {
            System.out.println(NAME + "Notifying app of client stopping.");
            listener.onClientStop(reason);
        }
    }

    public void addClientListener(ClientListener l) {
        listener = l;
        SystemTrayListener systrayListener = new SystemTrayListenerImp(listener);
        tray.addSystemTrayListener(systrayListener);
        tray.displayIconOnSystemTray(ssi.sysTrayIcon, ssi.enableTrayActions);

        NetworkConnectionListener netConnListener = new NetworkConnectionListenerImp(listener);
        if (sender != null)
            sender.addNetworkConnectionListener(netConnListener);
        else
            System.out.println(NAME + "ERROR - Cannot add listener to network connection.");
    }

    @Override
    public void onScreenCaptured(BufferedImage screen) {
        PerformanceStats.incrFramesCaptured(1);
    }
}