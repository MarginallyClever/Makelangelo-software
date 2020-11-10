package com.marginallyclever.artPipeline.converters;

import com.marginallyclever.artPipeline.TransformedImage;
import com.marginallyclever.artPipeline.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_SpiralPulse extends ImageConverter {
	private static boolean convertToCorners = false;  // draw the spiral right out to the edges of the square bounds.
	private static float zigDensity = 1.2f;  // increase to tighten zigzags
	private static float spacing = 2.5f;
	private static float height = 4.0f;
	
	@Override
	public String getName() {
		return Translator.get("SpiralPulseName");
	}

	@Override
	public ImageConverterPanel getPanel() {
		return new Converter_SpiralPulse_Panel(this);
	}


	/**
	 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void finish() {
		// black and white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(sourceImage);

		double toolDiameter = machine.getPenDiameter();

		float maxr;
		
		if (convertToCorners) {
			// go right to the corners
			float h2 = (float)machine.getMarginHeight();
			float w2 = (float)machine.getMarginWidth();
			maxr = (float) (Math.sqrt(h2 * h2 + w2 * w2) + 1.0f);
		} else {
			// do the largest circle that still fits in the margin.
			float w = (float)(machine.getMarginWidth())/2.0f;
			float h = (float)(machine.getMarginHeight())/2.0f;
			maxr = (float)( h < w ? h : w );
		}
		
		float r = maxr-(float)toolDiameter*5.0f, f;
		float fx, fy;
		int numRings = 0;
		float stepSize = machine.getPenDiameter() * height;
		float halfStep = stepSize / 2.0f;
		float zigZagSpacing = machine.getPenDiameter();
		int n=1;
		float PULSE_MINIMUM = 0.1f;
		float ringSize = halfStep*spacing;
		boolean init = false;
		int i;
		int z = 0;
		float r2,scale_z,pulse_size,nx,ny;

		turtle = new Turtle();
		
		while (r > toolDiameter) {
			// find circumference of current circle
			float circumference = (float) Math.floor((2.0f * r - toolDiameter) * Math.PI)*zigDensity;
			//if (circumference > 360.0f) circumference = 360.0f;
			
			for (i = 0; i <= circumference; ++i) {
				// tweak the diameter to make it look like a spiral
				r2 = r - ringSize * (float)i / circumference;
				
				f = (float)Math.PI * 2.0f * (float)i / circumference;
				fx = (float)Math.cos(f) * r2;
				fy = (float)Math.sin(f) * r2;
				// clip to paper boundaries
				if( isInsidePaperMargins(fx, fy) )
				{
					z = img.sample( fx - zigZagSpacing, fy - halfStep, fx + zigZagSpacing, fy + halfStep);
					scale_z = (255.0f - z) / 255.0f;
					pulse_size = halfStep * scale_z;
					nx = (halfStep+pulse_size*n) * fx / r2;
					ny = (halfStep+pulse_size*n) * fy / r2;

					if (!init) {
						turtle.moveTo(fx+nx, fy+ny);
						init = true;
					}
					if(pulse_size < PULSE_MINIMUM) turtle.penUp();
					else turtle.penDown();
					turtle.moveTo(fx+nx, fy + ny);
					n = -n;
				} else {
					if (!init) {
						init = true;
					}
					turtle.penUp();
					turtle.moveTo(fx, fy);
				}
			}
			n = -n;
			r -= ringSize;
			++numRings;
		}

		Log.message(numRings + " rings.");
	}

	public void setIntensity(float floatValue) {
		if(floatValue<0.1) floatValue=0.1f;
		if(floatValue>3.0) floatValue=3.0f;
		zigDensity=floatValue;
	}
	public float getIntensity() {
		return zigDensity;
	}

	public void setSpacing(float floatValue) {
		if(floatValue<0.5f) floatValue=0.5f;
		if(floatValue>10) floatValue=10;
		spacing=floatValue;
	}
	public float getSpacing() {
		return spacing;
	}

	public void setHeight(float floatValue) {
		if(floatValue<0.1) floatValue=1;
		if(floatValue>10) floatValue=10;
		height = floatValue;
	}
	public float getHeight() {
		return height;
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
