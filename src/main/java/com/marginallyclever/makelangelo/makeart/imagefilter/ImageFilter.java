package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.*;

/**
 * Filters modify a {@link TransformedImage}.
 * @author dan Royer
 */
public abstract class ImageFilter {
	protected static int red32(int color) {
		return ((color >> 16) & 0xff);
	}

	protected static int green32(int color) {
		return ((color >> 8) & 0xff);
	}

	protected static int blue32(int color) {
		return ((color) & 0xff);
	}

	protected static int alpha32(int color) {
		return ((color >> 24) & 0xff);
	}

	/**
	 * @param color RGBA
	 * @return grayscale value
	 */
	public static int decode32bit(int color) {
		int r = red32(color);
		int g = green32(color);
		int b = blue32(color);
		int a = alpha32(color);

		return average(r, g, b, a / 255.0);
	}

	/**
	 * @param red 0-255
	 * @param green 0-255
	 * @param blue 0-255
	 * @param alpha 0-255
	 * @return RGB color
	 */
	public static int encode32bit(int red,int green,int blue,int alpha) {
		red &= 0xff;
		green &= 0xff;
		blue &= 0xff;
		alpha &= 0xff;
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	/**
	 * @param greyscale 0-255
	 * @return RGB fully opaque
	 */
	public static int encode32bit(int greyscale) {
		greyscale &= 0xff;
		return encode32bit(greyscale,greyscale,greyscale,0xff);
	}

	/**
	 * @param color RGBA
	 * @return grayscale value
	 */
	protected static int decodeColor(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();
		return average(r, g, b, a / 255.0);
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
	 * Apply this filter and return the result as an image.
	 * @return the altered image
	 */
	abstract public TransformedImage filter();
}
