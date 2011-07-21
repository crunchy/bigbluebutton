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
package org.bigbluebutton.deskshare.client.logging;

import org.bigbluebutton.deskshare.client.ScreenShareInfo;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceStats
{
    // singleton for sharing across the app
    private static PerformanceStats instance;
    
    private static Timer timer;
    private static int statsInterval = 2000;

    private static boolean running;
    private static PerformanceSampler sampler;
    private static Sample sample = new Sample();
    
    private PerformanceStats() {}

    private PerformanceStats(int interval) {
        setStatsInterval(interval);
    }
    
    public static PerformanceStats getInstance() {
        if (instance == null) {
            instance = new PerformanceStats();
        }
        return instance;
    }
    
    public static PerformanceStats getInstance(int interval) {
        return setStatsInterval(interval);
    }
    
    public static PerformanceStats setStatsInterval(int interval) {
        statsInterval = interval;
        return getInstance();
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public static PerformanceStats start() {
        // start is called from the setters as a precaution
        // if statsLogging is turned off, stop this
        if (!ScreenShareInfo.statsLogging) {
            stop();
            return getInstance();
        }
        
        if (timer != null || running) {
            return getInstance();
        }
        
        // start sampling
        System.out.println("Starting performance logging");
        sampler = new PerformanceSampler();
        sample = new Sample();
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // let's add a sample to our collection
                sampler.add(sample);
                sampler.computeSnapshot();
                System.out.println(sampler.getLastResultsAsString());
                sample = new Sample();
            }
        }, statsInterval, statsInterval);
        running = true;
        return getInstance();
    }
    
    public static PerformanceStats stop() {
        if (timer != null) {
            timer.cancel();
        }
        running = false;
        return getInstance();
    }
    
    /***************************************
     * stats logging
    ***************************************/
    public static void incrBytesSent(int bytes) {
        if (!ScreenShareInfo.statsLogging) {
            return;
        }
        start();
        sample.addBytes(bytes);
    }

    public static void incrMessagesSent(int messages) {
        if (!ScreenShareInfo.statsLogging) { 
            return; 
        }
        start();
        sample.addMessages(messages);
    }

    public static void incrBlocksSent(int blocks) {
        if (!ScreenShareInfo.statsLogging) { 
            return; 
        }
        start();
        sample.addBlocks(blocks);
    }

    public static void setStartTime(long time) {
        if (!ScreenShareInfo.statsLogging) { 
            return; 
        }
        start();
        sample.setTime(time);
    }

    public static void incrFramesCaptured(int frames) {
        if (!ScreenShareInfo.statsLogging) { 
            return; 
        }
        start();
        sample.addFrames(frames);
    }
    
    public static void incrKeyFramesSent(int kf) {
        if (!ScreenShareInfo.statsLogging) { 
            return; 
        }
        start();
        sample.addKeyFrames(kf);
    }
    
    public static long now() {
        return System.currentTimeMillis();
    }
}