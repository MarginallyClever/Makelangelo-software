package com.marginallyclever.makelangeloRobot.converters;

import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_BlackAndWhite;


public class Converter_Pulse extends ImageConverter {
	private static float blockScale = 6.0f;
	private static int direction = 0;
	private String[] directionChoices = new String[]{Translator.get("horizontal"), Translator.get("vertical") }; 
	
	@Override
	public String getName() {
		return Translator.get("PulseLineName");
	}

	@Override
	public ImageConverterPanel getPanel() {
		return new Converter_Pulse_Panel(this);
	}
	
	public float getScale() {
		return blockScale;
	}
	public void setScale(float value) {
		if(value<1) value=1;
		blockScale = value;
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

	/**
	 * Converts images into zigzags in paper space instead of image space
	 *
	 * @param img the buffered image to convert
	 * @throws IOException couldn't open output file
	 */
	public void finish() {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(sourceImage);
		
		double yBottom = machine.getMarginBottom();
		double yTop    = machine.getMarginTop()   ;
		double xLeft   = machine.getMarginLeft()  ;
		double xRight  = machine.getMarginRight() ;
		
		// figure out how many lines we're going to have on this image.
		float stepSize = machine.getPenDiameter() * blockScale;
		float halfStep = stepSize / 2.0f;
		float zigZagSpacing = machine.getPenDiameter();

		// from top to bottom of the image...
		double x, y, z, scale_z, pulse_size, i = 0;
		double n = 1;

		turtle = new Turtle();
		
		if (direction == 0) {
			// horizontal
			for (y = yBottom; y < yTop; y += stepSize) {
				++i;

				if ((i % 2) == 0) {
					// every even line move left to right
					turtle.jumpTo(xLeft, y + halfStep);

					for (x = xLeft; x < xRight; x += zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - zigZagSpacing, y - halfStep, x + zigZagSpacing, y + halfStep);
						// scale the intensity value
						assert (z >= 0);
						assert (z <= 255.0f);
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;
						turtle.moveTo( x, (y + halfStep + pulse_size * n));
						n = -n;
					}
				} else {
					// every odd line move right to left
					//moveTo(file,x,y,pen up?)]
					turtle.jumpTo(xRight, y + halfStep);

					for (x = xRight; x >= xLeft; x -= zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - zigZagSpacing, y - halfStep, x + zigZagSpacing, y + halfStep);
						// scale the intensity value
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						assert (scale_z <= 1.0);
						pulse_size = halfStep * scale_z;
						turtle.moveTo(x, (y + halfStep + pulse_size * n));
						n = -n;
					}
				}
			}
		} else {
			// vertical
			for (x = xLeft; x < xRight; x += stepSize) {
				++i;

				if ((i % 2) == 0) {
					// every even line move top to bottom
					//moveTo(file,x,y,pen up?)]
					turtle.jumpTo(x + halfStep, yBottom);

					for (y = yBottom; y < yTop; y += zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - halfStep, y - zigZagSpacing, x + halfStep, y + zigZagSpacing);
						// scale the intensity value
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;
						turtle.moveTo((x + halfStep + pulse_size * n), y);
						n = -n;
					}
				} else {
					// every odd line move bottom to top
					//moveTo(file,x,y,pen up?)]
					turtle.jumpTo( x + halfStep, yTop);

					for (y = yTop; y >= yBottom; y -= zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - halfStep, y - zigZagSpacing, x + halfStep, y + zigZagSpacing);
						// scale the intensity value
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;
						turtle.moveTo((x + halfStep + pulse_size * n), y);
						n = -n;
					}
				}
			}
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
