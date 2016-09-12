package com.marginallyclever.makelangeloRobot.imageFilters;

import java.awt.Color;

import com.marginallyclever.makelangeloRobot.TransformedImage;

/**
 * Filters modify a BufferedImage.
 * @author danroyer
 *
 */
public class ImageFilter {
	public static int decode32bit(int pixel) {
		double a = 255-((pixel>>24) & 0xff);
		int r = ((pixel >> 16) & 0xff);
		int g = ((pixel >> 8) & 0xff);
		int b = ((pixel) & 0xff);

		int r2 = (int)((255 - r) * (a / 255.0) + r);
		int g2 = (int)((255 - g) * (a / 255.0) + g);
		int b2 = (int)((255 - b) * (a / 255.0) + b);
		
		return (r2 + g2 + b2) / 3;
	}

	public static int decodeColor(Color c) {
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		double a = 255-c.getAlpha();

		int r2 = (int)((255 - r) * (a / 255.0) + r);
		int g2 = (int)((255 - g) * (a / 255.0) + g);
		int b2 = (int)((255 - b) * (a / 255.0) + b);
		
		return (r2 + g2 + b2) / 3;
	}

	public static int encode32bit(int i) {
		return (0xff << 24) | (i << 16) | (i << 8) | i;
	}
	
	/**
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	 * @return the altered image
	 */
	public TransformedImage filter(TransformedImage img) {
		return img;
	}
}
