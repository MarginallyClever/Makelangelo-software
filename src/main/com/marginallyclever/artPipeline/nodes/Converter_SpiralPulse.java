package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.NodeConnectorBoolean;
import com.marginallyclever.core.node.NodeConnectorDouble;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_SpiralPulse extends ImageConverter {
	// draw the spiral right out to the edges of the square bounds.
	private NodeConnectorBoolean convertToCorners = new NodeConnectorBoolean("Converter_SpiralPulse.toCorners",true);
	// increase to tighten zigzags.  0.1-3.0
	private NodeConnectorDouble inputDensity = new NodeConnectorDouble("Converter_SpiralPulse.inputDensity",1.2);
	// space between rings of spiral.  0.5-10.0
	private NodeConnectorDouble inputSpacing = new NodeConnectorDouble("Converter_SpiralPulse.inputSpacing",2.5);
	// height of zigzag.  should probably be less than spacing.  1-10
	private NodeConnectorDouble inputHeight = new NodeConnectorDouble("Converter_SpiralPulse.inputHeight",4.0);
	
	
	public Converter_SpiralPulse() {
		super();
	}
	
	@Override
	public String getName() {
		return Translator.get("Converter_SpiralPulse.name");
	}

	/**
	 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		// black and white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(inputImage.getValue());

		double toolDiameter = 1.0;

		double [] bounds = img.getBounds();
		double yBottom = bounds[TransformedImage.BOTTOM];
		double yTop    = bounds[TransformedImage.TOP];
		double xLeft   = bounds[TransformedImage.LEFT];
		double xRight  = bounds[TransformedImage.RIGHT];
		double w = xRight - xLeft;
		double h = yTop - yBottom;
		
		double maxr;
		
		if (convertToCorners.getValue()) {
			// go right to the corners
			maxr = (Math.sqrt(h*h + w*w) + 1.0f);
		} else {
			// do the largest circle that still fits in the margin.
			w/=2;
			h/=2;
			maxr = ( h < w ? h : w );
		}
		
		double r = maxr-toolDiameter*5.0, f;
		double fx, fy;
		int numRings = 0;
		double stepSize = toolDiameter * inputHeight.getValue();
		double halfStep = stepSize / 2.0f;
		double zigZagSpacing = toolDiameter;
		int n=1;
		double PULSE_MINIMUM = 0.1f;
		double ringSize = halfStep*inputSpacing.getValue();
		boolean init = false;
		int i;
		int z = 0;
		double r2,scale_z,pulse_size,nx,ny;

		double zigDensity = inputDensity.getValue();
		
		turtle = new Turtle();
		
		while (r > toolDiameter) {
			// find circumference of current circle
			double circumference = Math.floor((2.0f * r - toolDiameter) * Math.PI)*zigDensity;
			//if (circumference > 360.0f) circumference = 360.0f;
			
			for (i = 0; i <= circumference; ++i) {
				// tweak the diameter to make it look like a spiral
				r2 = r - ringSize * (float)i / circumference;
				
				f = (float)Math.PI * 2.0f * (float)i / circumference;
				fx = (float)Math.cos(f) * r2;
				fy = (float)Math.sin(f) * r2;
				// clip to paper boundaries
				boolean isInside = (fx>=xLeft && fx<xRight && fy>=yBottom && fy<yTop);
				if(isInside) {
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

		outputTurtle.setValue(turtle);
		return false;
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
