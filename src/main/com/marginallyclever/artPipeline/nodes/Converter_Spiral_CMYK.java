package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.core.ColorRGB;
import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.imageFilters.Filter_CMYK;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.NodeConnectorBoolean;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
 * Inspired by reddit user bosny
 * @author Dan Royer
 */
public class Converter_Spiral_CMYK extends ImageConverter {
	// draw the spiral right out to the edges of the square bounds.
	private NodeConnectorBoolean convertToCorners = new NodeConnectorBoolean("Converter_Spiral_CMYK.toCorners",true);
	// cyan channel
	protected NodeConnectorTurtle outputTurtleC = new NodeConnectorTurtle("Converter_Spiral_CMYK.outputTurtleC");
	// magenta channel
	protected NodeConnectorTurtle outputTurtleM = new NodeConnectorTurtle("Converter_Spiral_CMYK.outputTurtleM");
	// yellow channel
	protected NodeConnectorTurtle outputTurtleY = new NodeConnectorTurtle("Converter_Spiral_CMYK.outputTurtleY");
	
	public Converter_Spiral_CMYK() {
		super();
		inputs.add(convertToCorners);
		outputs.remove(outputTurtle);
		outputs.add(outputTurtleY);
		outputs.add(outputTurtleC);
		outputs.add(outputTurtleM);
		outputs.add(outputTurtle);
	}
	
	@Override
	public String getName() {
		return Translator.get("Converter_Spiral_CMYK.name");
	}
	
	@Override
	public boolean iterate() {
		Filter_CMYK cmyk = new Filter_CMYK();
		cmyk.filter(inputImage.getValue());

		double [] bounds = inputImage.getValue().getBounds();
		double h2 = bounds[TransformedImage.TOP] - bounds[TransformedImage.BOTTOM];
		double w2 = bounds[TransformedImage.RIGHT] - bounds[TransformedImage.LEFT];
		double separation = (w2<h2) ? w2/4 : h2/4;

		Log.message("Yellow...");		outputTurtleY.setValue(outputChannel(cmyk.getY(),new ColorRGB(255,255,  0),255.0*1.0,Math.cos(Math.toRadians(45    ))*separation,Math.sin(Math.toRadians(45    ))*separation));
		Log.message("Cyan...");			outputTurtleC.setValue(outputChannel(cmyk.getC(),new ColorRGB(  0,255,255),255.0*1.0,Math.cos(Math.toRadians(45+ 90))*separation,Math.sin(Math.toRadians(45+ 90))*separation));
		Log.message("Magenta...");		outputTurtleM.setValue(outputChannel(cmyk.getM(),new ColorRGB(255,  0,255),255.0*1.0,Math.cos(Math.toRadians(45+180))*separation,Math.sin(Math.toRadians(45+180))*separation));
		Log.message("Black...");		outputTurtle .setValue(outputChannel(cmyk.getK(),new ColorRGB(  0,  0,  0),255.0*1.0,Math.cos(Math.toRadians(45+270))*separation,Math.sin(Math.toRadians(45+270))*separation));
		Log.message("Finishing...");

		return false;
	}

	protected Turtle outputChannel(TransformedImage img,ColorRGB newColor,double cutoff,double cx,double cy) {
		Turtle turtle = new Turtle();
		
		turtle.setColor(newColor);
		
		double toolDiameter = 2.0;

		int i, j;
		int steps = 4;
		double leveladd = cutoff / (double)(steps+1);
		double level;
		int z = 0;

		double [] bounds = img.getBounds();
		double yBottom = bounds[TransformedImage.BOTTOM];
		double yTop    = bounds[TransformedImage.TOP];
		double xLeft   = bounds[TransformedImage.LEFT];
		double xRight  = bounds[TransformedImage.RIGHT];
		double h2 = yTop - yBottom;
		double w2 = xRight - xLeft;

		double maxr;
		if (convertToCorners.getValue()) {
			// go right to the corners
			maxr = (Math.sqrt(h2 * h2 + w2 * w2) + 1.0);
		} else {
			// do the largest circle that still fits in the image.
			double w = w2/2.0;
			double h = h2/2.0;
			maxr = ( h < w ? h : w );
		}

		
		double r = maxr, f;
		double fx, fy;
		boolean wasInside = false;
		int numRings = 0;
		int downMoves = 0;
		j = 0;
		while (r > toolDiameter) {
			++j;
			level = leveladd * (1+(j % steps));
			// find circumference of current circle
			double circumference = Math.floor((2.0f * r - toolDiameter) * Math.PI);
			if (circumference > 360.0f) circumference = 360.0f;
			
			for (i = 1; i <= circumference; ++i) {
				f = Math.PI * 2.0f * (double)i / circumference;
				fx = cx+Math.cos(f) * r;
				fy = cy+Math.sin(f) * r;

				boolean isInside = (fx>=xLeft && fx<xRight && fy>=yBottom && fy<yTop);
				if(isInside != wasInside) {
					turtle.moveTo(fx,fy);
					turtle.penUp();
				}
				
				if(isInside) {
					try {
						z = img.sample3x3(fx, fy);
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					if(z<level) {
						turtle.penDown();
						downMoves++;
					} else {
						turtle.penUp();
					}
					turtle.moveTo(fx, fy);
				}

				wasInside = isInside;
			}
			r -= toolDiameter;
			++numRings;
		}

		Log.message(downMoves + " down moves.");
		Log.message(numRings + " rings.");
		
		return turtle;
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
