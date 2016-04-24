package com.marginallyclever.converters;


import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.basictypes.TransformedImage;
import com.marginallyclever.filters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.Translator;


public class Converter_Scanline extends ImageConverter {
	@Override
	public String getName() {
		return Translator.get("ScanlineName");
	}

	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 *
	 * @param img the image to convert.
	 */
	@Override
	public boolean convert(TransformedImage img,Writer out) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);


		// Set up the conversion from image space to paper space, select the current tool, etc.
		imageStart(out);
		// "please change to tool X and press any key to continue"
		tool.writeChangeTo(out);
		// Make sure the pen is up for the first move
		liftPen(out);

		// figure out how many lines we're going to have on this image.
		float steps = tool.getDiameter();
		if (steps < 1) steps = 1;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).
		double level = 255.0 / 2.0;

		// from top to bottom of the margin area...
		float yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin() * 10;
		float yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin() * 10;
		float xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin() * 10;
		float xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin() * 10;
		
		liftPen(out);/*
		moveTo(out,xLeft,yTop,true);
		lowerPen(out);
		moveTo(out,xRight,yTop,false);
		moveTo(out,xRight,yBottom,false);
		moveTo(out,xLeft,yBottom,false);
		moveTo(out,xLeft,yTop,false);
		liftPen(out);
		moveTo(out,0,0,true);*/
		
		
		float x, y, z;
		int i = 0;
		for (y = yBottom; y < yTop; y += steps) {
			++i;
			if ((i % 2) == 0) {
				// every even line move left to right

				//lineTo(file,x,y,pen up?)
				lineTo(out, (float) 0, (float) y, true);
				for (x = xLeft; x < xRight; ++x) {
					// read the image at x,y
					z = img.sample3x3(x, y);
					lineTo(out, x, y, (z > level));
				}
				lineTo(out, xRight, y, true);
			} else {
				// every odd line move right to left
				lineTo(out, xRight, y, true);
				for (x = xRight; x >= xLeft; --x) {
					z = img.sample3x3(x, y);
					lineTo(out, x, y, (z > level));
				}
				lineTo(out, xLeft, y, true);
			}
		}

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
