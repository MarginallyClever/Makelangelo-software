package com.marginallyclever.converters;


import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.basictypes.TransformedImage;
import com.marginallyclever.filters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_Spiral extends ImageConverter {
	private static boolean convertToCorners = false;  // draw the spiral right out to the edges of the square bounds.

	@Override
	public String getName() {
		return Translator.get("SpiralName");
	}


	/**
	 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
	 *
	 * @param img the image to convert.
	 */
	@Override
	public boolean convert(TransformedImage img,Writer out) throws IOException {
		// black and white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);

		imageStart(out);
		liftPen(out);

		double toolDiameter = tool.getDiameter();

		int i, j;
		final int steps = 4;
		double leveladd = 255.0 / 5.0f;
		double level;
		int z = 0;

		float maxr;
		convertToCorners=false;
		if (convertToCorners) {
			// go right to the corners
			float h2 = (float)machine.getPaperHeight() * 10;
			float w2 = (float)machine.getPaperWidth() * 10;
			maxr = (float) (Math.sqrt(h2 * h2 + w2 * w2) + 1.0f);
		} else {
			// do the largest circle that still fits in the image.
			float w = (float)machine.getPaperWidth()/2.0f;
			float h = (float)machine.getPaperHeight()/2.0f;
			maxr = (float)( h < w ? h : w );
			maxr *= machine.getPaperMargin() * 10.0f;
		}
		
		float r = maxr, f;
		float fx, fy;
		int numRings = 0;
		double[] each_level = new double[steps];
		each_level[0] = leveladd * 1;
		each_level[1] = leveladd * 3;
		each_level[2] = leveladd * 2;
		each_level[3] = leveladd * 4;
		j = 0;
		while (r > toolDiameter) {
			++j;
			level = each_level[j % steps];
			// find circumference of current circle
			float circumference = (float) Math.floor((2.0f * r - toolDiameter) * Math.PI);
			if (circumference > 360.0f) circumference = 360.0f;

			for (i = 0; i <= circumference; ++i) {
				f = (float) Math.PI * 2.0f * (float)i / (float)circumference;
				fx = (float) (Math.cos(f) * r);
				fy = (float) (Math.sin(f) * r);
				// clip to paper boundaries
				if( isInsidePaperMargins(fx, fy) )
				{
					try {
						z = img.sample3x3(fx, fy);
					} catch(Exception e) {
						e.printStackTrace();
					}
					moveTo(out, fx, fy, (z >= level));
				} else {
					moveTo(out, fx, fy, true);
				}
			}
			r -= toolDiameter;
			++numRings;
		}

		Log.write("yellow", numRings + " rings.");

		liftPen(out);

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
