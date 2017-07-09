package com.marginallyclever.makelangeloRobot.converters;


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
	protected double cutoffC=128.0;
	protected double cutoffY=128.0;
	protected double cutoffM=128.0;
	protected double cutoffK=128.0;
	
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
		
		outputChannel(out,cmyk.getY(),0 ,cutoffY);
		outputChannel(out,cmyk.getC(),15,cutoffC);
		outputChannel(out,cmyk.getM(),75,cutoffM);
		outputChannel(out,cmyk.getK(),45,cutoffK);

		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}
	
	void outputChannel(Writer out,TransformedImage img,float angle,double channelCutoff) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		double majorX = Math.cos(Math.toRadians(angle));
		double majorY = Math.sin(Math.toRadians(angle));

		liftPen(out);
		machine.writeChangeTo(out);

		// figure out how many lines we're going to have on this image.
		float steps = machine.getDiameter()*passes;
		if (steps < 1) steps = 1;

		// from top to bottom of the margin area...
		double yBottom = machine.getPaperBottom() * machine.getPaperMargin() * 10;
		double yTop    = machine.getPaperTop()    * machine.getPaperMargin() * 10;
		double xLeft   = machine.getPaperLeft()   * machine.getPaperMargin() * 10;
		double xRight  = machine.getPaperRight()  * machine.getPaperMargin() * 10;
		double dy = yTop - yBottom;
		double dx = xRight - xLeft;
		double radius = Math.sqrt(dx*dx+dy*dy);
		double r2     = radius*2;

		double majorPX,majorPY,startPX,startPY,endPX,endPY,a;
		int i=0;
		for(a = -radius;a<radius;a+=steps) {
			majorPX = majorX * a;
			majorPY = majorY * a;
			startPX = majorPX - majorY * radius;
			startPY = majorPY + majorX * radius;
			endPX   = majorPX + majorY * radius;
			endPY   = majorPY - majorX * radius;

			if ((i % 2) == 0) {
				convertAlongLine(startPX,startPY,endPX,endPY,steps,r2,channelCutoff,img,out);
			} else {
				convertAlongLine(endPX,endPY,startPX,startPY,steps,r2,channelCutoff,img,out);
			}
			++i;
		}
		liftPen(out);
	}
	
	protected void convertAlongLine(double x0,double y0,double x1,double y1,double stepSize,double r2,double channelCutoff,TransformedImage img,Writer out) throws IOException {
		double b;
		double dx=x1-x0;
		double dy=y1-y0;
		double halfStep = stepSize/2.0;
		double steps = r2 / stepSize;
		if(steps<1) steps=1;

		double n,x,y,v;

		for (b = 0; b <= steps; ++b) {
			n = b / steps;
			x = dx * n + x0;
			y = dy * n + y0;
			if(isInsidePaperMargins(x, y)) {
				v = img.sample( x - halfStep, y - halfStep, x + halfStep, y + halfStep);
			} else {
				v = 255;
			}
			lineTo(out, x, y, v>=channelCutoff);
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
