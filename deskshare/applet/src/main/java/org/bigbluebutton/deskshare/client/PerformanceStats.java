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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceStats
{
    // singleton for sharing across the app
    private static PerformanceStats instance;
    
    private static AtomicInteger framesCaptured = new AtomicInteger();
    private static AtomicInteger bytesSent = new AtomicInteger();
    private static AtomicInteger blocksSent = new AtomicInteger();
    private static AtomicInteger messagesSent = new AtomicInteger();
    private static AtomicLong startTime = new AtomicLong();
    private static AtomicLong endTime = new AtomicLong();
    private static Timer timer;
    private static String STATS_FORMAT = "s: %.3f; frames: %,d; blocks: %, d; messages: %,d; kB: %,.3f; fps: %.2f; \n";
    private static long statsInterval = 1000;

    private static boolean running;
    
    private PerformanceStats() {}
        
    private PerformanceStats(long interval) {
        setStatsInterval(interval);
    }
    
    public static PerformanceStats getInstance() {
        if (instance == null) {
            instance = new PerformanceStats();
        }
        return instance;
    }
    
    public static PerformanceStats getInstance(long interval) {
        return setStatsInterval(interval);
    }
    
    public static PerformanceStats setStatsInterval(long interval) {
        statsInterval = interval;
        return getInstance();
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public static PerformanceStats start() {
        if (timer != null || running) {
            return getInstance();
        }
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // todo: replace printf with sprintf
                asString();
            }
        }, statsInterval, statsInterval);
        startTime.set(now());
        running = true;
        return getInstance();
    }
    
    public static PerformanceStats stop() {
        if (timer != null) {
            timer.cancel();
        }
        endTime.set(now());
        running = false;
        return getInstance();
    }
    
    /***************************************
     * stats logging
    ***************************************/
    public static PerformanceStats incrBytesSent(int bytes) {
        start();
        bytesSent.addAndGet(bytes);
        return instance;
    }

    public static PerformanceStats incrMessagesSent(int messages) {
        start();
        messagesSent.addAndGet(messages);
        return instance;
    }

    public static PerformanceStats incrBlocksSent(int blocks) {
        start();
        blocksSent.addAndGet(blocks);
        return instance;
    }

    public static PerformanceStats setStartTime(long time) {
        start();
        startTime.addAndGet(time);
        return instance;
    }

    public static PerformanceStats incFramesCaptured(int frames) {
        start();
        framesCaptured.addAndGet(frames);
        return instance;
    }

    public static long now() {
        return System.currentTimeMillis();
    }
    
    public static String asString() {
        if (!running) {
            return "not running";
        }
        float duration = (now() - startTime.get()) / 1000F;
        int frames = framesCaptured.get();
        float fps = frames / duration;
        float kB  = bytesSent.get() / 1024F;
        System.out.printf(STATS_FORMAT, duration, frames, blocksSent.get(), messagesSent.get(), kB, fps);
        return "Pretty stats";
    }
}