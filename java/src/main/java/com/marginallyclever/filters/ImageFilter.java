package com.marginallyclever.filters;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Modifies a BufferedImage
 * @author danroyer
 *
 */
public class ImageFilter {
	public static int decode(int pixel) {
		int r = ((pixel >> 16) & 0xff);
		int g = ((pixel >> 8) & 0xff);
		int b = ((pixel) & 0xff);
		return (r + g + b) / 3;
	}

	public static int decode(Color c) {
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		return (r + g + b) / 3;
	}

	public static int encode(int i) {
		return (0xff << 24) | (i << 16) | (i << 8) | i;
	}
	
	/**
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	 * @return the altered image
	 */
	public BufferedImage filter(BufferedImage img) {
		return img;
	}
}
