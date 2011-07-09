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
import java.awt.image.BufferedImage;


/**
 * The Capture class uses the java Robot class to capture the screen.
 *
 * @author Snap
 */
public class ScreenCapture {
    private Robot robot;
    private Rectangle captureBounds;

    public ScreenCapture(int x, int y, int captureWidth, int captureHeight) {
	try {
	    robot = new Robot();
	} catch (AWTException e) {
	    System.out.println(e.getMessage());
	}

	this.captureBounds = new Rectangle(x, y, captureWidth, captureHeight);
    }

    public BufferedImage takeSingleSnapshot() {
	return robot.createScreenCapture(this.captureBounds);
    }

    public void setLocation(int x, int y) {
	captureBounds.setLocation(x, y);
    }
}
