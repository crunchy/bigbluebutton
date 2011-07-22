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
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

 /**
  * implements a rolling window of performance snapshots and computes stats
  */
public class PerformanceSampler {
    
    // samples to generate diff
    private LinkedBlockingQueue<Sample> queue;
    public static final int NUM_SAMPLES = 2;
    // computed averages to retain
    private LinkedBlockingDeque<PerformanceCalculator> results;
    public static final int NUM_SNAPSHOTS = 60; 
    // number of snapshots to inspect to determine slowdown
    public static final int DEFAULT_SLOWDOWN_WINDOW = 15;
    // if latest sample is older than this, don't report (in ms)
    public static final int MAX_SAMPLE_AGE = 30000;
    
    public PerformanceSampler() {
        queue = new LinkedBlockingQueue<Sample>(NUM_SAMPLES);
        results = new LinkedBlockingDeque<PerformanceCalculator>(NUM_SNAPSHOTS);
    }
    
    public PerformanceSampler(Sample sample) {
        queue = new LinkedBlockingQueue<Sample>(NUM_SAMPLES);
        results = new LinkedBlockingDeque<PerformanceCalculator>(NUM_SNAPSHOTS);
        add(sample);
    }
    
    public PerformanceSampler add(Sample sample) {
        shove(sample, queue);
        return this;
    }
    
    /**
      * tries to stick an object onto a given queue
      * if no room, pops off the oldest one 
    */
    private <T> void shove(T obj, Queue<T> q) {
        if (!q.offer(obj)) {
            q.poll();
            q.offer(obj);
        }
    }

    // *********
    // accessors
    // *********
    public boolean isFull() {
        return (queue.size() >= NUM_SAMPLES);
    }
    
    public boolean isResultsFull() {
        return (results.size() >= NUM_SNAPSHOTS);
    }
    
    public PerformanceCalculator getLastResults() {
        return results.isEmpty() ? null : results.getLast();
    }
    
    public String getLastResultsAsString() {
        PerformanceCalculator last = getLastResults();
        return last == null ? "No results" : last.toString();
    }
    
    /** 
      @return true if the last x samples are getting slower
    */
    public boolean isSlowingDown() {
        return isSlowingDown(DEFAULT_SLOWDOWN_WINDOW);
    }
    
    /** 
      @return true if the last numSamples samples are getting slower
      @param numSamples number of samples to inspect
    */    
    public boolean isSlowingDown(int numSamples) {
        // you don't know if it's slowing down until you have 2+ samples
        if (results.size() < 2 || numSamples < 2) {
            return false;
        }
        
        boolean slow = false;
        int whereAmI = 0;
        long now = System.currentTimeMillis();
        PerformanceCalculator calc;
        
        RollingAverage avg = new RollingAverage(3, 3);
        Iterator it = results.descendingIterator();
        long age;
        while (it.hasNext()) {
            calc = (PerformanceCalculator)it.next();
            if (!calc.hasValidThroughput()) {
                continue;
            }
            // stop once you've reached old samples
            age = now - calc.getMaxTime();
            if (now - calc.getMaxTime() > MAX_SAMPLE_AGE) {
                break;
            }
            // add the sample to our calculator
            avg.addToBuckets(whereAmI, calc.getThroughput());
            ++whereAmI;
            //System.out.printf("Sample age " + age + " - time %,d - throughput %,.4f\n", calc.getMaxTime(), calc.getThroughput());
        }
        avg.computeAverages();
        return avg.isDecreasing();
    }
    
    // ************************
    // calculates 
    // ************************
    public synchronized PerformanceCalculator computeSnapshot() {
        // need at least two samples
        if (queue.size() < NUM_SAMPLES) {
            return null;
        }
        ArrayList<Sample> samples = new ArrayList<Sample>(NUM_SAMPLES);
        queue.drainTo(samples, NUM_SAMPLES);
        PerformanceCalculator calc = new PerformanceCalculator(samples);
        shove(calc.compute(), results);
        return calc;
    }
}