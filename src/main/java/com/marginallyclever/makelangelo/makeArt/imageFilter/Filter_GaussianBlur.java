package com.marginallyclever.makelangelo.makeArt.imageFilter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Converts an image to N shades of grey.
 *
 * @author Dan
 */
public class Filter_GaussianBlur extends ImageFilter {
	private static final Logger logger = LoggerFactory.getLogger(Filter_GaussianBlur.class);
	int radius = 1;


	public Filter_GaussianBlur(int _radius) {
		assert (radius > 0);
		radius = _radius;
	}


	public TransformedImage filter(TransformedImage img) {
		int h = img.getSourceImage().getHeight();
		int w = img.getSourceImage().getWidth();
		int x, y;

		BufferedImage dest = new BufferedImage(img.getSourceImage().getWidth(), img.getSourceImage().getHeight(), img.getSourceImage().getType());

	    TransformedImage after = new TransformedImage(img);
	    BufferedImage afterBI = after.getSourceImage();
	    
		// scales could be filled with a gaussian curve: float[] scales = new float[radius];
		float[] scales = new float[3];
		scales[0] = 1.0f / 4.0f;
		scales[1] = 1.0f / 2.0f;
		scales[2] = 1.0f / 4.0f;

		ColorRGB pixel = new ColorRGB(0, 0, 0);
		ColorRGB p;
		double sum;

		// horizontal blur
		for (y = 0; y < h; ++y) {
			for (x = 0; x < w; ++x) {
				pixel.set(0, 0, 0);
				sum = 0;
				if (x - 1 >= 0) {
					p = new ColorRGB(img.getSourceImage().getRGB(x - 1, y));
					p.mul(scales[0]);
					pixel.add(p);
					sum += scales[0];
				}

				p = new ColorRGB(img.getSourceImage().getRGB(x, y));
				p.mul(scales[1]);
				pixel.add(p);
				sum += scales[1];

				if (x + 1 < w) {
					p = new ColorRGB(img.getSourceImage().getRGB(x + 1, y));
					p.mul(scales[2]);
					pixel.add(p);
					sum += scales[2];
				}

				//pixel.mul(1.0/sum);
				//if(b==255) Log.message(x+"\t"+y+"\t"+i+"\t"+b);
				dest.setRGB(x, y, pixel.toInt());
			}
		}

		// vertical blur
		for (x = 0; x < w; ++x) {
			for (y = 0; y < h; ++y) {
				pixel.set(0, 0, 0);
				sum = 0;
				if (y - 1 >= 0) {
					p = new ColorRGB(dest.getRGB(x, y - 1));
					p.mul(scales[0]);
					pixel.add(p);
					sum += scales[0];
				}

				p = new ColorRGB(dest.getRGB(x, y));
				p.mul(scales[1]);
				pixel.add(p);
				sum += scales[1];

				if (y + 1 < h) {
					p = new ColorRGB(dest.getRGB(x, y + 1));
					p.mul(scales[2]);
					pixel.add(p);
					sum += scales[2];
				}

				pixel.mul(1.0 / sum);
				//if(b==255) Log.message(x+"\t"+y+"\t"+i+"\t"+b);
				afterBI.setRGB(x, y, pixel.toInt());
			}
		}

		try {
			// save image
			File outputfile = new File("saved.png");
			ImageIO.write(img.getSourceImage(), "png", outputfile);
		} catch (IOException e) {
			logger.error("Failed to save image", e);
		}

		return after;
	}
}

/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */
