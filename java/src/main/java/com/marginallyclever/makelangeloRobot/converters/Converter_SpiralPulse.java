package com.marginallyclever.makelangeloRobot.converters;


import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_BlackAndWhite;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_SpiralPulse extends ImageConverter {
	private static boolean convertToCorners = false;  // draw the spiral right out to the edges of the square bounds.

	@Override
	public String getName() {
		return Translator.get("SpiralPulseName");
	}

	@Override
	public String getPreviewImage() {
		return "/images/converters/spiralPulse.JPG";
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
		machine.writeChangeTo(out);

		double toolDiameter = machine.getDiameter();

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
		
		float r = maxr-(float)toolDiameter*5.0f, f;
		float fx, fy;
		int numRings = 0;
		float stepSize = machine.getDiameter() * 4;
		float halfStep = stepSize / 2.0f;
		float zigZagSpacing = machine.getDiameter();
		int n=1;
		float PULSE_MINIMUM = 0.1f;
		float ringSize = halfStep*2.5f;
		float r2,scale_z,pulse_size,nx,ny;
		
		while (r > toolDiameter) {
			++j;
			// find circumference of current circle
			//if (circumference > 360.0f) circumference = 360.0f;
			
			for (i = 0; i <= circumference; ++i) {
				// tweak the diameter to make it look like a spiral
				
				// clip to paper boundaries
				if( isInsidePaperMargins(fx, fy) )
				{
					z = img.sample( fx - zigZagSpacing, fy - halfStep, fx + zigZagSpacing, fy + halfStep);
					moveTo(out, fx+nx, fy + ny, pulse_size < PULSE_MINIMUM);
					n = -n;
				} else {
					moveTo(out, fx, fy, true);
				}
			}
			n = -n;
			r -= ringSize;
			++numRings;
		}

		Log.write("yellow", numRings + " rings.");

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
