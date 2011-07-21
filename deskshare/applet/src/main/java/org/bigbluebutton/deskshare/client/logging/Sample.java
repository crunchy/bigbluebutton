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
 
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

 /**
  * performance snapshot
  */
public class Sample {
    
    private AtomicLong time = new AtomicLong();
    
    private AtomicInteger blockCount    = new AtomicInteger();
    private AtomicInteger messageCount  = new AtomicInteger();
    private AtomicInteger keyFrameCount = new AtomicInteger();
    private AtomicInteger byteCount     = new AtomicInteger();
    private AtomicInteger frameCount    = new AtomicInteger();
   
    // *****************************
    // constructors and initializers
    // *****************************
    public Sample() {
        initialize();
    }
    
    public Sample(long time) {
        initialize();
        this.time.set(time);
    }
    
    public void initialize() {
        time.set(System.currentTimeMillis());
        keyFrameCount.set(0);
        blockCount.set(0);
        messageCount.set(0);
        byteCount.set(0);
        frameCount.set(0);
    }

    // *******
    // setters
    // *******
    
    public Sample setTime() {
        time.set(System.currentTimeMillis());
        return this;
    }

    public Sample setTime(long t) {
        time.set(t);
        return this;
    }
    
    public Sample setBlockCount(int b) {
        blockCount.set(b);
        return this;
    }
    
    public Sample setMessageCount(int b) {
        messageCount.set(b);
        return this;
    }
    public Sample setKeyFrameCount(int b) {
        keyFrameCount.set(b);
        return this;
    }

    public Sample setByteCount(int b) {
        byteCount.set(b);
        return this;
    }

    public Sample setFrameCount(int b) {
        frameCount.set(b);
        return this;
    }

    // ******
    // adders
    // ******
    public void addBlocks(int b) {
        blockCount.addAndGet(b);
    }
    
    public void addMessages(int m) {
        messageCount.addAndGet(m);
    }

    public void addKeyFrames(int kf) {
        keyFrameCount.addAndGet(kf);
    }

    public void addBytes(int b) {
        byteCount.addAndGet(b);
    }

    public void addFrames(int frames) {
        frameCount.addAndGet(frames);
    }
    
    // *******
    // getters
    // *******
    public long getTime() {
        return time.get();
    }
    
    public int getBlockCount() {
        return blockCount.get();
    }
    
    public int getMessageCount() {
        return messageCount.get();
    }

    public int getKeyFrameCount() {
        return keyFrameCount.get();
    }
    
    public int getByteCount() {
        return byteCount.get();
    }
    
    public int getFrameCount() {
        return frameCount.get();
    }
    
    @Override
    public String toString() {
        return "Sample - time: " + getTime() + " - keyframe: " + getKeyFrameCount() + " - blocks: " + getBlockCount() + " - messages: " + getMessageCount() + " - bytes: " + getByteCount();
    }
    
    // ****************
    // diff two samples
    // ****************
    public Sample diff(Sample that) {
        Sample diffSample = new Sample(Math.max(this.getTime(), that.getTime()));
        diffSample.
            setBlockCount(Math.abs(that.getBlockCount() - this.getBlockCount())).
            setMessageCount(Math.abs(that.getMessageCount() - this.getMessageCount())).
            setKeyFrameCount(Math.abs(that.getKeyFrameCount() - this.getKeyFrameCount())).
            setByteCount(Math.abs(that.getByteCount() - this.getByteCount())).
            setFrameCount(Math.abs(that.getFrameCount() - this.getFrameCount()));
        ;
        return diffSample;
    }
    
    
}