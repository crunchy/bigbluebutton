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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyDeskShareMain implements ClientListener, LifeLineListener {
    private final BlockingQueue<ExitCode> exitReasonQ = new LinkedBlockingQueue<ExitCode>(5);

    private static LifeLine lifeline;
    private static DeskshareClient client;

    public static void main(String[] args) {
        MyDeskShareMain dsMain = new MyDeskShareMain();

        String hostValue        = "bbb-staging.crunchconnect.com";
        Integer listenPortValue = 9125;
        String iconValue        = "bbb.gif";

        Image image = Toolkit.getDefaultToolkit().getImage(iconValue);

        lifeline = new LifeLine(listenPortValue, dsMain);
        lifeline.listen();

        client = new DeskshareClient.NewBuilder()
                .host(hostValue)
                .port(9123)
                .room("mr-510")
                .captureWidth(800)
                .captureHeight(600)
                .scaleWidth(800)
                .scaleHeight(600)
                .quality(false)
                .aspectRatio(false)
                .x(0)
                .y(0)
                .fullScreen(true)
                .httpTunnel(false)
                .trayIcon(image)
                .enableTrayIconActions(false)
                .build();

        client.addClientListener(dsMain);
        client.start();

        try {
            System.out.println("Waiting for trigger to Stop client.");
            ExitCode reason = dsMain.exitReasonQ.take();
            System.out.println("Stopping client.");
            client.stop();
            lifeline.disconnect();
            System.exit(reason.getExitCode());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(500);
        }
    }

    public void onClientStop(ExitCode reason) {
        queueExitCode(reason);
    }

    @Override
    public void disconnected(ExitCode reason) {
        queueExitCode(reason);
    }

    private void queueExitCode(ExitCode reason) {
        try {
//			System.out.println("Trigger stop client ." + exitReasonQ.remainingCapacity());
            exitReasonQ.put(reason);
            System.out.println("Triggered stop client.");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            client.stop();
            lifeline.disconnect();
            System.exit(reason.getExitCode());
        }
    }
}
