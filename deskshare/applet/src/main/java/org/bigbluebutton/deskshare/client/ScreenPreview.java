package org.bigbluebutton.deskshare.client;

import org.bigbluebutton.deskshare.client.image_filters.ScalingFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenPreview implements ScreenCaptureListener {

    Container contentPane;
    JLabel label;
    private ScalingFilter filter;

    public ScreenPreview(Container pane) {
	contentPane = pane;
	setupLabel(contentPane.getSize());
    }

    private void setupLabel(Dimension size) {
	label = new JLabel();

	filter = new ScalingFilter(size.width, size.height, true);

	label.setPreferredSize(size);
	contentPane.add(label);
    }

    @Override
    public void onScreenCaptured(BufferedImage screen) {
	BufferedImage scaled = filter.filter(screen);

	label.setIcon(new ImageIcon(scaled));
	label.update(label.getGraphics());
    }
}
