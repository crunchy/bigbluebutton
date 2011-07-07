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

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Transparency;
import java.awt.image.BufferedImage;


/**
 * The Capture class uses the java Robot class to capture the screen.
 * @author Snap
 *
 */
public class ScreenCapture {	
	private Robot robot;
	private Rectangle screenBounds;	
	private int scaleWidth, scaleHeight, x,y, captureWidth, captureHeight;
	private boolean quality;
	private GraphicsConfiguration graphicsConfig;
        private static final int DEFAULT_TYPE = BufferedImage.TYPE_USHORT_555_RGB;	
	// try sharing the image objects instead of creating them every time
	// roger 2011-06-20
	private BufferedImage mainImage, copyImage, tmpImage;
	
	public ScreenCapture(int x, int y, int captureWidth, int captureHeight, int scaleWidth, int scaleHeight, boolean quality) {
		this.captureWidth = captureWidth;
		this.captureHeight = captureHeight;
		
		try{
			robot = new Robot();
		}catch (AWTException e){
			System.out.println(e.getMessage());
		}

		this.screenBounds = new Rectangle(x, y, this.captureWidth, this.captureHeight);
		this.scaleWidth = scaleWidth;
		this.scaleHeight = scaleHeight;
		this.quality = quality;
		GraphicsEnvironment je = GraphicsEnvironment.getLocalGraphicsEnvironment();
	        GraphicsDevice js = je.getDefaultScreenDevice();
                graphicsConfig = js.getDefaultConfiguration();
	}
        
        public BufferedImage takeSingleSnapshot() {
		mainImage = robot.createScreenCapture(this.screenBounds);
		
                // copy image to lower quality as as performance test
		tmpImage = new BufferedImage(mainImage.getWidth(), mainImage.getHeight(), DEFAULT_TYPE);
                Graphics2D g = tmpImage.createGraphics();
                g.drawRenderedImage(mainImage, null);
                g.dispose();
                mainImage = tmpImage;
		// end copy                                    
		
		if (needToScaleImage()) {
			if (quality) {
				return useQuality();
			}
			return getScaledInstance(scaleWidth, scaleHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
		} else {
			return mainImage;
		}
	}
	
	public void setX(int x) {
		this.x = x;
		updateBounds();
	}
	
	public void setY(int y) {
		this.y = y;
		updateBounds();
	}
	
	public void setWidth(int width) {
		this.captureWidth = width;
		updateBounds();
	}
	
	public void setHeight(int height) {
		this.captureHeight = height;
		updateBounds();
	}
	
	public void updateBounds() {
		this.screenBounds = new Rectangle(x, y, captureWidth, captureHeight);
	}
	
	private boolean needToScaleImage() {
		return (captureWidth != scaleWidth && captureHeight != scaleHeight);
	}

	private BufferedImage useQuality() {	    
		tmpImage = graphicsConfig.createCompatibleImage(scaleWidth, scaleHeight, mainImage.getType());
		tmpImage.setAccelerationPriority(1);
	    
		Graphics2D g2 = tmpImage.createGraphics();
		Image scaledImage = mainImage.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_AREA_AVERAGING);
		g2.drawImage(scaledImage, 0, 0, scaleWidth, scaleHeight, null);
		g2.dispose();
		//return resultImage;
		return tmpImage;
	}
		 
	/**
	 * See http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
	 * 
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * removed @param img the original image to be scaled, using shared object var instead
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
	public BufferedImage getScaledInstance(	int targetWidth,
						int targetHeight,
						Object hint,
						boolean higherQuality) {
		copyImage = mainImage;
		int w, h;
		if (higherQuality) {
		    // Use multi-step technique: start with original size, then
		    // scale down in multiple passes with drawImage()
		    // until the target size is reached
		    w = mainImage.getWidth();
		    h = mainImage.getHeight();
		} else {
		    // Use one-step technique: scale directly from original
		    // size to target size with a single drawImage() call
		    w = targetWidth;
		    h = targetHeight;
		}
        
		do {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
				    w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
				    h = targetHeight;
				}
			}

			tmpImage = new BufferedImage(w, h, DEFAULT_TYPE);
			Graphics2D g2 = tmpImage.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g2.drawImage(copyImage, 0, 0, w, h, null);
			g2.dispose();
			copyImage = tmpImage;
		} while (w != targetWidth || h != targetHeight);
        
		return copyImage;
	}
}
