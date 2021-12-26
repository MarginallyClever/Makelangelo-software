package com.marginallyclever.makelangelo.makeArt.imageFilter;

import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

/**
 * Converts an image to N shades of grey.
 *
 * @author Dan
 */
public class Filter_BlackAndWhite extends ImageFilter {
	private static final Logger logger = LoggerFactory.getLogger(Filter_BlackAndWhite.class);
	double levels = 2;

	public Filter_BlackAndWhite(int _levels) {
		levels = (double) _levels;
	}

	public TransformedImage filter(TransformedImage img) {
		int mode = 1;
		switch (mode) {
		case 0:	return filterLevels(img);
		case 1:	return filterTone(img);
		case 2:	return filterSimple(img);
		}
		return null;
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

		// logger.debug("min_intensity="+min_intensity);
		// logger.debug("max_intensity="+max_intensity);
		// logger.debug("levels="+levels);
		// logger.debug("inverse="+ilevels);

		double pixel;

		TransformedImage after = new TransformedImage(img);
		BufferedImage afterBI = after.getSourceImage();

		for (y = 0; y < h; ++y) {
			for (x = 0; x < w; ++x) {
				pixel = decode32bit(bi.getRGB(x, y));

				double a = (pixel - min_intensity) / intensity_range;
				double c = a * levels * ilevels;
				int b = (int) Math.max(Math.min(c * 255.0, 255), 0);
				// if(b==255) logger.debug(x+"\t"+y+"\t"+i+"\t"+b);
				afterBI.setRGB(x, y, ImageFilter.encode32bit(b));
			}
		}

		return after;
	}

	// accepts and returns a number between 0 and 255, inclusive.
	double toneControl(double v) {
		v /= 255.0;
		v = 0.017 * Math.exp(3.29 * v) + 0.005 * Math.exp(7.27 * v);
		return Math.min(1, Math.max(0, v)) * 255.0;
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

	/**
	 * An experimental black &#38; white converter that doesn't just greyscale to 4
	 * levels, it also tries to divide by histogram frequency. Didn't look good so I
	 * left it for the lulz.
	 *
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is to
	 *            process.
	 * @return the altered image
	 */
	@Deprecated
	public TransformedImage processViaHistogram(TransformedImage img) {
		int h = img.getSourceImage().getHeight();
		int w = img.getSourceImage().getWidth();

		int x, y, i;

		double[] histogram = new double[256];

		for (i = 0; i < 256; ++i) {
			histogram[i] = 0;
		}

		for (y = 0; y < h; ++y) {
			for (x = 0; x < w; ++x) {
				i = decode32bit(img.getSourceImage().getRGB(x, y));
				++histogram[i];
			}
		}

		double histogram_area = 0;
		// logger.debug("histogram:");
		for (i = 1; i < 255; ++i) {
			logger.debug("{}={}", i, histogram[i]);
			histogram_area += histogram[i];
		}
		double histogram_zone = histogram_area / (double) levels;
		// logger.debug("histogram area: "+histogram_area);
		// logger.debug("histogram zone: "+histogram_zone);

		double histogram_sum = 0;
		x = 0;
		y = 0;
		for (i = 1; i < 255; ++i) {
			histogram_sum += histogram[i];
			// logger.debug("mapping "+i+" to "+x);
			if (histogram_sum > histogram_zone) {
				// logger.debug("level up at "+i+" "+histogram_sum+" vs "+histogram_zone);
				histogram_sum -= histogram_zone;
				x += (int) (256.0 / (double) levels);
				++y;
			}
			histogram[i] = x;
		}

		// logger.debug("y="+y+" x="+x);
		int pixel, b;

		for (y = 0; y < h; ++y) {
			for (x = 0; x < w; ++x) {
				pixel = decode32bit(img.getSourceImage().getRGB(x, y));
				b = (int) histogram[pixel];
				img.getSourceImage().setRGB(x, y, ImageFilter.encode32bit(b));
			}
		}

		return img;
	}
}

/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Makelangelo. If not, see <http://www.gnu.org/licenses/>.
 */
