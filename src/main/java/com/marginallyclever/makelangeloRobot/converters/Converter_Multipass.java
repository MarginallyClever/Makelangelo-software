package com.marginallyclever.makelangeloRobot.converters;


import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_BlackAndWhite;


public class Converter_Multipass extends ImageConverter {
	static private float angle=0;
	static private int passes=4;
	
	@Override
	public String getName() {
		return Translator.get("ConverterMultipassName");
	}

	@Override
	public ImageConverterPanel getPanel() {
		return new Converter_Multipass_Panel(this);
	}
	
	public float getAngle() {
		return angle;
	}
	public void setAngle(float value) {
		angle = value;
	}
	public int getPasses() {
		return passes;
	}
	public void setPasses(int value) {
		if(passes<1) passes=1;
		passes=value;
	}
	
	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 *
	 * @param img the image to convert.
	 */
	@Override
	public void finish(Writer out) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(sourceImage);

		double majorX = Math.cos(Math.toRadians(angle));
		double majorY = Math.sin(Math.toRadians(angle));

		// Set up the conversion from image space to paper space, select the current tool, etc.
		imageStart(out);
		liftPen(out);
		machine.writeChangeToDefaultColor(out);

		// figure out how many lines we're going to have on this image.
		float stepSize = machine.getPenDiameter();
		if (stepSize < 1) stepSize = 1;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).
		double level = 255.0 / (double)(passes+1);

		// from top to bottom of the margin area...
		double yBottom = machine.getPaperBottom() * machine.getPaperMargin();
		double yTop    = machine.getPaperTop()    * machine.getPaperMargin();
		double xLeft   = machine.getPaperLeft()   * machine.getPaperMargin();
		double xRight  = machine.getPaperRight()  * machine.getPaperMargin();
		double dy = yTop - yBottom;
		double dx = xRight - xLeft;
		double radius = Math.sqrt(dx*dx+dy*dy);

		int i=0;
		for(double a = -radius;a<radius;a+=stepSize) {
			double majorPX = majorX * a;
			double majorPY = majorY * a;
			double startPX = majorPX - majorY * radius;
			double startPY = majorPY + majorX * radius;
			double endPX   = majorPX + majorY * radius;
			double endPY   = majorPY - majorX * radius;
		
			double l2 = level * (1 + (i % passes));
			if ((i % 2) == 0) {
				convertAlongLine(startPX,startPY,endPX,endPY,stepSize,l2,img,out);
			} else {
				convertAlongLine(endPX,endPY,startPX,startPY,stepSize,l2,img,out);
			}
			++i;
		}

	    lineTo(out, machine.getHomeX(), machine.getHomeY(), true);
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
