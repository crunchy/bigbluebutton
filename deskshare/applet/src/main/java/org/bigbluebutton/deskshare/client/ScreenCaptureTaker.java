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

import org.bigbluebutton.deskshare.client.image_filters.SimpleFilter;

import java.awt.image.BufferedImage;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ScreenCaptureTaker {
    private final ScreenCapture capture;
    private final Vector<SimpleFilter> filters;
    private final Vector<ScreenCaptureListener> listeners;

    private volatile boolean startCapture = false;
    private final Executor screenCapTakerExec = Executors.newSingleThreadExecutor();
    private int pauseDuration = ScreenShareInfo.IDEAL_PAUSE_DURATION;
    private final int PAUSE_DURATION_RECOMPUTE_FREQUENCY = 25; // recompute pause duration every x frames

    public ScreenCaptureTaker(ScreenCapture capture) {
        this.capture = capture;
        this.filters = new Vector<SimpleFilter>();
        this.listeners = new Vector<ScreenCaptureListener>();
    }

    public void setCaptureCoordinates(int x, int y) {
        capture.setLocation(x, y);
    }

    private long captureScreen() {
        long start = System.currentTimeMillis();

        BufferedImage image = capture.takeSingleSnapshot();

        for (SimpleFilter filter : filters) {
            image = filter.filter(image);
        }

        long duration = System.currentTimeMillis() - start;

        notifyListeners(image);
        return duration;
    }

    private void notifyListeners(BufferedImage image) {
        for(ScreenCaptureListener listener: listeners) {
                listener.onScreenCaptured(image);
        }
    }

    public void addListener(ScreenCaptureListener listener) {
        listeners.add(listener);
    }

    public void addFilter(SimpleFilter filter) {
        filters.add(filter);
    }

    private void pause(int dur) {
        try {
            Thread.sleep(dur);
        } catch (Exception e) {
            System.out.println("Exception sleeping.");
        }
    }

    public void start() {
        // System.out.println("Starting screen capture.");
        startCapture = true;
        Runnable screenCapRunner = new Runnable() {
            public void run() {
                // adjust the pause depending on how long it takes to capture
                // but not too often (once every 100 frames)
                // the idea is to capture as often as possible to hit some FPS
                // all durations in ms
                long captureDuration = 0;
                int cycle = 0;  // when this reaches PAUSE_DURATION_RECOMPUTE_FREQUENCY, re-examine the pause duration
                pauseDuration = ScreenShareInfo.IDEAL_PAUSE_DURATION;

                while (startCapture) {
                    captureDuration = captureScreen();
                    pause(pauseDuration);
                    if (++cycle >= PAUSE_DURATION_RECOMPUTE_FREQUENCY) {
                        // evaluate the queue size and see if we need to adjust the pause
                        cycle = 0;
                        recomputePauseDuration(captureDuration);
                    }
                }
                // System.out.println("Stopping screen capture.");
            }
        };
        screenCapTakerExec.execute(screenCapRunner);
    }

    public int recomputePauseDuration(long captureDuration) {
        if (captureDuration + pauseDuration > ScreenShareInfo.MAX_PAUSE_DURATION) {
            pauseDuration = Math.max(10,
                (int) (ScreenShareInfo.MAX_PAUSE_DURATION - captureDuration)); // at least 10
        } else {
            pauseDuration = ScreenShareInfo.IDEAL_PAUSE_DURATION;
        }
        return pauseDuration;
    }
    
    public int recomputePauseDurationForQueue(int queueSize) {
        // let's make pauseDuration 20% longer for now
        pauseDuration = Math.min(ScreenShareInfo.MAX_PAUSE_DURATION, (int)(pauseDuration * 1.2));
        System.out.println("New pause duration is " + pauseDuration);
        return pauseDuration;
    }
    
    public void stop() {
        startCapture = false;
    }
}
