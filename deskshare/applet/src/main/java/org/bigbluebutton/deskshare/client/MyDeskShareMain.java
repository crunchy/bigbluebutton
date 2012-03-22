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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyDeskShareMain implements ClientListener {
    private final BlockingQueue<ExitCode> exitReasonQ = new LinkedBlockingQueue<ExitCode>(5);

    private static DeskshareClient client;
    private static final int DESKSHARE_PORT = 9123;

    public static void main(String[] args) {
	MyDeskShareMain dsMain = new MyDeskShareMain();

	String hostValue = "ss-staging-us.salescrunch.com";
	String roomId = "mr7";
	int listenPortValue = DESKSHARE_PORT;

//		cli: host room port (all optional)
	switch (args.length) {
	    case 3:
		int port = 0;
		try {
		    port = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
		    System.err.println("Port must be integer");
		}
		if (port > 0) {
		    listenPortValue = port;
		}
//				falling through to next case on purpose
	    case 2:
		roomId = args[1];
		// falling through to next case on purpose
	    case 1:
		hostValue = args[0];
	    default:
	}

	client = new DeskshareClient.NewBuilder()
	    .host(hostValue)
	    .port(listenPortValue)
	    .room(roomId)
	    .fullScreen(true)
	    .quality(true)
//			.captureWidth(800)
//			.captureHeight(600)
//			.scaleWidth(800)
//			.scaleHeight(600)
//			.aspectRatio(false)
//			.x(0)
//			.y(0)
	    .build();

	client.addClientListener(dsMain);
	client.start();

	try {
	    System.out.println("Waiting for trigger to stop client.");
	    ExitCode reason = dsMain.exitReasonQ.take();
	    System.out.println("Stopping client.");
	    client.stop();

	    System.exit(reason.getExitCode());
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    System.exit(500);
	}
    }

    public void onClientStop(ExitCode reason) {
	queueExitCode(reason);
    }

    private void queueExitCode(ExitCode reason) {
	try {
	    exitReasonQ.put(reason);
	    System.out.println("Triggered stop client.");
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    client.stop();

	    System.exit(reason.getExitCode());
	}
    }
}
