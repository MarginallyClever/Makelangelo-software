package com.marginallyclever.converters;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.filters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Translator;


public class Converter_Boxes extends ImageConverter {
	public Converter_Boxes(MakelangeloRobotSettings mc) {
		super(mc);
	}

	@Override
	public String getName() {
		return Translator.get("BoxGeneratorName");
	}

	/**
	 * Overrides MoveTo() because optimizing for zigzag is different logic than straight lines.
	 */
	@Override
	protected void moveTo(Writer out, float x, float y, boolean up) throws IOException {
		if (lastUp != up) {
			if (up) liftPen(out);
			else lowerPen(out);
			lastUp = up;
		}
		tool.writeMoveTo(out, TX(x), TY(y));
	}

	// sample the pixels from x0,y0 (top left) to x1,y1 (bottom right)
	protected int takeImageSampleBlock(BufferedImage img, int x0, int y0, int x1, int y1) {
		// point sampling
		int value = 0;
		int sum = 0;

		if (x0 < 0) x0 = 0;
		if (x1 > imageWidth - 1) x1 = imageWidth - 1;
		if (y0 < 0) y0 = 0;
		if (y1 > imageHeight - 1) y1 = imageHeight - 1;

		for (int y = y0; y < y1; ++y) {
			for (int x = x0; x < x1; ++x) {
				value += sample1x1(img, x, y);
				++sum;
			}
		}

		if (sum == 0) return 255;

		return value / sum;
	}

	/**
	 * turn the image into a grid of boxes.  box size is affected by source image darkness.
	 * @param img the image to convert.
	 */
	public boolean convert(BufferedImage img,Writer out) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);

		// Set up the conversion from image space to paper space, select the current tool, etc.
		imageStart(img, out);
		// "please change to tool X and press any key to continue"
		tool.writeChangeTo(out);
		// Make sure the pen is up for the first move
		liftPen(out);

		double pw = machine.getPaperWidth();
		//double ph = machine.GetPaperHeight();

		// figure out how many lines we're going to have on this image.
		float steps = (float) (pw / tool.getDiameter());
		if (steps < 1) steps = 1;

		float blockSize = (int) (imageWidth / steps);
		float halfstep = (float) blockSize / 2.0f;

		// from top to bottom of the image...
		float x, y, z;
		int i = 0;
		for (y = 0; y < imageHeight; y += blockSize) {
			++i;
			if ((i % 2) == 0) {
				// every even line move left to right
				//MoveTo(file,x,y,pen up?)]
				for (x = 0; x < imageWidth - blockSize; x += blockSize) {
					// read a block of the image and find the average intensity in this block
					z = takeImageSampleBlock(img, (int) x, (int) (y - halfstep), (int) (x + blockSize), (int) (y + halfstep));
					// scale the intensity value
					float scale_z = (255.0f - (float) z) / 255.0f;
					float pulse_size = (halfstep - 1.0f) * scale_z;
					if (pulse_size > 0.1f) {
						// draw a square.  the diameter is relative to the intensity.
						moveTo(out, x + halfstep - pulse_size, y + halfstep - pulse_size, true);
						moveTo(out, x + halfstep + pulse_size, y + halfstep - pulse_size, false);
						moveTo(out, x + halfstep + pulse_size, y + halfstep + pulse_size, false);
						moveTo(out, x + halfstep - pulse_size, y + halfstep + pulse_size, false);
						moveTo(out, x + halfstep - pulse_size, y + halfstep - pulse_size, false);
						moveTo(out, x + halfstep - pulse_size, y + halfstep - pulse_size, true);
					}
				}
			} else {
				// every odd line move right to left
				//MoveTo(file,x,y,pen up?)]
				for (x = imageWidth - blockSize; x >= 0; x -= blockSize) {
					// read a block of the image and find the average intensity in this block
					z = takeImageSampleBlock(img, (int) (x - blockSize), (int) (y - halfstep), (int) x, (int) (y + halfstep));
					// scale the intensity value
					float scale_z = (255.0f - (float) z) / 255.0f;
					float pulse_size = (halfstep - 1.0f) * scale_z;
					if (pulse_size > 0.1f) {
						// draw a square.  the diameter is relative to the intensity.
						moveTo(out, x - halfstep - pulse_size, y + halfstep - pulse_size, true);
						moveTo(out, x - halfstep + pulse_size, y + halfstep - pulse_size, false);
						moveTo(out, x - halfstep + pulse_size, y + halfstep + pulse_size, false);
						moveTo(out, x - halfstep - pulse_size, y + halfstep + pulse_size, false);
						moveTo(out, x - halfstep - pulse_size, y + halfstep - pulse_size, false);
						moveTo(out, x - halfstep - pulse_size, y + halfstep - pulse_size, true);
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
 * This file is part of DrawbotGUI.
 * <p>
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */
