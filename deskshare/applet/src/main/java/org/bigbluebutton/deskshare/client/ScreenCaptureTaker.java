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

import java.awt.image.BufferedImage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ScreenCaptureTaker {	
	private ScreenCapture capture;
	private ScreenCaptureListener listeners;
	
	private volatile boolean startCapture = false;
	private final Executor screenCapTakerExec = Executors.newSingleThreadExecutor();
	private Runnable screenCapRunner;
	
	public ScreenCaptureTaker(int x, int y, int captureWidth, int captureHeight, int scaleWidth, int scaleHeight, boolean quality){
		capture = new ScreenCapture(x, y, captureWidth, captureHeight, scaleWidth, scaleHeight, quality);
	}

	public void setCaptureCoordinates(int x, int y){
		capture.setX(x);
		capture.setY(y);
	}
	
	private long captureScreen() {		
		long start = System.currentTimeMillis();
		BufferedImage image = capture.takeSingleSnapshot();
		long duration = System.currentTimeMillis() - start;
		notifyListeners(image);
		return duration;
	}
	
	private void notifyListeners(BufferedImage image) {
		listeners.onScreenCaptured(image);
	}
		
	public void addListener(ScreenCaptureListener listener) {
		listeners = listener;
	}

	private void pause(int dur) {
		try{
			Thread.sleep(dur);
		} catch (Exception e){
			System.out.println("Exception sleeping.");
		}
	}
	
	public void start() {
		// System.out.println("Starting screen capture.");		
		startCapture = true;
		screenCapRunner =  new Runnable() {
			public void run() {
				// manually adjust the pause depending on how long it takes to capture
				// but not too often (once every 100 frames)
				// the idea is to capture as often as possible to hit some FPS
				// all durations in ms
				long captureDuration = 0;
				int cycle = 0;  // when this reaches 100, re-examine the pause duration
				int idealPauseDuration = 100;
				int maxCapturePauseDuration = 120;
				int pauseDuration = idealPauseDuration;
				
				while (startCapture){
					captureDuration = captureScreen();
					pause(pauseDuration);
					if (++cycle >= 100) {
						cycle = 0;
						if (captureDuration + pauseDuration > maxCapturePauseDuration) {
							pauseDuration = Math.max(10, (int)(maxCapturePauseDuration - captureDuration)); // at least 10
						} else {
							pauseDuration = idealPauseDuration;
						}
					}
				}
				// System.out.println("Stopping screen capture.");		
			}
		};
		screenCapTakerExec.execute(screenCapRunner);	
	}
	
	public void stop() {
		startCapture = false;
	}
}
