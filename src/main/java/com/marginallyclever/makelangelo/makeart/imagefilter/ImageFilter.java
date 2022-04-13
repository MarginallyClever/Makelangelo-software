package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.*;

/**
 * Filters modify a BufferedImage.
 * @author danroyer
 *
 */
public class ImageFilter {
	public static int decode32bit(int pixel) {
		int r = ((pixel >> 16) & 0xff);
		int g = ((pixel >> 8) & 0xff);
		int b = ((pixel) & 0xff);
		double a = (double) ((pixel >> 24) & 0xff) / 255.0;

		return average(r, g, b, a);
	}

	public static int decodeColor(Color c) {
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		double a = (double)c.getAlpha() / 255.0;
		return average(r, g, b, a);
	}

	private static int average(int r, int g, int b, double a) {
		int r2 = (int)(r * a);
		int g2 = (int)(g * a);
		int b2 = (int)(b * a);

		return (r2 + g2 + b2) / 3;
	}

	public static int encode32bit(int i) {
		i &= 0xff;
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
