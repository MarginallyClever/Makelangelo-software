package com.marginallyclever.converters;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.filters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Translator;


public class Converter_Scanline extends ImageConverter {
	public Converter_Scanline(MakelangeloRobotSettings mc) {
		super(mc);
	}

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

		// figure out how many lines we're going to have on this image.
		int steps = (int) Math.ceil(tool.getDiameter() / (1.75 * scale));
		if (steps < 1) steps = 1;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).
		double level = 255.0 / 2.0;

		// from top to bottom of the image...
		int x, y, z, i = 0;
		for (y = 0; y < imageHeight; y += steps) {
			++i;
			if ((i % 2) == 0) {
				// every even line move left to right

				//MoveTo(file,x,y,pen up?)
				moveTo(out, (float) 0, (float) y, true);
				for (x = 0; x < imageWidth; ++x) {
					// read the image at x,y
					z = sample3x3(img, x, y);
					moveTo(out, (float) x, (float) y, (z > level));
				}
				moveTo(out, (float) imageWidth, (float) y, true);
			} else {
				// every odd line move right to left
				moveTo(out, (float) imageWidth, (float) y, true);
				for (x = imageWidth - 1; x >= 0; --x) {
					z = sample3x3(img, x, y);
					moveTo(out, (float) x, (float) y, (z > level));
				}
				moveTo(out, (float) 0, (float) y, true);
			}
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
