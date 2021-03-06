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

import java.awt.*;
import javax.swing.*;
import org.bigbluebutton.deskshare.client.ScreenShareInfo;

public class DeskShareApplet extends JApplet implements ClientListener {
    
    public static final String NAME = "DESKSHAREAPPLET: ";
    private static final long serialVersionUID = 1L;
    private static boolean running = false;

    String hostValue = "localhost";
    Integer portValue = 9123;
    String roomValue = "85115";
    Integer cWidthValue = 800;
    Integer cHeightValue = 600;
    Integer sWidthValue = 800;
    Integer sHeightValue = 600;
    Boolean qualityValue = false;
    Boolean aspectRatioValue = false;
    Integer xValue = 0;
    Integer yValue = 0;
    Boolean tunnelValue = true;
    Boolean fullScreenValue = false;
    DeskshareClient client;
    Image icon;

    public boolean isSharing = false;

    @Override
    public void init() {
        System.out.println("Desktop Sharing Applet Initializing");
        hostValue = getParameter("IP");
        String port = getParameter("PORT");
        if (port != null) {
            portValue = Integer.parseInt(port);
        }
        roomValue = getParameter("ROOM");

        String captureFullScreen = getParameter("FULL_SCREEN");
        if (captureFullScreen != null) {
            fullScreenValue = Boolean.parseBoolean(captureFullScreen);
        }

        String tunnel = getParameter("HTTP_TUNNEL");
        if (tunnel != null) {
            tunnelValue = Boolean.parseBoolean(tunnel);
        }
        icon = getImage(getCodeBase(), "bbb.gif");
    }

    @Override
    public void start() {
        if (running) {
            System.out.println("Already running");
            return;
        }
        System.out.println("Desktop Sharing Applet Starting");
        super.start();
        client = new DeskshareClient.NewBuilder()
            .host(hostValue)
            .port(portValue)
            .room(roomValue)
            .captureWidth(cWidthValue)
            .captureHeight(cHeightValue)
            .scaleWidth(sWidthValue)
            .scaleHeight(sHeightValue)
            .quality(qualityValue)
            .aspectRatio(aspectRatioValue)
            .x(xValue)
            .y(yValue)
            .fullScreen(fullScreenValue)
            .httpTunnel(tunnelValue)
            .trayIcon(icon)
            .enableTrayIconActions(false)
            .build();
        client.addClientListener(this);
        // for debugging; remove
        displaySettings(); 
        running = true;
        client.start();
    }

    @Override
    public void destroy() {
        System.out.println("Desktop Sharing Applet Destroy");
        running = false;
        client.stop();
        super.destroy();
    }

    @Override
    public void stop() {
        System.out.println("Desktop Sharing Applet Stopping");
        running = false;
        client.stop();
        super.stop();
    }

    public void onClientStop(ExitCode reason) {
        running = false;
        client.stop();
    }
    
    // interact with settings from client app
    public void displaySettings() {
        System.out.println("Config:\n" + ScreenShareInfo.asString());
    }
    
    public String getSettings() {
        return ScreenShareInfo.asString();
    }
    
    public void setColorDepth(int depth) {
        ScreenShareInfo.setColorDepth(depth);
    }
    
    public void increaseColorDepth() {
        ScreenShareInfo.increaseColorDepth();
    }
    
    public void decreaseColorDepth() {
        ScreenShareInfo.decreaseColorDepth();
    }
    
    public void setMaxColorDepth() {
        ScreenShareInfo.setMaxColorDepth();
    }
    
    public void setMinColorDepth() {
        ScreenShareInfo.setMinColorDepth();
    }
    
    public void setKeyframeTriggerThreshold(double t) {
        ScreenShareInfo.setKeyframeTriggerThreshold(t);
    }

}