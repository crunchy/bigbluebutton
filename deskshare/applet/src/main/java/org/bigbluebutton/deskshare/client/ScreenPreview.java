package org.bigbluebutton.deskshare.client;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenPreview extends JFrame implements ScreenCaptureListener {

    JLabel label;

    public ScreenPreview(int width, int height) {
	super("ScreenPreview");

	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	Dimension size = new Dimension(width, height);

	label = new JLabel();

	label.setPreferredSize(size);
	getContentPane().add(label);

	pack();
	setVisible(true);
    }

    @Override
    public void onScreenCaptured(BufferedImage screen) {
	label.setIcon(new ImageIcon(screen));
	label.update(label.getGraphics());
    }
}
