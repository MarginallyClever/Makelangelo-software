package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.artPipeline.nodes.panels.Converter_Spiral_Panel;
import com.marginallyclever.convenience.TransformedImage;
import com.marginallyclever.convenience.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_Spiral extends ImageConverter {
	private static boolean convertToCorners = true;  // draw the spiral right out to the edges of the square bounds.

	private NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle();
	
	public Converter_Spiral() {
		super();
		outputs.add(outputTurtle);
	}
	
	@Override
	public String getName() {
		return Translator.get("SpiralName");
	}

	@Override
	public NodePanel getPanel() {
		return new Converter_Spiral_Panel(this);
	}

	public boolean getToCorners() {
		return convertToCorners;
	}
	
	public void setToCorners(boolean arg0) {
		convertToCorners=arg0;
	}
	
	/**
	 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		// black and white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(sourceImage.getValue());

		double toolDiameter = 1.0;

		int i, j;
		final int steps = 4;
		double leveladd = 255.0 / (double)(steps+1);
		double level;
		int z = 0;

		double [] bounds = img.getBounds();
		double yBottom = bounds[TransformedImage.BOTTOM];
		double yTop    = bounds[TransformedImage.TOP];
		double xLeft   = bounds[TransformedImage.LEFT];
		double xRight  = bounds[TransformedImage.RIGHT];
		double w = xRight - xLeft;
		double h = yTop - yBottom;
		
		double maxr;
		if (convertToCorners) {
			// go right to the corners
			maxr = (Math.sqrt(h*h + w*w) + 1.0f);
		} else {
			// do the largest circle that still fits in the image.
			w/=2.0f;
			h/=2.0f;
			maxr = ( h < w ? h : w );
		}

		turtle = new Turtle();
		
		double r = maxr, f;
		double fx, fy;
		int numRings = 0;
		j = 0;
		while (r > toolDiameter) {
			++j;
			level = leveladd * (1+(j%steps));
			// find circumference of current circle
			float circumference = (float) Math.floor((2.0f * r - toolDiameter) * Math.PI);
			if (circumference > 360.0f) circumference = 360.0f;

			for (i = 0; i <= circumference; ++i) {
				f = Math.PI * 2.0 * (double)i / (double)circumference;
				fx = Math.cos(f) * r;
				fy = Math.sin(f) * r;

				boolean isInside = (fx>=xLeft && fx<xRight && fy>=yBottom && fy<yTop);
				if(isInside) {
					try {
						z = img.sample3x3(fx, fy);
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					if(z<level) turtle.penDown();
					else turtle.penUp();
				} else turtle.penUp();
				turtle.moveTo(fx, fy);
			}
			r -= toolDiameter;
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
