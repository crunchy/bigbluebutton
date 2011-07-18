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

import java.awt.image.BufferedImage;

public class DeskShareAppletSettings {
       
    public static enum Preset {
        LOW_KEYFRAME_LOW_COLOR, 
        LOW_KEYFRAME_HIGH_COLOR, 
        HIGH_KEYFRAME_LOW_COLOR, 
        HIGH_KEYFRAME_HIGH_COLOR 
    }
    
    public static final double LOW_KEYFRAME     = 0.4;
    public static final double HIGH_KEYFRAME    = 0.7;
    
    public static final int LOW_COLOR   = BufferedImage.TYPE_USHORT_555_RGB;
    public static final int HIGH_COLOR  = BufferedImage.TYPE_INT_ARGB_PRE;
    
    private double keyframe;
    private int color;

    public DeskShareAppletSettings(Preset preset) {
        set(preset);
    }
        
    private void set(Preset preset) {
        switch(preset) {
            case LOW_KEYFRAME_HIGH_COLOR:
                keyframe        = LOW_KEYFRAME;
                color           = HIGH_COLOR;
                break;
            case HIGH_KEYFRAME_LOW_COLOR:
                keyframe        = HIGH_KEYFRAME;
                color           = LOW_COLOR;
                break;
            case HIGH_KEYFRAME_HIGH_COLOR:
                keyframe        = HIGH_KEYFRAME;
                color           = HIGH_COLOR;
                break;
            case LOW_KEYFRAME_LOW_COLOR:
            default:
                keyframe        = LOW_KEYFRAME;
                color           = LOW_COLOR;
                break;
        }
    }

    public double getKeyframe() {
        return keyframe;
    }
    
    public int getColor() {
        return color;
    }
}