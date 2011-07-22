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

// slowing down = x consecutive rolling averages 
// that are some smaller than the previous one
public class RollingAverage {
    private int numBuckets, bucketSize;
    private double[] buckets, averages;
    private int[] actualBucketSize;
    // sample 2 is "less than" sample 1 if sample 2 < SHRINKAGE * sample 1
    private static final double SHRINKAGE = .90; 
    
    public RollingAverage(int numBuckets, int bucketSize) {
        this.bucketSize = bucketSize;
        this.numBuckets = numBuckets;
        this.buckets = new double[numBuckets];
        this.averages = new double[numBuckets];
        this.actualBucketSize = new int[numBuckets];
    }
    
    public double[] addToBuckets(int rank, double value) {
        int firstBucket = rank % bucketSize;
        for (int i = firstBucket; i < Math.min(numBuckets, firstBucket + bucketSize); ++i) {
            buckets[i] += value;
            actualBucketSize[i] += 1;
        }
        return buckets;
    }
    
    public double[] computeAverages() {
        for (int i = 0; i < numBuckets; ++i) {
            if (actualBucketSize[i] != 0) {
                averages[i] = buckets[i] / actualBucketSize[i];
            } else {
                averages[i] = 0;
            }
        }
        return averages;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < numBuckets; ++i) {
            sb.append("Sample " + i + " = " + averages[i] + "\n");
        }
        return sb.toString();
    }
    
    public boolean isDecreasing() {
        int decreaseEvents = 0;
        int possibleEvents = 0;
        int events = 0;
        for (int i = 0; i < numBuckets - 1; ++i) {
            ++events;
            if (actualBucketSize[i] > 0 && actualBucketSize[i+1] > 0) {
                ++possibleEvents;
                if (averages[i+1] < SHRINKAGE * averages[i]) {
                    ++decreaseEvents;
                }
            }
        }
        return (decreaseEvents == possibleEvents);
    }
}

