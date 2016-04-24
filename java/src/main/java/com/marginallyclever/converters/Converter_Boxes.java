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

		// Set up the conversion from image space to paper space, select the current tool, etc.
		imageStart(out);
		// "please change to tool X and press any key to continue"
		tool.writeChangeTo(out);
		// Make sure the pen is up for the first move
		liftPen(out);

		float pw = (float)machine.getPaperWidth();
		float ph = (float)machine.getPaperHeight();

		// figure out how many lines we're going to have on this image.
		float steps = (float) (pw / tool.getDiameter());
		if (steps < 1) steps = 1;

		float blockSize = (int) (pw / steps);
		float halfstep = (float) blockSize / 2.0f;

		// from top to bottom of the image...
		float x, y, z;
		int i = 0;
		for (y = 0; y < ph; y += blockSize) {
			++i;
			if ((i % 2) == 0) {
				// every even line move left to right
				//lineTo(file,x,y,pen up?)]
				for (x = 0; x < pw - blockSize; x += blockSize) {
					// read a block of the image and find the average intensity in this block
					z = img.sample( x, y - halfstep, x + blockSize, y + halfstep );
					// scale the intensity value
					float scale_z = (255.0f - (float) z) / 255.0f;
					float pulse_size = (halfstep - 1.0f) * scale_z;
					if (pulse_size > 0.1f) {
						// draw a square.  the diameter is relative to the intensity.
						lineTo(out, x + halfstep - pulse_size, y + halfstep - pulse_size, true);
						lineTo(out, x + halfstep + pulse_size, y + halfstep - pulse_size, false);
						lineTo(out, x + halfstep + pulse_size, y + halfstep + pulse_size, false);
						lineTo(out, x + halfstep - pulse_size, y + halfstep + pulse_size, false);
						lineTo(out, x + halfstep - pulse_size, y + halfstep - pulse_size, false);
						lineTo(out, x + halfstep - pulse_size, y + halfstep - pulse_size, true);
					}
				}
			} else {
				// every odd line move right to left
				//lineTo(file,x,y,pen up?)]
				for (x = pw - blockSize; x >= 0; x -= blockSize) {
					// read a block of the image and find the average intensity in this block
					z = img.sample(x - blockSize, y - halfstep, x, y + halfstep );
					// scale the intensity value
					float scale_z = (255.0f - (float) z) / 255.0f;
					float pulse_size = (halfstep - 1.0f) * scale_z;
					if (pulse_size > 0.1f) {
						// draw a square.  the diameter is relative to the intensity.
						lineTo(out, x - halfstep - pulse_size, y + halfstep - pulse_size, true);
						lineTo(out, x - halfstep + pulse_size, y + halfstep - pulse_size, false);
						lineTo(out, x - halfstep + pulse_size, y + halfstep + pulse_size, false);
						lineTo(out, x - halfstep - pulse_size, y + halfstep + pulse_size, false);
						lineTo(out, x - halfstep - pulse_size, y + halfstep - pulse_size, false);
						lineTo(out, x - halfstep - pulse_size, y + halfstep - pulse_size, true);
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
