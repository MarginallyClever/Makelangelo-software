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
		double a = 255.0 - (double)((pixel>>24) & 0xff) / 255.0;
		int r = ((pixel >> 16) & 0xff);
		int g = ((pixel >> 8) & 0xff);
		int b = ((pixel) & 0xff);

		int r2 = (int)( (255.0 - r) * a + r);
		int g2 = (int)( (255.0 - g) * a + g);
		int b2 = (int)( (255.0 - b) * a + b);
		
		return (r2 + g2 + b2) / 3;
	}

	public static int decodeColor(Color c) {
		double r = c.getRed();
		double g = c.getGreen();
		double b = c.getBlue();
		double a = 1.0 - (double)c.getAlpha() / 255.0;

		int r2 = (int)( (255.0 - r) * a + r);
		int g2 = (int)( (255.0 - g) * a + g);
		int b2 = (int)( (255.0 - b) * a + b);
		
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
