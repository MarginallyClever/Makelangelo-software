package com.marginallyclever.makelangeloRobot.converters;


import java.awt.Color;
import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_CMYK;


// http://the-print-guide.blogspot.ca/2009/05/halftone-screen-angles.html
public class Converter_CMYK extends ImageConverter {
	static protected int passes=1;
	// Color values are from 0...255 inclusive.  255 is white, 0 is black.
	// Lift the pen any time the color value is > cutoff
	
	@Override
	public String getName() {
		return Translator.get("ConverterCMYKName");
	}

	@Override
	public ImageConverterPanel getPanel() {
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

		// avoids changing to black and then back to yellow.
		Color defaultColor = machine.getPenDownColorDefault();
		Color yellow = new Color(255,255,  0);
		machine.setPenDownColorDefault(yellow);
		
		imageStart(out);
		
		Log.info("Yellow...");		outputChannel(out,cmyk.getY(),0 ,new Color(255,255,  0));
		Log.info("Cyan...");		outputChannel(out,cmyk.getC(),15,new Color(  0,255,255));
		Log.info("Magenta...");		outputChannel(out,cmyk.getM(),75,new Color(255,  0,255));
		Log.info("Black...");		outputChannel(out,cmyk.getK(),45,new Color(  0,  0,  0));
		Log.info("Finishing...");

		machine.setPenDownColorDefault(defaultColor);
		imageEnd(out);
	}
	
	protected void outputChannel(Writer out,TransformedImage img,float angle,Color newColor) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		double dx = Math.cos(Math.toRadians(angle));
		double dy = Math.sin(Math.toRadians(angle));
		double [] channelCutoff = {0,153,51,102,204};
		
		liftPen(out);
		machine.writeChangeTo(out,newColor);

		// figure out how many lines we're going to have on this image.
		float stepSize = machine.getPenDiameter()*passes;
		if (stepSize < 1) stepSize = 1;

		// from top to bottom of the margin area...
		double height  = machine.getMarginTop() - machine.getMarginBottom();
		double width   = machine.getMarginRight() - machine.getMarginLeft();
		double maxLen  = Math.sqrt(width*width+height*height);

		double [] error0 = new double[(int)Math.ceil(maxLen)];
		double [] error1 = new double[(int)Math.ceil(maxLen)];
		
		double px,py,x0,y0,x1,y1,a;
		int i=0;
		for(a = -maxLen;a<maxLen;a+=stepSize) {
			px = dx * a;
			py = dy * a;
			// p0-p1 is at a right angle to dx/dy
			x0 = px - dy * maxLen;
			y0 = py + dx * maxLen;
			x1 = px + dy * maxLen;
			y1 = py - dx * maxLen;

			double cutoff=channelCutoff[i%channelCutoff.length];
			if ((i % 2) == 0) {
				convertAlongLineErrorTerms(x0,y0,x1,y1,stepSize,cutoff,error0,error1,img,out);
			} else {
				convertAlongLineErrorTerms(x1,y1,x0,y0,stepSize,cutoff,error0,error1,img,out);
			}
			
			for(int j=0;j<error0.length;++j) {
				error0[j]=error1[error0.length-1-j];
				error1[error0.length-1-j]=0;
			}
			++i;
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
