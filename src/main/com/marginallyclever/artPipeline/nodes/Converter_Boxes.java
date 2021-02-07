package com.marginallyclever.artPipeline.nodes;


import com.marginallyclever.artPipeline.nodes.panels.Converter_Boxes_Panel;
import com.marginallyclever.convenience.TransformedImage;
import com.marginallyclever.convenience.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.convenience.nodes.NodeConnectorInt;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * A grid of boxes across the paper, and make the boxes bigger if the image is darker in that area.
 * @author Dan Royer
 *
 */
public class Converter_Boxes extends ImageConverter {
	// only consider intensity above the lowpass value.
	private NodeConnectorInt inputLowpass = new NodeConnectorInt("Generator_Dragon.inputLowpass",127);
	// how big should the largest box be?
	private NodeConnectorInt inputMaxBoxSize = new NodeConnectorInt("Generator_Dragon.inputMaxBoxSize",4);
	
	public int boxMaxSize;
	public int cutoff;

	Turtle turtle;
	
	@Override
	public String getName() {
		return Translator.get("BoxGeneratorName");
	}


	@Override
	public NodePanel getPanel() {
		return (NodePanel)new Converter_Boxes_Panel(this);
	}
	
	@Override
	public void restart() {
		turtle = new Turtle();
		boxMaxSize = inputMaxBoxSize.getValue();
		cutoff=  inputLowpass.getValue();
	}
	
	@Override
	public boolean iterate() {
		if(inputImage.getValue() == null) return false;
		
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(inputImage.getValue());

		Turtle turtle = new Turtle();
		
		double [] bounds = img.getBounds();
		double yBottom = bounds[TransformedImage.BOTTOM];
		double yTop    = bounds[TransformedImage.TOP];
		double xLeft   = bounds[TransformedImage.LEFT];
		double xRight  = bounds[TransformedImage.RIGHT];
		double pw = xRight - xLeft;
		
		// figure out how many lines we're going to have on this image.
		double fullStep = 2.0*boxMaxSize;
		double halfStep = fullStep / 2.0f;
		
		double steps = pw / fullStep;
		if (steps < 1) steps = 1;
		
		// from top to bottom of the image...
		double x, y, z;
		int i = 0;
		for (y = yBottom + halfStep; y < yTop - halfStep; y += fullStep) {
			++i;
			if ((i % 2) == 0) {
				// every even line move left to right
				for (x = xLeft; x < xRight; x += fullStep) {
					// read a block of the image and find the average intensity in this block
					z = img.sample( x, y - halfStep, x + fullStep, y + halfStep );
					// scale the intensity value
					double scaleZ =  (255.0f - z) / 255.0;
					double pulseSize = (halfStep) * scaleZ *0.9;
					if (scaleZ > cutoff/255.0) {
						drawFilledBox(turtle,x,y,halfStep,pulseSize);
					}
				}
			} else {
				// every odd line move right to left
				for (x = xRight; x > xLeft; x -= fullStep) {
					// read a block of the image and find the average intensity in this block
					z = img.sample(x - fullStep, y - halfStep, x, y + halfStep );
					// scale the intensity value
					double scaleZ = (255.0f - z) / 255.0f;
					double pulseSize = (halfStep - 0.5f) * scaleZ;
					if (pulseSize > cutoff/255.0) {
						drawFilledBox(turtle,x,y,halfStep,pulseSize);
					}
				}
			}
		}
		
		outputTurtle.setValue(turtle);
		return false;
	}
	
	protected void drawFilledBox(Turtle turtle,double x,double y,double halfStep,double pulseSize) {
		double d = 2.0*boxMaxSize;
		
		double xmin = x - halfStep - pulseSize;
		double xmax = x - halfStep + pulseSize;
		double ymin = y + halfStep - pulseSize;
		double ymax = y + halfStep + pulseSize;
		// draw a square.  the diameter is relative to the intensity.
		turtle.jumpTo(xmin, ymin);
		turtle.moveTo(xmax, ymin);
		turtle.moveTo(xmax, ymax);
		turtle.moveTo(xmin, ymax);
		turtle.moveTo(xmin, ymin);
		// fill in the square
		boolean flip = false;
		for(double yy=ymin;yy<ymax;yy+=d) {
			turtle.moveTo(flip?xmin:xmax,yy);
			flip = !flip;
		}
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
