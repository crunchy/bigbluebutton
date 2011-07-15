package org.bigbluebutton.deskshare.client.image_filters;

import java.awt.image.BufferedImage;

public interface SimpleFilter {
    public BufferedImage filter(BufferedImage srcImage);
}
