package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import com.marginallyclever.makelangelo.makeArt.imageFilter.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_SpiralPulse extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_SpiralPulse.class);
	private static boolean convertToCorners = false;  // draw the spiral right out to the edges of the square bounds.
	private static double zigDensity = 1.2f;  // increase to tighten zigzags
	private static double spacing = 2.5f;
	private static double height = 4.0f;
	
	@Override
	public String getName() {
		return Translator.get("SpiralPulseName");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("intensity")) setIntensity((double)evt.getNewValue());
		if(evt.getPropertyName().equals("spacing")) setSpacing((double)evt.getNewValue());
		if(evt.getPropertyName().equals("height")) setHeight((double)evt.getNewValue());
	}

	/**
	 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void finish() {
		// black and white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(myImage);

		double toolDiameter = 1;

		double maxr;
		
		if (convertToCorners) {
			// go right to the corners
			float h2 = (float)myPaper.getMarginHeight();
			float w2 = (float)myPaper.getMarginWidth();
			maxr = (float) (Math.sqrt(h2 * h2 + w2 * w2) + 1.0f);
		} else {
			// do the largest circle that still fits in the margin.
			float w = (float)(myPaper.getMarginWidth())/2.0f;
			float h = (float)(myPaper.getMarginHeight())/2.0f;
			maxr = (float)( h < w ? h : w );
		}
		
		double r = maxr - toolDiameter*5.0f, f;
		double fx, fy;
		int numRings = 0;
		double stepSize = toolDiameter * height;
		double halfStep = stepSize / 2.0f;
		double zigZagSpacing = toolDiameter;
		int n=1;
		double PULSE_MINIMUM = 0.1f;
		double ringSize = halfStep*spacing;
		boolean init = false;
		int i;
		int z = 0;
		double r2,scale_z,pulse_size,nx,ny;

		turtle = new Turtle();
		
		while (r > toolDiameter) {
			// find circumference of current circle
			double circumference =  Math.floor((2.0f * r - toolDiameter) * Math.PI)*zigDensity;
			//if (circumference > 360.0f) circumference = 360.0f;
			
			for (i = 0; i <= circumference; ++i) {
				// tweak the diameter to make it look like a spiral
				r2 = r - ringSize * (float)i / circumference;
				
				f = Math.PI * 2.0f * (float)i / circumference;
				fx = Math.cos(f) * r2;
				fy = Math.sin(f) * r2;
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

		logger.debug("{} rings.", numRings);
	}

	public void setIntensity(double v) {
		if(v<0.1) v=0.1f;
		if(v>3.0) v=3.0f;
		zigDensity=v;
	}
	public double getIntensity() {
		return zigDensity;
	}

	public void setSpacing(double v) {
		if(v<0.5f) v=0.5f;
		if(v>10) v=10;
		spacing=v;
	}
	public double getSpacing() {
		return spacing;
	}

	public void setHeight(double v) {
		if(v<0.1) v=1;
		if(v>10) v=10;
		height = v;
	}
	public double getHeight() {
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
