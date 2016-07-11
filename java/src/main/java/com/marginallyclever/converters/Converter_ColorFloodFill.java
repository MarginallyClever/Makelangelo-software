package com.marginallyclever.converters;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

import com.marginallyclever.basictypes.C3;
import com.marginallyclever.basictypes.ColorPalette;
import com.marginallyclever.basictypes.TransformedImage;
import com.marginallyclever.imageFilters.Filter_GaussianBlur;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;

/**
 * @author danroyer
 * @since at least 7.1.4
 */
public class Converter_ColorFloodFill extends ImageConverter {
	private ColorPalette palette;
	private float diameter;
	private float lastX, lastY;
	private TransformedImage imgChanged;
	private TransformedImage imgMask;

	float yBottom , yTop, xLeft, xRight;


	public Converter_ColorFloodFill() {
		palette = new ColorPalette();
		palette.addColor(new C3(  0,   0,   0));
		palette.addColor(new C3(255,   0,   0));
		palette.addColor(new C3(  0, 255,   0));
		palette.addColor(new C3(  0,   0, 255));
		palette.addColor(new C3(255, 255, 255));
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
		
		int c = (new C3(255, 255, 255)).toInt();
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
	protected C3 takeImageSampleBlock(float x2, float y2, float f, float g) {
		// point sampling
		C3 value = new C3(0, 0, 0);
		int sum = 0;

		for (float y = y2; y < g; ++y) {
			for (float x = x2; x < f; ++x) {
				if(isInsidePaperMargins(x, y) && imgChanged.canSampleAt(x, y)) {
					value.add(new C3(imgChanged.sample1x1(x, y)));
					++sum;
				}
			}
		}

		if (sum == 0) return new C3(255, 255, 255);

		return value.mul(1.0f / sum);
	}


	protected boolean doesQuantizedBlockMatch(int color_index, float x, float y) {
		C3 original_color = takeImageSampleBlock((int) x, (int) y, (int) (x + diameter), (int) (y + diameter));
		int quantized_color = palette.quantizeIndex(original_color);
		return (quantized_color == color_index);
	}


	/**
	 * queue-based flood fill
	 *
	 * @param colorIndex
	 * @throws IOException
	 */
	protected void floodFillBlob(int colorIndex, float x, float y, Writer osw) throws IOException {
		LinkedList<Point> pointsToVisit = new LinkedList<>();
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
				//System.out.print("Jump at "+x+", "+y+"\n");
				moveTo(osw, lastX, lastY, true);
				moveTo(osw, a.x, a.y, true);
				moveTo(osw, a.x, a.y, false);
			} else {
				//System.out.print("Move to "+x+", "+y+"\n");
				moveTo(osw, a.x, a.y, false);
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
	 * @throws IOException
	 */
	void scanForContiguousBlocks(int colorIndex, Writer osw) throws IOException {
		C3 originalColor;
		int quantized_color;

		float x, y;
		int z = 0;

		Log.write("orange", "Palette color " + palette.getColor(colorIndex).toString() );

		for (y = yBottom; y < yTop; y += diameter) {
			for (x = xLeft; x < xRight; x += diameter) {
				if (getMaskTouched(x, y)) continue;

				originalColor = takeImageSampleBlock(x, y, x + diameter, y + diameter);
				quantized_color = palette.quantizeIndex(originalColor);
				if (quantized_color == colorIndex) {
					// found blob
					floodFillBlob(colorIndex, x, y, osw);
					z++;
					//if(z==20)
					//            return;
				}
			}
		}
		System.out.println("Found " + z + " blobs.");
	}

	private void scanColor(int i, Writer osw) throws IOException {
		// "please change to tool X and press any key to continue"
		tool = machine.getTool(i);
		liftPen(osw);
		tool.writeChangeTo(osw);
		// Make sure the pen is up for the first move

		Log.write("green", "Color " + i );

		scanForContiguousBlocks(i, osw);
	}

	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 *
	 * @param img the image to convert.
	 */
	public boolean convert(TransformedImage img,Writer out) throws IOException {
		Filter_GaussianBlur blur = new Filter_GaussianBlur(1);
		img = blur.filter(img);
		//    Histogram h = new Histogram();
		//    h.getHistogramOf(img);


		// create a color mask so we don't repeat any pixels
		BufferedImage bi = new BufferedImage(img.getSourceImage().getWidth(), img.getSourceImage().getHeight(), BufferedImage.TYPE_INT_RGB);
		imgMask = new TransformedImage(bi);
		imgMask.copySettingsFrom(imgChanged);
		Graphics2D g = bi.createGraphics();
		g.setPaint(new Color(0, 0, 0));
		g.fillRect(0, 0, bi.getWidth(), bi.getHeight());

		imageStart(out);
		
		yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin() * 10;
		yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin() * 10;
		xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin() * 10;
		xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin() * 10;
		
		diameter = (int)( tool.getDiameter() * 10.0f );

		imgChanged = img;

		lastX = 0;
		lastY = 0;

		scanColor(0, out);  // black
		scanColor(1, out);  // red
		scanColor(2, out);  // green
		scanColor(3, out);  // blue

		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
		return true;
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
