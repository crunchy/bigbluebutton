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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ScreenShareInfo {
    
    // retain as public for backward compatibility
    public static String host;
    public static int port;
    public static String room;
    public static int captureWidth;
    public static int captureHeight;
    public static int scaleWidth;
    public static int scaleHeight;
    public static boolean quality;
    public static boolean aspectRatio;
    public static int x;
    public static int y;
    public static boolean httpTunnel;
    public static boolean fullScreen;
    public static Image sysTrayIcon;
    public static boolean enableTrayActions;
    public static Container contentPane;
    
    // singleton for sharing across the app
    private static ScreenShareInfo instance;
    private static double keyframeTriggerThreshold = 0.4;
    private static int colorDepth = BufferedImage.TYPE_USHORT_555_RGB;
    
    // to increase/decrease color depth
    private static final int NUM_COLOR_DEPTHS = 13;
    private static ArrayList<Integer> orderedColorDepths;
    private static int colorDepthIndex = 0;

    private ScreenShareInfo() {
        // ordering color depths based on file size obtained during test, increasing
        orderedColorDepths = new ArrayList<Integer>(NUM_COLOR_DEPTHS);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_BYTE_BINARY);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_BYTE_GRAY);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_USHORT_555_RGB);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_USHORT_565_RGB);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_4BYTE_ABGR_PRE);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_BYTE_INDEXED);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_USHORT_GRAY);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_3BYTE_BGR);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_INT_BGR);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_INT_RGB);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_4BYTE_ABGR);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_INT_ARGB_PRE);
        orderedColorDepths.add((Integer)BufferedImage.TYPE_INT_ARGB);
        // find whichever one is default
        findColorDepthIndex();
    }
        
    public static ScreenShareInfo getInstance() {
        if (instance == null) {
            instance = new ScreenShareInfo();
        }
        return instance;
    }
    
    public static int getColorDepth() {
        return colorDepth;
    }
    
    public static String getColorDepthName() {
        String name;
        switch(colorDepth) {
            case BufferedImage.TYPE_3BYTE_BGR:
                name = "3BYTE_BGR"; break;
            case BufferedImage.TYPE_4BYTE_ABGR:
                name = "4BYTE_BGR"; break;
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                name = "4BYTE_ABGR_PRE"; break;
            case BufferedImage.TYPE_BYTE_BINARY:
                name = "BYTE_BINARY"; break;
            case BufferedImage.TYPE_BYTE_GRAY:
                name = "BYTE_GRAY"; break;
            case BufferedImage.TYPE_BYTE_INDEXED:
                name = "BYTE_INDEXED"; break;
            case BufferedImage.TYPE_CUSTOM:
                name = "CUSTOM"; break;
            case BufferedImage.TYPE_INT_ARGB:
                name = "INT_ARGB"; break;
            case BufferedImage.TYPE_INT_ARGB_PRE:
                name = "ARGB_PRE"; break;
            case BufferedImage.TYPE_INT_BGR:
                name = "INT_BGR"; break;
            case BufferedImage.TYPE_INT_RGB:
                name = "INT_RGB"; break;
            case BufferedImage.TYPE_USHORT_555_RGB:
                name = "USHORT_555_RGB"; break;
            case BufferedImage.TYPE_USHORT_565_RGB:
                name = "USHORT_565_RGB"; break;
            case BufferedImage.TYPE_USHORT_GRAY:
                name = "USHORT_GRAY"; break;
            default:
                name = "unknown";
        }
        return name;
    }
    
    private static int findColorDepthIndex() {
        colorDepthIndex = orderedColorDepths.indexOf((Integer)colorDepth);
        return colorDepthIndex;
    }

    public static double getKeyframeTriggerThreshold() {
        return keyframeTriggerThreshold;
    }

    public static ScreenShareInfo setColorDepth(int d) {
        colorDepth = d;
        System.out.println("Color depth set to " + getColorDepthName() + "\n" + asString());
        return getInstance();
    }
    
    public static int increaseColorDepth() {
        int pos = findColorDepthIndex();
        if (pos >= -1 && pos < orderedColorDepths.size() - 1) {
            ++pos;
            setColorDepth(orderedColorDepths.get(pos).intValue());
        }
        return colorDepth;
    }
    
    public static int decreaseColorDepth() {
        int pos = findColorDepthIndex();
        if (pos > 0 && pos < orderedColorDepths.size()) {
            --pos;
            setColorDepth(orderedColorDepths.get(pos).intValue());
        }
        return colorDepth;
    }
    
    public static ScreenShareInfo setMinColorDepth() {
        setColorDepth(orderedColorDepths.get(0).intValue());
        return getInstance();
    }
    
    public static ScreenShareInfo setMaxColorDepth() {
        setColorDepth(orderedColorDepths.get(orderedColorDepths.size()-1).intValue());
        return getInstance();
    }
    
    public static ScreenShareInfo setKeyframeTriggerThreshold(double t) {
        keyframeTriggerThreshold = t;
        System.out.println("New keyframe threshold set to " + t + "\n" + asString());
        return getInstance();
    }
    
    public static String asString() {
        StringBuffer sb = new StringBuffer("Color depth index: " + findColorDepthIndex() + "\n" + 
            "Color depth name: " + getColorDepthName() + "\n" +
            "New keyframe threshold: " + getKeyframeTriggerThreshold() + "\n"
        );
        return sb.toString();
    }
}
