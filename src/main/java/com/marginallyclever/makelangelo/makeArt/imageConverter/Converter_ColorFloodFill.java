package com.marginallyclever.makelangelo.makeArt.imageConverter;


import com.marginallyclever.convenience.ColorPalette;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import com.marginallyclever.makelangelo.makeArt.imageFilter.Filter_GaussianBlur;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.LinkedList;

/**
 * @author Dan Royer
 * @since 7.1.4
 */
@Deprecated
public class Converter_ColorFloodFill extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_ColorFloodFill.class);
	private ColorPalette palette;
	private float diameter;
	private float lastX, lastY;
	private TransformedImage imgChanged;
	private TransformedImage imgMask;

	protected double yBottom , yTop, xLeft, xRight;


	public Converter_ColorFloodFill() {
		palette = new ColorPalette();
		palette.addColor(new ColorRGB(  0,   0,   0));
		palette.addColor(new ColorRGB(255,   0,   0));
		palette.addColor(new ColorRGB(  0, 255,   0));
		palette.addColor(new ColorRGB(  0,   0, 255));
		palette.addColor(new ColorRGB(255, 255, 255));
	}

	@Override
	public String getName() {
		return Translator.get("RGBFloodFillName");
	}


	/**
	 * test the mask from x0,y0 (top left) to x1,y1 (bottom right) to see if this region has already been visited
	 *
	 * @param x0 left
	 * @param y0 top
	 * @return true if all the pixels in this region are zero.
	 */
	protected boolean getMaskTouched(float x0, float y0) {
		float x1 = x0 + diameter;
		float y1 = y0 + diameter;

		Color value;
		int sum = 0;
		for (float y = y0; y < y1; ++y) {
			for (float x = x0; x < x1; ++x) {
				if(imgMask.canSampleAt(x, y)) {
					++sum;
					value = new Color(imgMask.sample1x1Unchecked(x, y));
					if (value.getRed() != 0) {
						return true;
					}
				}
			}
		}

		return (sum == 0);
	}

	protected void setMaskTouched(float x0, float y0, float x1, float y1) {
		
		int c = (new ColorRGB(255, 255, 255)).toInt();
		for (float y = y0; y < y1; ++y) {
			for (float x = x0; x < x1; ++x) {
				if(imgMask.canSampleAt(x, y)) {
					imgMask.getSourceImage().setRGB(imgMask.getTransformedX(x), imgMask.getTransformedY(y), c);
				}
			}
		}
		//imgMask.flush();
	}


	/**
	 * sample the pixels from x0,y0 (top left) to x1,y1 (bottom right) and average the color.
	 *
	 * @param x2
	 * @param y2
	 * @param f
	 * @param g
	 * @return the average color in the region.  if nothing is sampled, return white.
	 */
	protected ColorRGB takeImageSampleBlock(float x2, float y2, float f, float g) {
		// point sampling
		ColorRGB value = new ColorRGB(0, 0, 0);
		int sum = 0;

		for (float y = y2; y < g; ++y) {
			for (float x = x2; x < f; ++x) {
				if(isInsidePaperMargins(x, y) && imgChanged.canSampleAt(x, y)) {
					value.add(new ColorRGB(imgChanged.sample1x1(x, y)));
					++sum;
				}
			}
		}

		if (sum == 0) return new ColorRGB(255, 255, 255);

		return value.mul(1.0f / sum);
	}


	protected boolean doesQuantizedBlockMatch(int color_index, float x, float y) {
		ColorRGB original_color = takeImageSampleBlock((int) x, (int) y, (int) (x + diameter), (int) (y + diameter));
		int quantized_color = palette.quantizeIndex(original_color);
		return (quantized_color == color_index);
	}


	/**
	 * queue-based flood fill
	 *
	 * @param colorIndex
	 */
	protected void floodFillBlob(int colorIndex, float x, float y) {
		LinkedList<Point> pointsToVisit = new LinkedList<Point>();
		pointsToVisit.add(new Point((int)x, (int)y));

		Point a;

		while (!pointsToVisit.isEmpty()) {
			a = pointsToVisit.removeLast();

			if (getMaskTouched(a.x,a.y)) continue;
			if (!doesQuantizedBlockMatch(colorIndex, a.x, a.y)) continue;
			// mark this spot as visited.
			setMaskTouched(a.x, a.y, (int)(a.x + diameter), (int)(a.y + diameter));

			// if the difference between the last filled pixel and this one is more than diameter*2, pen up, move, pen down.
			float dx = a.x - lastX;
			float dy = a.y - lastY;
			if ((dx * dx + dy * dy) > diameter * diameter * 2.0f) {
				//logger.debug("Jump at "+x+", "+y+"\n");
				turtle.jumpTo(a.getX(),a.getY());
			} else {
				//logger.debug("Move to "+x+", "+y+"\n");
				turtle.moveTo(a.x, a.y);
			}
			// update the last position.
			lastX = a.x;
			lastY = a.y;

			//      if( !getMaskTouched((int)(a.x + diameter), (int)a.y            ))
			pointsToVisit.add(new Point((int)(a.x + diameter), a.y                 ));
			//      if( !getMaskTouched((int)(a.x - diameter), (int)a.y            ))
			pointsToVisit.add(new Point((int)(a.x - diameter), a.y                 ));
			//      if( !getMaskTouched((int)a.x             , (int)(a.y + diameter)))
			pointsToVisit.add(new Point(     a.x             , (int)(a.y + diameter)));
			//      if( !getMaskTouched((int)a.x             , (int)(a.y - diameter)))
			pointsToVisit.add(new Point(     a.x             , (int)(a.y - diameter)));
		}
	}


	/**
	 * find blobs of color in the original image.  Send that to the flood fill system.
	 *
	 * @param colorIndex index into the list of colors at the top of the class
	 */
	private void scanColor(int colorIndex) {
		// "please change to tool X and press any key to continue"
		turtle.penUp();
		turtle.setColor(palette.getColor(colorIndex));
		
		ColorRGB originalColor;
		int quantized_color;

		float x, y;
		int z = 0;

		logger.debug("Palette color {}", palette.getColor(colorIndex).toString());

		for (y = (int)yBottom; y < yTop; y += diameter) {
			for (x = (int)xLeft; x < xRight; x += diameter) {
				if (getMaskTouched(x, y)) continue;

				originalColor = takeImageSampleBlock(x, y, x + diameter, y + diameter);
				quantized_color = palette.quantizeIndex(originalColor);
				if (quantized_color == colorIndex) {
					// found blob
					floodFillBlob(colorIndex, x, y);
					z++;
					//if(z==20)
					//            return;
				}
			}
		}
		logger.debug("Found {} blobs.", z);
	}

	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 *
	 * {@link #myImage} the image to convert.
	 */

	@Override
	public void finish() {
		Filter_GaussianBlur blur = new Filter_GaussianBlur(1);
		TransformedImage img = blur.filter(myImage);
		//    Histogram h = new Histogram();
		//    h.getHistogramOf(img);

		turtle=new Turtle();
		
		// create a color mask so we don't repeat any pixels
		BufferedImage bi = new BufferedImage(img.getSourceImage().getWidth(), img.getSourceImage().getHeight(), BufferedImage.TYPE_INT_RGB);
		imgMask = new TransformedImage(bi);
		imgMask.copySettingsFrom(imgChanged);
		Graphics2D g = bi.createGraphics();
		g.setPaint(new Color(0, 0, 0));
		g.fillRect(0, 0, bi.getWidth(), bi.getHeight());

		yBottom = myPaper.getMarginBottom();
		yTop    = myPaper.getMarginTop();
		xLeft   = myPaper.getMarginLeft();
		xRight  = myPaper.getMarginRight();
		
		diameter = 1;

		imgChanged = img;

		lastX = 0;
		lastY = 0;

		scanColor(0);  // black
		scanColor(1);  // red
		scanColor(2);  // green
		scanColor(3);  // blue

		turtle.penUp();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {}
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
