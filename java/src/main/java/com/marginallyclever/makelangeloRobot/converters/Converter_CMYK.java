package com.marginallyclever.makelangeloRobot.converters;


import java.awt.Color;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JPanel;

import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_CMYK;


// http://the-print-guide.blogspot.ca/2009/05/halftone-screen-angles.html
public class Converter_CMYK extends ImageConverter {
	static protected int passes=4;
	// Color values are from 0...255 inclusive.  255 is white, 0 is black.
	// Lift the pen any time the color value is > cutoff
	
	@Override
	public String getName() {
		return Translator.get("ConverterCMYKName");
	}

	@Override
	public JPanel getPanel() {
		return new Converter_CMYK_Panel(this);
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
		Filter_CMYK cmyk = new Filter_CMYK();
		cmyk.filter(sourceImage);

		// Set up the conversion from image space to paper space, select the current tool, etc.
		imageStart(out);
		
		outputChannel(out,cmyk.getY(),0 ,new Color(255,242,  0));
		outputChannel(out,cmyk.getC(),15,new Color(  0,174,239));
		outputChannel(out,cmyk.getM(),75,new Color(236,  0,140));
		outputChannel(out,cmyk.getK(),45,new Color(  0,  0,  0));

		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}
	
	void outputChannel(Writer out,TransformedImage img,float angle,Color newColor) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		double majorX = Math.cos(Math.toRadians(angle));
		double majorY = Math.sin(Math.toRadians(angle));
		final double [] channelCutoff = {51,153,102,204};
		
		liftPen(out);
		machine.writeChangeTo(out,newColor);

		// figure out how many lines we're going to have on this image.
		float stepSize = machine.getPenDiameter()*passes;
		if (stepSize < 1) stepSize = 1;

		// from top to bottom of the margin area...
		double yBottom = machine.getPaperBottom() * machine.getPaperMargin() * 10;
		double yTop    = machine.getPaperTop()    * machine.getPaperMargin() * 10;
		double xLeft   = machine.getPaperLeft()   * machine.getPaperMargin() * 10;
		double xRight  = machine.getPaperRight()  * machine.getPaperMargin() * 10;
		double dy = yTop - yBottom;
		double dx = xRight - xLeft;
		double radius = Math.sqrt(dx*dx+dy*dy);

		double majorPX,majorPY,startPX,startPY,endPX,endPY,a;
		int i=0;
		for(a = -radius;a<radius;a+=stepSize) {
			majorPX = majorX * a;
			majorPY = majorY * a;
			startPX = majorPX - majorY * radius;
			startPY = majorPY + majorX * radius;
			endPX   = majorPX + majorY * radius;
			endPY   = majorPY - majorX * radius;

			if ((i % 2) == 0) {
				convertAlongLine(startPX,startPY,endPX,endPY,stepSize,channelCutoff[i%4],img,out);
			} else {
				convertAlongLine(endPX,endPY,startPX,startPY,stepSize,channelCutoff[i%4],img,out);
			}
			++i;
		}
		liftPen(out);
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
