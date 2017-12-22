package com.marginallyclever.makelangeloRobot.converters;


import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangeloRobot.TransformedImage;
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
	public void finish(Writer out) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(sourceImage);

		imageStart(out);
		liftPen(out);
		machine.writeChangeToDefaultColor(out);
		
		convertPaperSpace(img, out);

		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}


	private void convertPaperSpace(TransformedImage img, Writer out) throws IOException {
		double PULSE_MINIMUM = 0.5;

		float yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin();
		float yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin();
		float xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin();
		float xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin();
		
		// figure out how many lines we're going to have on this image.
		float stepSize = machine.getPenDiameter() * blockScale;
		float halfStep = stepSize / 2.0f;
		float zigZagSpacing = machine.getPenDiameter();

		// from top to bottom of the image...
		float x, y, z, scale_z, pulse_size, i = 0;
		double n = 1;

		boolean lifted=true;
		if (direction == 0) {
			// horizontal
			for (y = yBottom; y < yTop; y += stepSize) {
				++i;

				if ((i % 2) == 0) {
					// every even line move left to right
					//moveTo(file,x,y,pen up?)]
					if(!lifted) {
						lifted=true;
						moveTo(out, xLeft, y + halfStep, true);
					}

					for (x = xLeft; x < xRight; x += zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - zigZagSpacing, y - halfStep, x + zigZagSpacing, y + halfStep);
						// scale the intensity value
						assert (z >= 0);
						assert (z <= 255.0f);
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;
						if(pulse_size<PULSE_MINIMUM) {
							if(!lifted) {
								moveTo(out, x, (y + halfStep + pulse_size * n), pulse_size < PULSE_MINIMUM);
								lifted=true;
							}
						} else {
							lifted=false;
							moveTo(out, x, (y + halfStep + pulse_size * n), pulse_size < PULSE_MINIMUM);
						}
						n = -n;
					}
					if(!lifted) {
						lifted=true;
						moveTo(out, xRight, y + halfStep, true);
					}
				} else {
					// every odd line move right to left
					//moveTo(file,x,y,pen up?)]
					if(!lifted) {
						lifted=true;
						moveTo(out, xRight, y + halfStep, true);
					}

					for (x = xRight; x >= xLeft; x -= zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - zigZagSpacing, y - halfStep, x + zigZagSpacing, y + halfStep);
						// scale the intensity value
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						assert (scale_z <= 1.0);
						pulse_size = halfStep * scale_z;
						if(pulse_size<PULSE_MINIMUM) {
							if(!lifted) {
								lifted=true;
								moveTo(out, x, (y + halfStep + pulse_size * n), pulse_size < PULSE_MINIMUM);
							}
						} else {
							lifted=false;
							moveTo(out, x, (y + halfStep + pulse_size * n), pulse_size < PULSE_MINIMUM);
						}
						n = -n;
					}
					
					if(!lifted) {
						lifted=true;
						moveTo(out, xLeft, y + halfStep, true);
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
					moveTo(out, x + halfStep, yBottom, true);

					for (y = yBottom; y < yTop; y += zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - halfStep, y - zigZagSpacing, x + halfStep, y + zigZagSpacing);
						// scale the intensity value
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;
						moveTo(out, (x + halfStep + pulse_size * n), y, pulse_size < PULSE_MINIMUM);
						n = -n;
					}
					moveTo(out, x + halfStep, yTop, true);
				} else {
					// every odd line move bottom to top
					//moveTo(file,x,y,pen up?)]
					moveTo(out, x + halfStep, yTop, true);

					for (y = yTop; y >= yBottom; y -= zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - halfStep, y - zigZagSpacing, x + halfStep, y + zigZagSpacing);
						// scale the intensity value
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;
						moveTo(out, (x + halfStep + pulse_size * n), y, pulse_size < PULSE_MINIMUM);
						n = -n;
					}
					moveTo(out, x + halfStep, yBottom, true);
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
