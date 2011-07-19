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
package org.bigbluebutton.deskshare.client.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.bigbluebutton.deskshare.client.DeskShareAppletSettings;
import org.bigbluebutton.deskshare.client.ScreenShareInfo;

public class CaptureRegionFrame {
    private static final long serialVersionUID = 1L;

    private Button btnStartStop;
    //private JComboBox colorDepthSelect, keyframeThresholdSelect;
    private JLabel windowSizeLabel;
    /*
    private final String[] colorDepthNames = { 
        "TYPE_BYTE_BINARY",
        "TYPE_BYTE_GRAY",
        "TYPE_USHORT_555_RGB (default)",
        "TYPE_USHORT_565_RGB",
        "TYPE_4BYTE_ABGR_PRE",
        "TYPE_BYTE_INDEXED",
        "TYPE_USHORT_GRAY",
        "TYPE_3BYTE_BGR",
        "TYPE_INT_BGR",
        "TYPE_INT_RGB",
        "TYPE_4BYTE_ABGR",
        "TYPE_INT_ARGB_PRE",
        "TYPE_INT_ARGB"
    };
    private final int colorDepthSelectedIndex = 2;
    private final int[] colorDepthValues = {
        BufferedImage.TYPE_BYTE_BINARY,
        BufferedImage.TYPE_BYTE_GRAY,
        BufferedImage.TYPE_USHORT_555_RGB,
        BufferedImage.TYPE_USHORT_565_RGB,
        BufferedImage.TYPE_4BYTE_ABGR_PRE,
        BufferedImage.TYPE_BYTE_INDEXED,
        BufferedImage.TYPE_USHORT_GRAY,
        BufferedImage.TYPE_3BYTE_BGR,
        BufferedImage.TYPE_INT_BGR,
        BufferedImage.TYPE_INT_RGB,
        BufferedImage.TYPE_4BYTE_ABGR,
        BufferedImage.TYPE_INT_ARGB_PRE,
        BufferedImage.TYPE_INT_ARGB
    };
    private final String[] keyframeThresholdNames = {
        "10%",
        "20%",
        "30%",
        "40% (default)",
        "50%",
        "60%",
        "70%",
        "80%",
        "90%"
    };
    private final int keyframeThresholdSelectedIndex = 3;
    */
    private JComboBox presetSelect;
    private final String[] presetNames = { 
        "Low keyframe, low color (default)",
        "Low keyframe, high color",
        "High keyframe, low color",
        "High keyframe, high color"
    };
    private final int presetNamesSelectedIndex = 0;
    private final DeskShareAppletSettings.Preset[] presetNamesValues = {
        DeskShareAppletSettings.Preset.LOW_KEYFRAME_LOW_COLOR,
        DeskShareAppletSettings.Preset.LOW_KEYFRAME_HIGH_COLOR,
        DeskShareAppletSettings.Preset.HIGH_KEYFRAME_LOW_COLOR,
        DeskShareAppletSettings.Preset.HIGH_KEYFRAME_HIGH_COLOR
    };

    private CaptureRegionListener client;
    private boolean capturing = false;
    private WindowlessFrame frame;
    
    public CaptureRegionFrame(CaptureRegionListener client, int borderWidth) {
        frame = new WindowlessFrame(borderWidth);
        this.client = client;
        frame.setCaptureRegionListener(client);
        frame.setToolbar(createToolbar());
    }
    
    public void setHeight(int h) {
        frame.setHeight(h);
    }
    
    public void setWidth(int w) {
        frame.setWidth(w);
    }
    
    public void setLocation(int x, int y) {
        frame.setLocation(x, y);
    }
    
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
    
    private JPanel createToolbar() {
        final JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        capturing = false;
        btnStartStop = new Button("Start Sharing");
        btnStartStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*
                if (capturing) {
                    capturing = false;
                    btnStartStop.setLabel("Start Capture");
                    stopCapture();
                } else {
                    capturing = true;
                    btnStartStop.setLabel("Stop Capture");
                    startCapture();
                }*/
                startCapture();
            }
        });
        panel.add(btnStartStop);
        
        // pulldown menus
        /*
        colorDepthSelect = new JComboBox(colorDepthNames);
        colorDepthSelect.setSelectedIndex(colorDepthSelectedIndex);
        colorDepthSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ScreenShareInfo.setColorDepth(colorDepthValues[colorDepthSelect.getSelectedIndex()]);
            }
        });
        panel.add(colorDepthSelect);
        
        keyframeThresholdSelect = new JComboBox(keyframeThresholdNames);
        keyframeThresholdSelect.setSelectedIndex(keyframeThresholdSelectedIndex);
        keyframeThresholdSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double newThreshold = (double)(keyframeThresholdSelect.getSelectedIndex() + 1) * .1;
                ScreenShareInfo.setKeyframeTriggerThreshold(newThreshold);
            }
        });
        panel.add(keyframeThresholdSelect);
*/        
        presetSelect = new JComboBox(presetNames);
        presetSelect.setSelectedIndex(presetNamesSelectedIndex);
        presetSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ScreenShareInfo.setSettings(new DeskShareAppletSettings(presetNamesValues[presetSelect.getSelectedIndex()]));
            }
        });
        panel.add(presetSelect);
        windowSizeLabel = new JLabel("Window size will go here");
        panel.add(windowSizeLabel); 
        
        return panel;
    }
    
    public void updateWindowSizeLabel() {
        Rectangle rect = frame.getFramedRectangle();
        windowSizeLabel.setText((int)rect.getWidth() + "x" + (int)rect.getHeight());
    }
    
    private void startCapture() {
        updateWindowSizeLabel();
        frame.changeBorderToBlue();
        frame.removeResizeListeners();
        Rectangle rect = frame.getFramedRectangle();
        client.onStartCapture(rect.x, rect.y, frame.getWidth(), frame.getHeight());
    }
    
    private void stopCapture() {
        frame.changeBorderToRed();
        client.onStopCapture();
    }
}
