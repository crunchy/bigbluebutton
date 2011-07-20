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
package org.bigbluebutton.deskshare.client.net;

import java.util.*;
//import java.util.Arrays;

public class BlockMessage implements Message {

    private Integer[] blocks;
    private boolean forceKeyFrame;
    
    public BlockMessage(Integer[] blocks) {
        setBlocks(blocks);
        forceKeyFrame = false;
    }

    public BlockMessage(Integer[] blocks, boolean fkf) {
        setBlocks(blocks);
        forceKeyFrame = fkf;
    }
    /**
     * sets blocks and sorts them
     * sorted is crucial; removing the sort will break other methods
    */
    public void setBlocks(Integer[] blocks) {
        this.blocks = blocks;
        Arrays.sort(this.blocks);
    }
    
    public boolean discardBlock(int at) {
        if (at < 0 || at >= this.blocks.length) {
            return false;
        }
        Integer[] tmp = new Integer[blocks.length - 1];
        System.arraycopy(blocks, 0, tmp, 0, at);
        if (blocks.length != at) {
            System.arraycopy(
                blocks, at+1, 
                tmp, at, 
                blocks.length - at - 1);
        }
        setBlocks(tmp);
        return true;
    }
    
    public boolean discardBlockByValue(int value) {
        boolean result = false;
        synchronized(blocks) {
            int index = Arrays.binarySearch(blocks, new Integer(value));
            if (index >= 0) {
                result = discardBlock(index);
            }
        }
        return result;
    }
    
    @Override
    public MessageType getMessageType() {
        return MessageType.BLOCK;
    }

    @Override
    public boolean isBlockMessage() {
        return true;
    }
    
    @Override
    public boolean isCursorMessage() {
        return false;
    }

    public Integer[] getBlocks() {
        return blocks;
    }
    
    public int size() {
        return blocks.length;
    }
    
    public boolean getForceKeyFrame() {
        return forceKeyFrame;
    }
    
    public boolean isEmpty() {
        return (blocks.length == 0);
    }
    
    /**
     * @returns true if another message has the same blocks as this
     * depends on the blocks being sorted in setBlocks
    */
    public boolean hasSameBlocksAs(BlockMessage that) {
        return Arrays.equals(this.blocks, that.getBlocks());
    }
    
    // find duplicate blocks
    public BlockMessage discardBlocksSharedWith(BlockMessage that) {
        ArrayList<Integer> dupes = new ArrayList<Integer>();

        // first find the duplicates
        for (Integer blockNumber: this.blocks) {
            if (Arrays.binarySearch(that.getBlocks(), blockNumber) >= 0) {
                dupes.add(new Integer(blockNumber));
            }
        }
        System.out.println("Found " + dupes.size() + " duplicates out of " + this.blocks.length);
        
        // then remove the dupes from this message
        for(Integer dupeBlockNumber: dupes) {
            discardBlockByValue(dupeBlockNumber);
        }

        return this;
    }
}