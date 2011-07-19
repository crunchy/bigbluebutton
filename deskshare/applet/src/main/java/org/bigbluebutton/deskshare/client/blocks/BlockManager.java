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
package org.bigbluebutton.deskshare.client.blocks;

import org.bigbluebutton.deskshare.client.net.BlockMessage;
import org.bigbluebutton.deskshare.common.Dimension;
import org.bigbluebutton.deskshare.client.ScreenShareInfo;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class BlockManager {
    private final Map<Integer, Block> blocksMap;
    private int numColumns;
    private int numRows;
    private int totalBlocks;

    private ChangedBlocksListener listeners;
    private Dimension screenDim, blockDim;

    public BlockManager() {
        blocksMap = new HashMap<Integer, Block>();
    }

    public void initialize(Dimension screen, Dimension tile) {
        screenDim = screen;
        blockDim = tile;

        BlockFactory factory = new BlockFactory(screen, tile);

        numColumns = factory.getColumnCount();
        numRows = factory.getRowCount();
        totalBlocks = numColumns * numRows;

        for (int position = 1; position <= getBlockCount(); position++) {
            Block block = factory.createBlock(position);
            blocksMap.put(position, block);
        }
    }

    public void processCapturedScreen(BufferedImage capturedScreen) {
        Vector<Integer> changedBlocks = getChangedBlocks(capturedScreen, false);
        boolean forceKeyFrame = false;
        int changedCount = changedBlocks.size();
        if (changedCount > 0) {
            int numberOfBlocks = getBlockCount();
            if (numberOfBlocks > 0) {
                double pctChanged = (double)changedCount / numberOfBlocks;
                if (pctChanged > ScreenShareInfo.getKeyframeTriggerThreshold()) {
                    forceKeyFrame = true;
                    System.out.println(100 * pctChanged + "% blocks changed ********");
                    // mark all the blocks as changed
                    changedBlocks = getChangedBlocks(capturedScreen, true);
                }
            }
            // chunk up the refresh
            partitionBlockMessages(changedBlocks, totalBlocks / ScreenShareInfo.NETWORK_SENDER_COUNT, forceKeyFrame);
        }
    }

    private Vector<Integer> getChangedBlocks(BufferedImage capturedScreen, boolean forceAllBlocks) {
        Vector<Integer> changedBlocks = new Vector<Integer>();
    
        for (int position = getBlockCount(); position >= 1; position--) {
            // no need to get the block 
            if (forceAllBlocks || blocksMap.get(position).hasChanged(capturedScreen)) {
                changedBlocks.add(position);
            }
        }
        return changedBlocks;
    }

    private void partitionBlockMessages(Vector<Integer> changedBlocks, int blocksPerSection, boolean forceKeyFrame) {
        int changedCount = changedBlocks.size();
        List<Integer> section;
        Integer[] bc;

        // nudge # blocks per section; if not, when sending keyframes, 
        // you wind up partitioning into two sections, and the 2nd one is tiny 
        // (always 44 bytes in my test). There's no risk of overflow b/c the
        // offset is bounded by changedCount in the loop
        // I don't quite understand this yet
        ++blocksPerSection;
        
        for (int start = 0; start <= changedCount; start += blocksPerSection) {
            int offset = start + blocksPerSection;

            if (offset > changedCount) { 
                offset = changedCount; 
            }

            section = changedBlocks.subList(start, offset);

            bc = new Integer[section.size()];
            section.toArray(bc);
            notifyChangedBlockListener(new BlockMessage(bc, forceKeyFrame));
        }
    }

    private void notifyChangedBlockListener(BlockMessage position) {
        listeners.onChangedBlock(position);
    }

    public void addListener(ChangedBlocksListener listener) {
        listeners = listener;
    }

    public void removeListener(ChangedBlocksListener listener) {
        listeners = null;
    }

    public void blockSent(int position) {
        Block block = blocksMap.get(position);
        block.sent();
    }

    public Block getBlock(int position) {
        return blocksMap.get(position);
    }

    public int getRowCount() {
        return numRows;
    }

    public int getColumnCount() {
        return numColumns;
    }

    public int getBlockCount() {
        return numColumns * numRows;
    }

    public Dimension getScreenDim() {
        return screenDim;
    }

    public Dimension getBlockDim() {
        return blockDim;
    }
    
    public void setKeyframeThreshold(double t) {
        ScreenShareInfo.getInstance().setKeyframeTriggerThreshold(t);
    }
}