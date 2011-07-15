package org.bigbluebutton.deskshare.client.image_filters;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ChangeTypeFilter implements SimpleFilter {
    private int type;

    public ChangeTypeFilter(int type) {
	this.type = type;
    }

    @Override
    public BufferedImage filter(BufferedImage mainImage) {
	BufferedImage tmpImage = new BufferedImage(mainImage.getWidth(),
	    mainImage.getHeight(), type);
	Graphics2D g = tmpImage.createGraphics();
	g.drawRenderedImage(mainImage, null);
	g.dispose();

	return tmpImage;
    }

}
