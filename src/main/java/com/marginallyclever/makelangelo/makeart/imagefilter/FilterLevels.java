package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Converts an image to N levels.
 * @author Dan Royer
 */
public class FilterLevels extends ImageFilter {
	private static final Logger logger = LoggerFactory.getLogger(FilterLevels.class);
	private final TransformedImage img;
	private final double levels;
	private final int mode = 1;

	public FilterLevels(TransformedImage img, int levels) {
		super();
		this.img = img;
		this.levels = levels;
	}

	@Override
	public TransformedImage filter() {
		return switch (mode) {
			case 0 -> filterLevels(img);
			case 1 -> filterTone(img);
			case 2 -> filterSimple(img);
			default -> null;
		};
	}

	protected TransformedImage filterLevels(TransformedImage img) {
		int h = img.getSourceImage().getHeight();
		int w = img.getSourceImage().getWidth();
		int x, y, i;

		double max_intensity = -1000;
		double min_intensity = 1000;

		BufferedImage bi = img.getSourceImage();
		for (y = 0; y < h; ++y) {
			for (x = 0; x < w; ++x) {
				i = decode32bit(bi.getRGB(x, y));
				if (max_intensity < i) max_intensity = i;
				if (min_intensity > i) min_intensity = i;
			}
		}
		double intensity_range = max_intensity - min_intensity;

		double ilevels = 1;
		if (levels != 0)
			ilevels = 1.0 / levels;

		double pixel;

		TransformedImage after = new TransformedImage(img);
		BufferedImage afterBI = after.getSourceImage();

		for (y = 0; y < h; ++y) {
			for (x = 0; x < w; ++x) {
				pixel = decode32bit(bi.getRGB(x, y));
				double a = (pixel - min_intensity) / intensity_range;
				double c = a * levels * ilevels;
				int b = (int) Math.max(Math.min(c * 255.0, 255), 0);
				afterBI.setRGB(x, y, ImageFilter.encode32bit(b));
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

	public TransformedImage filterTone(TransformedImage img) {
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

	public TransformedImage filterSimple(TransformedImage img) {
		int h = img.getSourceImage().getHeight();
		int w = img.getSourceImage().getWidth();

		BufferedImage bi = img.getSourceImage();
		TransformedImage after = new TransformedImage(img);
		BufferedImage afterBI = after.getSourceImage();

		int x, y;
		for (y = 0; y < h; ++y) {
			for (x = 0; x < w; ++x) {
				double pixel = decode32bit(bi.getRGB(x, y));
				int rgb = (int) Math.min(255, Math.max(0, pixel));
				afterBI.setRGB(x, y, ImageFilter.encode32bit(rgb));
			}
		}
		return after;
	}

	public static void main(String[] args) throws IOException {
		TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")) );
		FilterLevels f = new FilterLevels(src,255);
		ResizableImagePanel.showImage(f.filter().getSourceImage(), "Filter_Greyscale" );
	}
}