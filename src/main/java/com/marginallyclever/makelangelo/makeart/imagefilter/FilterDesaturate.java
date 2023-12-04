package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Converts an image to greyscale.
 * @author Dan Royer
 */
public class FilterDesaturate extends ImageFilter {
	private final TransformedImage img;

	public FilterDesaturate(TransformedImage img) {
		super();
		this.img = img;
	}

	@Override
	public TransformedImage filter() {
		int h = img.getSourceImage().getHeight();
		int w = img.getSourceImage().getWidth();

		BufferedImage bi = img.getSourceImage();
		TransformedImage after = new TransformedImage(img);
		BufferedImage afterBI = after.getSourceImage();

		int x, y;
		for (y = 0; y < h; ++y) {
			for (x = 0; x < w; ++x) {
				double pixel = decode32bit(bi.getRGB(x, y));
				//double v2 = sRGBtoLinear(pixel);
				double v2 = toneControl(pixel);
				int rgb = (int) Math.min(255, Math.max(0, v2));
				afterBI.setRGB(x, y, ImageFilter.encode32bit(rgb));
			}
		}
		return after;
	}

	private double sRGBtoLinear(double b) {
		b /= 255.0;
		if (b <= 0.04045) b /= 12.92;
		else b = Math.pow((b + 0.055) / 1.055, 2.4);
		return b * 255.0;
	}

	/**
	 * accepts and returns a number between 0 and 255, inclusive.
 	 */
	private double toneControl(double b) {
		b /= 255.0;
		b = 0.017 * Math.exp(3.29 * b) + 0.005 * Math.exp(7.27 * b);
		return Math.min(1, Math.max(0, b)) * 255.0;
	}

	public static void main(String[] args) throws IOException {
		TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")) );
		FilterDesaturate f = new FilterDesaturate(src);
		ResizableImagePanel.showImage(f.filter().getSourceImage(), "FilterDesaturate" );
	}
}