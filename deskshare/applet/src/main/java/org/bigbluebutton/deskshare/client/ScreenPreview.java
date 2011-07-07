package org.bigbluebutton.deskshare.client;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenPreview extends JFrame implements ScreenCaptureListener {

    JLabel label;
    Rectangle screenRect;

    int scaleX, scaleY;

    public ScreenPreview() {
        super("ScreenPreview");

        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize().width,
                                   Toolkit.getDefaultToolkit().getScreenSize().height);

        scaleX = Toolkit.getDefaultToolkit().getScreenSize().width;
        scaleY = Toolkit.getDefaultToolkit().getScreenSize().height;

        label = new JLabel();
        label.setPreferredSize(new Dimension(scaleX, scaleY));
        getContentPane().add( label );

        pack();
        setVisible(true);
    }

    @Override
    public void onScreenCaptured(BufferedImage screen) {
      label.setIcon(new ImageIcon(screen));
      label.update(label.getGraphics());
    }
}
