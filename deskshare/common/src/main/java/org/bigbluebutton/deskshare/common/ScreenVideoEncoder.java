/** 
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
**/
package org.bigbluebutton.deskshare.common;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.zip.Deflater;

public final class ScreenVideoEncoder {

	private static byte FLV_KEYFRAME = 0x10;
	private static byte FLV_INTERFRAME = 0x20;
	private static byte SCREEN_VIDEO_CODEC_ID = 0x03;
	
	public static int[] getPixelsFromImage(BufferedImage image) {
		return ((DataBufferInt)(image).getRaster().getDataBuffer()).getData();
	}
	
	public static byte encodeFlvVideoDataHeader(boolean isKeyFrame) {
		if (isKeyFrame) return (byte) (FLV_KEYFRAME + SCREEN_VIDEO_CODEC_ID);
		return (byte) (FLV_INTERFRAME + SCREEN_VIDEO_CODEC_ID);
	}
	
	public static byte[] encodeBlockAndScreenDimensions(int blockWidth, int imageWidth, int blockHeight, int imageHeight) {
		byte[] dims = new byte[4];
		
		int bw = (((blockWidth/16) - 1) & 0xf) << 12;
		int iw = (imageWidth & 0xfff);
		int ew = (bw | iw);
		
		int bh = (((blockHeight/16) - 1) & 0xf) << 12;
		int ih = (imageHeight & 0xfff);
		int eh = (bh | ih);
		
		dims[0] = (byte) ((ew & 0xff00) >> 8);
		dims[1] = (byte) (ew & 0xff);
		dims[2] = (byte) ((eh & 0xff00) >> 8);
		dims[3] = (byte) (eh & 0xff);
		
		return dims;
	}

    public static byte[] encodePixels(int pixels[], int width, int height) {
		changePixelScanFromBottomLeftToTopRight(pixels, width, height);
		
		byte[] bgrPixels = convertFromRGBtoBGR(pixels);
		byte[] compressedPixels = compressUsingZlib(bgrPixels, Deflater.DEFLATED);
		byte[] encodedDataLength = ScreenVideoEncoder.encodeCompressedPixelsDataLength(compressedPixels.length);
		byte[] encodedData = new byte[encodedDataLength.length + compressedPixels.length];

		System.arraycopy(encodedDataLength, 0, encodedData, 0, encodedDataLength.length);
		System.arraycopy(compressedPixels, 0, encodedData, encodedDataLength.length, compressedPixels.length);

		return encodedData;
	}
	
	private static byte[] encodeCompressedPixelsDataLength(int length) {
		int byte1 =  ((length & 0xFF00) >> 8);
		int byte2 =  (length & 0x0FFF);
//		System.out.println("Block size = " + length + " hex=" + Integer.toHexString(length) + " bytes= " + Integer.toHexString(byte1) + " " + Integer.toHexString(byte2));
		return new byte[] {(byte)byte1 , (byte) (byte2 &0xFFF) };
	}
	
	public static byte[] encodeBlockUnchanged() {
		return new byte[] { (byte) 0, (byte) 0 };
	}
	
	/**
	 * Screen capture pixels are arranged top-left to bottom-right. ScreenVideo encoding
	 * expects pixels are arranged bottom-left to top-right.
	 * @param pixels - contains pixels of the image
	 * @param width - width of the image
	 * @param height - height of the image
	 */
	private static void changePixelScanFromBottomLeftToTopRight(int[] pixels, int width, int height) {
		int[] swap = new int[pixels.length];
		
		long start = System.currentTimeMillis();
		for (int i = 0; i < height; i++) {
			int sourcePos = i * width;
			int destPos = (height - (i+1)) * width;
			System.arraycopy(pixels, sourcePos, swap, destPos, width);
		}
		
		System.arraycopy(swap, 0, pixels, 0, pixels.length);
		long end = System.currentTimeMillis();
//		System.out.println("Scanning pixels[" + pixels.length + "] took " + (end-start) + " ms.");
	}
	
	
	/**
	 * Compress the byte array using Zlib. 
	 * @param pixels
	 * @param compressionLevel one of Deflater compression levels. 
	 * Passed as a parameter instead of object variable so we can adjust it dynamically later on
	 * @return a byte array of compressed data
	 */
	private static byte[] compressUsingZlib(byte[] pixels, int compressionLevel) {
		// Create the compressed stream
		byte[] output = new byte[pixels.length];
		Deflater compressor = new Deflater(compressionLevel);
		compressor.setInput(pixels);
		compressor.finish();
		int compressedDataLength = compressor.deflate(output);
		// this truncates output down to its actual size by copying
		// it was declared w/ uncompressed length, and filled w/ compressed data
		byte[] zData = new byte[compressedDataLength] ;
		System.arraycopy(output, 0, zData, 0, compressedDataLength);
		return zData;
	}
	
	/**
	 * Extracts the RGB bytes from a pixel represented by a 4-byte integer (ARGB).
	 * @param pixels
	 * @return pixels in BGR order
	 */
	private static byte[] convertFromRGBtoBGR(int[] pixels) {	
		long start = System.currentTimeMillis();
		byte[] rgbPixels = new byte[pixels.length * 3];
		int position = 0;
		
		for (int i = 0; i < pixels.length; i++) {
			byte red = (byte) ((pixels[i] >> 16) & 0xff);
			byte green = (byte) ((pixels[i] >> 8) & 0xff);
			byte blue = (byte) (pixels[i] & 0xff);

			// Sequence should be BGR
			rgbPixels[position++] = blue;
			rgbPixels[position++] = green;
			rgbPixels[position++] = red;
/*
 * If we want to send grayscale images.
			byte brightness = convertToGrayScale(red, green, blue);
			
			// Sequence should be BGR
			rgbPixels[position++] = brightness;
			rgbPixels[position++] = brightness;
			rgbPixels[position++] = brightness;
*/		}
		
		long end = System.currentTimeMillis();
//		System.out.println("Extracting pixels[" + pixels.length + "] took " + (end-start) + " ms.");				
		return rgbPixels;
	}
			
	private static byte convertToGrayScale(int r, int g, int b) {
		return (byte)(0.212671 * r + 0.715160 * g + 0.072169 * b);
	}
	
	public static String toStringBits(int value) {
	    int displayMask = 1 << 31;
	    StringBuffer buf = new StringBuffer(35);
			   
	    for ( int c = 1; c <= 32; c++ ) 
	    {
	        buf.append( ( value & displayMask ) == 0 ? '0' : '1' );
	        value <<= 1;

		if ( c % 8 == 0 )
			buf.append( ' ' );
	    }
	    return buf.toString();
	}
		    
	public static String toStringBits(byte value) {
		int displayMask = 1 << 7;
		StringBuffer buf = new StringBuffer(8);

		for ( int c = 1; c <= 8; c++ ) {
			buf.append((value & displayMask) == 0 ? '0' : '1');
			value <<= 1;
			if ( c % 8 == 0 )
				buf.append( ' ' );
		}

		return buf.toString();
	}
}
