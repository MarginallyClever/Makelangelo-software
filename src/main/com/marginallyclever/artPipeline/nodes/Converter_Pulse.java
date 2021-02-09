package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.core.Point2D;
import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.core.node.NodeConnectorDouble;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;


/**
 * 
 * @author Dan Royer
 */
public class Converter_Pulse extends ImageConverter {
	// height of zigzag.  should probably be less than spacing.  >=1
	private NodeConnectorDouble inputHeight = new NodeConnectorDouble("Converter_Pulse.inputHeight",6.0);
	
	// TODO select-one-of-many
	private static int direction = 0;
	private String[] directionChoices = new String[]{Translator.get("horizontal"), Translator.get("vertical") }; 
	
	@Override
	public String getName() {
		return Translator.get("Converter_Pulse.name");
	}
	
	public String[] getDirections() {
		return directionChoices;
	}
	public int getDirectionIndex() {
		return direction;
	}
	public void setDirectionIndex(int value) {
		if(value<0) value=0;
		if(value>=directionChoices.length) value=directionChoices.length-1;
		direction = value;
	}
	
	protected void convertLine(Turtle turtle,TransformedImage img,double zigZagSpacing,double halfStep,Point2D a,Point2D b) {		
		Point2D dir = new Point2D(b.x-a.x,b.y-a.y);
		double len = dir.length();
		dir.scale(1/len);
		Point2D ortho = new Point2D(-dir.y,dir.x);
		
		turtle.jumpTo(
			a.x + ortho.x*halfStep,
			a.y + ortho.y*halfStep
		);

		int n=1;
		for (double p = 0; p <= len; p += zigZagSpacing) {
			double x = a.x + dir.x * p; 
			double y = a.y + dir.y * p; 
			// read a block of the image and find the average intensity in this block
			double z = img.sample( x - zigZagSpacing, y - halfStep, x + zigZagSpacing, y + halfStep);
			// scale the intensity value
			double scale_z = (255.0f - z) / 255.0f;
			//scale_z *= scale_z;  // quadratic curve
			double pulseSize = halfStep * scale_z;
			
			turtle.moveTo(
				x + ortho.x*pulseSize*n,
				y + ortho.y*pulseSize*n
			);
			n = -n;
		}
	}

	/**
	 * Converts images into zigzags in paper space instead of image space
	 */
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(inputImage.getValue());

		double [] bounds = img.getBounds();
		double yBottom = bounds[TransformedImage.BOTTOM];
		double yTop    = bounds[TransformedImage.TOP];
		double xLeft   = bounds[TransformedImage.LEFT];
		double xRight  = bounds[TransformedImage.RIGHT];
		
		// figure out how many lines we're going to have on this image.
		double stepSize = inputHeight.getValue();
		double halfStep = stepSize / 2.0f;
		double zigZagSpacing = 1.0;
		double spaceBetweenLines = stepSize;

		// from top to bottom of the image...
		double x, y = 0;
		int i=0;

		Point2D a = new Point2D();
		Point2D b = new Point2D();
		
		turtle = new Turtle();
		
		if (direction == 0) {
			// horizontal
			for (y = yBottom; y < yTop; y += spaceBetweenLines) {
				++i;

				if ((i % 2) == 0) {
					a.set(xLeft,y);
					b.set(xRight,y);
					convertLine(turtle,img,zigZagSpacing,halfStep,a,b);
				} else {
					a.set(xRight,y);
					b.set(xLeft,y);
					convertLine(turtle,img,zigZagSpacing,halfStep,a,b);
				}
			}
		} else {
			// vertical
			for (x = xLeft; x < xRight; x += spaceBetweenLines) {
				++i;

				if ((i % 2) == 0) {
					a.set(x,yBottom);
					b.set(x,yTop);
					convertLine(turtle,img,zigZagSpacing,halfStep,a,b);
				} else {
					a.set(x,yTop);
					b.set(x,yBottom);
					convertLine(turtle,img,zigZagSpacing,halfStep,a,b);
				}
			}
		}

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
