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
 
import java.util.ArrayList;

 /**
  * performance snapshot
  */
public class PerformanceCalculator {
    
    private ArrayList<Sample> samples;
    private long duration;
    private double durationInSeconds;
    
    private int keyFrameCount;
    private int blockCount;
    private int messageCount;
    private int byteCount;
    
    // per second
    private double blockPS;
    private double messagePS;
    private double bytePS;

    private long minTime;
    private long maxTime;
    
    public PerformanceCalculator(ArrayList samples) {
        this.samples = samples;
        initialize();
    }
    
    public synchronized void add(Sample sample) {
        samples.add(sample);
    }
    
    public void initialize() {
        duration          = 0;
        durationInSeconds = 0;
        
        keyFrameCount = 0;
        blockCount    = 0;
        messageCount  = 0;
        byteCount     = 0;
        
        blockPS    = 0.0;
        messagePS  = 0.0;
        bytePS     = 0.0;

        minTime = 0;
        maxTime = 0;
    }
    
    public double getBlockPS() {
        return blockPS;
    }

    public double getMessagePS() {
        return messagePS;
    }

    public double getBytePS() {
        return bytePS;
    }

    public synchronized PerformanceCalculator compute() {
        initialize();
        if (this.samples.size() < 2) {
            System.out.println("Error - not enough samples");
            return this;
        }

        int lastIndex = samples.size();
        Sample latest = samples.get(lastIndex - 1);
        Sample first  = samples.get(0);
        minTime = Math.min(latest.getTime(), first.getTime());
        maxTime = Math.max(latest.getTime(), first.getTime());

        // shouldn't happen--samples should be consecutive
        if (minTime == maxTime) {
            System.out.println("Error - same time in both samples");
            return this;
        }
        
        Sample diff         = latest.diff(first);
        duration            = maxTime - minTime;
        durationInSeconds   = (double)duration / 1000;


        blockCount          = diff.getBlockCount();
        messageCount        = diff.getMessageCount();
        keyFrameCount       = diff.getKeyFrameCount();
        byteCount           = diff.getByteCount();

        blockPS             = diff.getBlockCount()    / durationInSeconds;
        messagePS           = diff.getMessageCount()  / durationInSeconds;
        bytePS              = diff.getByteCount()     / durationInSeconds;
        
        return this;
    }
    
    @Override
    public String toString() {
        return String.format(  "Time: %,d - Duration: %,.3f s\nKF: %,d - Msg: %,d - Blk: %,d - KB: %,.2f\nMsg/s: %,.2f - Blk/s: %,.2f - KB/s: %,.2f",
                                maxTime, durationInSeconds, 
                                keyFrameCount, messageCount, blockCount, (double)byteCount / 1024,
                                messagePS, blockPS, (double)bytePS / 1024);
    }
}