package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.*;

/**
 * Filters modify a {@link TransformedImage}.
 * @author dan Royer
 */
public abstract class ImageFilter {
	/**
	 * @param color RGBA
	 * @return grayscale value
	 */
	public static int decode32bit(int color) {
		Color c = new Color(color);
		return (c.getRed() + c.getGreen() + c.getBlue()) / 3;
	}

	/**
	 * @param greyscale 0-255
	 * @return RGB fully opaque
	 */
	public static int encode32bit(int greyscale) {
		greyscale &= 0xff;
		Color c = new Color(greyscale,greyscale,greyscale);
		return c.getRGB();
	}

	/**
	 * Apply this filter and return the result as an image.
	 * @return the altered image
	 */
	abstract public TransformedImage filter();
}
