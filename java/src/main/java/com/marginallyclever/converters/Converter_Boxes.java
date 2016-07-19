package com.marginallyclever.converters;


import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.basictypes.TransformedImage;
import com.marginallyclever.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.Translator;


public class Converter_Boxes extends ImageConverter {
	@Override
	public String getName() {
		return Translator.get("BoxGeneratorName");
	}

	/**
	 * turn the image into a grid of boxes.  box size is affected by source image darkness.
	 * @param img the image to convert.
	 */
	public boolean convert(TransformedImage img,Writer out) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);

		imageStart(out);
		tool = machine.getCurrentTool();
		liftPen(out);
		tool.writeChangeTo(out);

		double yBottom = machine.getPaperBottom() * machine.getPaperMargin() * 10;
		double yTop    = machine.getPaperTop()    * machine.getPaperMargin() * 10;
		double xLeft   = machine.getPaperLeft()   * machine.getPaperMargin() * 10;
		double xRight  = machine.getPaperRight()  * machine.getPaperMargin() * 10;
		double pw = xRight - xLeft;
		
		// figure out how many lines we're going to have on this image.
		double d = tool.getDiameter();
		double fullStep = d*10.0f;
		double halfStep = fullStep / 2.0f;
		
		double steps = pw / fullStep;
		if (steps < 1) steps = 1;

		// from top to bottom of the image...
		double x, y, z;
		int i = 0;
		for (y = yBottom + halfStep; y < yTop - halfStep; y += fullStep) {
			++i;
			if ((i % 2) == 0) {
				// every even line move left to right
				for (x = xLeft; x < xRight; x += fullStep) {
					// read a block of the image and find the average intensity in this block
					z = img.sample( x, y - halfStep, x + fullStep, y + halfStep );
					// scale the intensity value
					double scaleZ =  (255.0f - z) / 255.0f;
					double pulseSize = (halfStep - 0.5f) * scaleZ;
					if (pulseSize > 0.5f) {
						double xmin = x + halfStep - pulseSize;
						double xmax = x + halfStep + pulseSize;
						double ymin = y + halfStep - pulseSize;
						double ymax = y + halfStep + pulseSize;
						// Draw a square.  the diameter is relative to the intensity.
						moveTo(out, xmin, ymin, true);
						lowerPen(out);
						moveTo(out, xmax, ymin, false);
						moveTo(out, xmax, ymax, false);
						moveTo(out, xmin, ymax, false);
						moveTo(out, xmin, ymin, false);
						// fill in the square
						boolean flip = false;
						for(double yy=ymin;yy<ymax;yy+=d) {
							moveTo(out,flip?xmin:xmax,yy,false);
							flip = !flip;
						}
						liftPen(out);
					}
				}
			} else {
				// every odd line move right to left
				for (x = xRight; x > xLeft; x -= fullStep) {
					// read a block of the image and find the average intensity in this block
					z = img.sample(x - fullStep, y - halfStep, x, y + halfStep );
					// scale the intensity value
					double scaleZ = (255.0f - z) / 255.0f;
					double pulseSize = (halfStep - 0.5f) * scaleZ;
					if (pulseSize > 0.1f) {
						double xmin = x - halfStep - pulseSize;
						double xmax = x - halfStep + pulseSize;
						double ymin = y + halfStep - pulseSize;
						double ymax = y + halfStep + pulseSize;
						// draw a square.  the diameter is relative to the intensity.
						moveTo(out, xmin, ymin, true);
						lowerPen(out);
						moveTo(out, xmax, ymin, false);
						moveTo(out, xmax, ymax, false);
						moveTo(out, xmin, ymax, false);
						moveTo(out, xmin, ymin, false);
						// fill in the square
						boolean flip = false;
						for(double yy=ymin;yy<ymax;yy+=d) {
							moveTo(out,flip?xmin:xmax,yy,false);
							flip = !flip;
						}
						liftPen(out);
					}
				}
			}

			liftPen(out);
		    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
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
