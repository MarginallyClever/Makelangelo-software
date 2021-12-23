package com.marginallyclever.makelangelo.makeArt.imageConverter;


import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import com.marginallyclever.makelangelo.makeArt.imageFilter.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * A grid of boxes across the paper, and make the boxes bigger if the image is darker in that area.
 * @author Dan Royer
 *
 */
public class Converter_Boxxy extends ImageConverter {
	public static int boxMaxSize=4; // 0.8*5
	public static int cutoff=127;
	
	@Override
	public String getName() {
		return Translator.get("BoxGeneratorName");
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("size")) setBoxMaxSize((int)evt.getNewValue());
		if(evt.getPropertyName().equals("cutoff")) setCutoff((int)evt.getNewValue());
	}

	public void setBoxMaxSize(int arg0) {
		boxMaxSize=arg0;
	}
	
	public int getBoxMasSize() {
		return boxMaxSize;
	}
	
	public void setCutoff(int arg0) {
		cutoff = arg0; 
	}
	public int getCutoff() {
		return cutoff;
	}
	
	@Override
	public void finish() {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(myImage);

		double yBottom = myPaper.getMarginBottom();
		double yTop    = myPaper.getMarginTop();
		double xLeft   = myPaper.getMarginLeft();
		double xRight  = myPaper.getMarginRight();
		double pw = xRight - xLeft;
		
		// figure out how many lines we're going to have on this image.
		double fullStep = boxMaxSize;
		double halfStep = fullStep / 2.0f;
		
		double steps = pw / fullStep;
		if (steps < 1) steps = 1;

		turtle = new Turtle();

		double lowpass = cutoff/255.0;
		
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
					if (scaleZ > lowpass) {
						double ratio = (scaleZ-lowpass)/(1.0-lowpass);
						drawBox(x,y,ratio,halfStep);
					}
				}
			} else {
				// every odd line move right to left
				for (x = xRight; x > xLeft; x -= fullStep) {
					// read a block of the image and find the average intensity in this block
					z = img.sample( x - halfStep, y - halfStep, x + halfStep, y + halfStep);
					// scale the intensity value
					double scaleZ = (255.0f - z) / 255.0f;
					if (scaleZ > lowpass) {
						double ratio = (scaleZ-lowpass)/(1.0-lowpass);
						drawBox(x,y,ratio,halfStep);
					}
				}
			}
		}
	}

	private void drawBox(double x,double y,double ratio,double halfStep) {
		double pulseSize = (halfStep - 0.5f) * ratio;
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
		for(double yy=ymin;yy<ymax;yy+=boxMaxSize) {
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
