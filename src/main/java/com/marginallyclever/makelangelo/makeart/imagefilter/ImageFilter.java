package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.*;

/**
 * Filters modify a {@link TransformedImage}.
 * @author dan Royer
 */
public class ImageFilter {
	/**
	 * @param color RGBA
	 * @return grayscale value
	 */
	public static int decode32bit(int color) {
		int r = ((color >> 16) & 0xff);
		int g = ((color >> 8) & 0xff);
		int b = ((color) & 0xff);
		double a = (double) ((color >> 24) & 0xff) / 255.0;

		return average(r, g, b, a);
	}

	/**
	 * @param greyscale 0-255
	 * @return RGB fully opaque
	 */
	public static int encode32bit(int greyscale) {
		greyscale &= 0xff;
		return (0xff << 24) | (greyscale << 16) | (greyscale << 8) | greyscale;
	}

	/**
	 * @param color RGBA
	 * @return grayscale value
	 */
	public static int decodeColor(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		double a = (double)color.getAlpha() / 255.0;
		return average(r, g, b, a);
	}

	/**
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @param a alpha
	 * @return grayscale value
	 */
	private static int average(int r, int g, int b, double a) {
		int r2 = (int)(r * a);
		int g2 = (int)(g * a);
		int b2 = (int)(b * a);

		return (r2 + g2 + b2) / 3;
	}
	
	/**
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	 * @return the altered image
	 */
	public TransformedImage filter(TransformedImage img) {
		return img;
	}
}
