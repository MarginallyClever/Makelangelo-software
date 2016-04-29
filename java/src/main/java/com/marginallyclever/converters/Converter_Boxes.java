package com.marginallyclever.converters;


import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.basictypes.TransformedImage;
import com.marginallyclever.filters.Filter_BlackAndWhite;
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
		liftPen(out);

		float yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin() * 10;
		float yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin() * 10;
		float xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin() * 10;
		float xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin() * 10;
		float pw = xRight - xLeft;
		
		// figure out how many lines we're going to have on this image.
		float blockSize = tool.getDiameter()*10.0f;
		float halfStep = blockSize / 2.0f;
		
		float steps = pw / blockSize;
		if (steps < 1) steps = 1;

		// from top to bottom of the image...
		float x, y, z;
		int i = 0;
		for (y = yBottom + halfStep; y < yTop - halfStep; y += blockSize) {
			++i;
			if ((i % 2) == 0) {
				// every even line move left to right
				for (x = xLeft; x < xRight; x += blockSize) {
					// read a block of the image and find the average intensity in this block
					z = img.sample( x, y - halfStep, x + blockSize, y + halfStep );
					// scale the intensity value
					float scaleZ =  (255.0f - z) / 255.0f;
					float pulseSize = (halfStep - 1.0f) * scaleZ;
					if (pulseSize > 0.1f) {
						// draw a square.  the diameter is relative to the intensity.
						moveTo(out, x + halfStep - pulseSize, y + halfStep - pulseSize, true);
						lowerPen(out);
						moveTo(out, x + halfStep + pulseSize, y + halfStep - pulseSize, false);
						moveTo(out, x + halfStep + pulseSize, y + halfStep + pulseSize, false);
						moveTo(out, x + halfStep - pulseSize, y + halfStep + pulseSize, false);
						moveTo(out, x + halfStep - pulseSize, y + halfStep - pulseSize, false);
						liftPen(out);
					}
				}
			} else {
				// every odd line move right to left
				for (x = xRight; x > xLeft; x -= blockSize) {
					// read a block of the image and find the average intensity in this block
					z = img.sample(x - blockSize, y - halfStep, x, y + halfStep );
					// scale the intensity value
					float scaleZ = 1;//(255.0f - z) / 255.0f;
					float pulseSize = (halfStep - 1.0f) * scaleZ;
					if (pulseSize > 0.1f) {
						// draw a square.  the diameter is relative to the intensity.
						moveTo(out, x - halfStep - pulseSize, y + halfStep - pulseSize, true);
						lowerPen(out);
						moveTo(out, x - halfStep + pulseSize, y + halfStep - pulseSize, false);
						moveTo(out, x - halfStep + pulseSize, y + halfStep + pulseSize, false);
						moveTo(out, x - halfStep - pulseSize, y + halfStep + pulseSize, false);
						moveTo(out, x - halfStep - pulseSize, y + halfStep - pulseSize, false);
						liftPen(out);
					}
				}
			}

			liftPen(out);

			tool.writeMoveTo(out, 0, 0);
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
