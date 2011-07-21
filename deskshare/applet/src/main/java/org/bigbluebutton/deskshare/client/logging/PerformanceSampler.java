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
    public static final int NUM_SNAPSHOTS = 20; 
    
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
    
    private <T> void shove(T obj, Queue<T> q) {
        if (!q.offer(obj)) {
            q.poll();
            q.offer(obj);
        }
    }
    
    public boolean isFull() {
        return (queue.size() >= NUM_SAMPLES);
    }
    
    public boolean isResultsFull() {
        return (results.size() >= NUM_SNAPSHOTS);
    }
    
    public synchronized void computeSnapshot() {
        // need at least two samples
        if (queue.size() < NUM_SAMPLES) {
            return;
        }
        ArrayList<Sample> samples = new ArrayList<Sample>(NUM_SAMPLES);
        queue.drainTo(samples, NUM_SAMPLES);
        PerformanceCalculator calc = new PerformanceCalculator(samples);
        shove(calc.compute(), results);
    }
    
    public PerformanceCalculator getLastResults() {
        return results.isEmpty() ? null : results.getLast();
    }
    
    public String getLastResultsAsString() {
        PerformanceCalculator last = getLastResults();
        return last == null ? "No results" : last.toString();
    }
}