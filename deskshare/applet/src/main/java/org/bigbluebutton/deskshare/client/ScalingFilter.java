package org.bigbluebutton.deskshare.client;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScalingFilter implements SimpleFilter {
    private int targetWidth;
    private int targetHeight;
    private Object hint;
    private boolean multiStep;

    public ScalingFilter(int targetWidth, int targetHeight,
			 boolean multiStep) {
	this.targetWidth = targetWidth;
	this.targetHeight = targetHeight;

	// TODO make configurable
	this.multiStep = multiStep;
	this.hint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
    }


    @Override
    public BufferedImage filter(BufferedImage srcImage) {
	int w, h, dstType;
	dstType = srcImage.getType();

	if (multiStep) {
	    // Use multi-step technique: start with original size, then
	    // scale down in multiple passes with drawImage()
	    // until the target size is reached
	    w = srcImage.getWidth();
	    h = srcImage.getHeight();
	} else {
	    // Use one-step technique: scale directly from original
	    // size to target size with a single drawImage() call
	    w = targetWidth;
	    h = targetHeight;
	}

	do {
	    if (multiStep && w > targetWidth) {
		w /= 2;
		if (w < targetWidth) {
		    w = targetWidth;
		}
	    }

	    if (multiStep && h > targetHeight) {
		h /= 2;
		if (h < targetHeight) {
		    h = targetHeight;
		}
	    }

	    BufferedImage tmpImage = new BufferedImage(w, h, dstType);
	    Graphics2D g2 = tmpImage.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
	    g2.drawImage(srcImage, 0, 0, w, h, null);
	    g2.dispose();
	    srcImage = tmpImage;
	} while (w != targetWidth || h != targetHeight);

	return srcImage;
    }

}
