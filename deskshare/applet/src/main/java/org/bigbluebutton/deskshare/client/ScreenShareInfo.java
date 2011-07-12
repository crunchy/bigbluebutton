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
    private static float keyframeTriggerThreshold = 0.4f;
    private static int colorDepth = BufferedImage.TYPE_USHORT_555_RGB;

    private ScreenShareInfo() {}
        
    public static ScreenShareInfo getInstance() 
    {
        if (instance == null) {
            instance = new ScreenShareInfo();
        }
        return instance;
    }
    
    public static int getColorDepth()
    {
        return colorDepth;
    }
    
    public static String getColorDepthName()
    {
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

    public static float getKeyframeTriggerThreshold()
    {
        return keyframeTriggerThreshold;
    }

    public static ScreenShareInfo setColorDepth(int d)
    {
        colorDepth = d;
        return getInstance();
    }

    public static ScreenShareInfo setKeyframeTriggerThreshold(float t)
    {
        keyframeTriggerThreshold = t;
        return getInstance();
    }
}
