package com.marginallyclever.artPipeline.deprecated;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;

import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodes.ImageConverter;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.TransformedImage;
import com.marginallyclever.convenience.imageFilters.Filter_GaussianBlur;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * @Deprecated because it will not work accurately with TransformedImages. 
 * @author Dan Royer
 * @since 7.1.4
 */
@Deprecated
public class Converter_ColorFloodFill extends ImageConverter {
	private float diameter;
	private float lastX, lastY;
	private float colorEpsilon = 64;
	private TransformedImage imgChanged;
	private TransformedImage imgMask;

	protected double yBottom , yTop, xLeft, xRight;


	public Converter_ColorFloodFill() {}

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
	protected ColorRGB takeImageSampleBlock(Turtle turtle,float x2, float y2, float f, float g) {
		// point sampling
		ColorRGB value = new ColorRGB(0, 0, 0);
		int sum = 0;

		for (float y = y2; y < g; ++y) {
			for (float x = x2; x < f; ++x) {
				if(imgChanged.canSampleAt(x, y)) {
					value.add(new ColorRGB(imgChanged.sample1x1(x, y)));
					++sum;
				}
			}
		}

		if (sum == 0) return new ColorRGB(255, 255, 255);

		return value.mul(1.0f / sum);
	}


	protected boolean doesColorMatch(Turtle turtle,float x, float y) {
		ColorRGB originalColor = takeImageSampleBlock(turtle,(int) x, (int) y, (int) (x + diameter), (int) (y + diameter));
		double d = originalColor.diff(turtle.getColor());
		return (d < colorEpsilon);
	}


	/**
	 * queue-based flood fill
	 *
	 * @param colorIndex
	 */
	protected void floodFillBlob(Turtle turtle,float x, float y) {
		LinkedList<Point> pointsToVisit = new LinkedList<>();
		pointsToVisit.add(new Point((int)x, (int)y));

		Point a;

		while (!pointsToVisit.isEmpty()) {
			a = pointsToVisit.removeLast();

			if (getMaskTouched(a.x,a.y)) continue;
			if (!doesColorMatch(turtle,a.x, a.y)) continue;
			// mark this spot as visited.
			setMaskTouched(a.x, a.y, (int)(a.x + diameter), (int)(a.y + diameter));

			// if the difference between the last filled pixel and this one is more than diameter*2, pen up, move, pen down.
			float dx = a.x - lastX;
			float dy = a.y - lastY;
			if ((dx * dx + dy * dy) > diameter * diameter * 2.0f) {
				//Log.message("Jump at "+x+", "+y+"\n");
				turtle.jumpTo(a.getX(),a.getY());
			} else {
				//Log.message("Move to "+x+", "+y+"\n");
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
	void scanForContiguousBlocks(Turtle turtle) {
		int z = 0;

		Log.message("Palette color " + turtle.getColor().toString() );

		for(float y = (int)yBottom; y < yTop; y += diameter) {
			for(float x = (int)xLeft; x < xRight; x += diameter) {
				if (getMaskTouched(x, y)) continue;

				ColorRGB originalColor = takeImageSampleBlock(turtle,x, y, x + diameter, y + diameter);
				double d = originalColor.diff(turtle.getColor());
				if (d < colorEpsilon) {
					// found blob
					floodFillBlob(turtle, x, y);
					z++;
					//if(z==20)
					//            return;
				}
			}
		}
		Log.message("Found " + z + " blobs.");
	}

	private void scanColor(Turtle turtle) {
		turtle.penUp();
		scanForContiguousBlocks(turtle);
	}

	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 *
	 * @param img the image to convert.
	 */
	public ArrayList<Turtle> finish() {
		Turtle turtle = new Turtle();
		Filter_GaussianBlur blur = new Filter_GaussianBlur(1);
		TransformedImage img = blur.filter(sourceImage.getValue());
		//    Histogram h = new Histogram();
		//    h.getHistogramOf(img);

		// create a color mask so we don't repeat any pixels
		BufferedImage bi = new BufferedImage(img.getSourceImage().getWidth(), img.getSourceImage().getHeight(), BufferedImage.TYPE_INT_RGB);
		imgMask = new TransformedImage(bi);
		imgMask.copySettingsFrom(imgChanged);
		Graphics2D g = bi.createGraphics();
		g.setPaint(new Color(0, 0, 0));
		g.fillRect(0, 0, bi.getWidth(), bi.getHeight());

		yBottom = 100;
		yTop    = 100;
		xLeft   = -100;
		xRight  = -100;
		
		diameter = (int)( 2.0 );

		imgChanged = img;

		lastX = 0;
		lastY = 0;

		scanColor(turtle);

		turtle.penUp();
		
		ArrayList<Turtle> list = new ArrayList<Turtle>();
		list.add(turtle);
		return list;
	}

	@Override
	public NodePanel getPanel() {
		// TODO Auto-generated method stub
		return null;
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
