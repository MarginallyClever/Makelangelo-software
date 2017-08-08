package com.marginallyclever.makelangeloRobot.converters;


import java.io.IOException;
import java.io.Writer;
import javax.swing.JPanel;
import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_BlackAndWhite;


// create random lines across the image.  Raise and lower the pen to darken the appropriate areas
public class Converter_Wander extends ImageConverter {
	static protected int numLines = 2500;
	
	@Override
	public String getName() {
		return Translator.get("ConverterWanderName");
	}

	@Override
	public JPanel getPanel() {
		return new Converter_Wander_Panel(this);
	}
	
	public void finish(Writer out) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(sourceImage);


		// Set up the conversion from image space to paper space, select the current tool, etc.
		imageStart(out);
		liftPen(out);
		machine.writeChangeTo(out);

		float stepSize = machine.getPenDiameter()*5;
		if (stepSize < 1) stepSize = 1;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).
		double level = 255.0 / 4.0;

		// from top to bottom of the margin area...
		float yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin() * 10;
		float yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin() * 10;
		float xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin() * 10;
		float xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin() * 10;
		double dy = yTop - yBottom-1;
		double dx = xRight - xLeft-1;

		liftPen(out);
		moveTo(out,0,yTop,true);

		double startPX = 0; 
		double startPY = yTop;

		int i;
		for(i=0;i<numLines;++i) {
			level = 200.0 * (double)i / (double)numLines;
			double endPX = xLeft   + (Math.random() * dx)+0.5; 
			double endPY = yBottom + (Math.random() * dy)+0.5; 

			convertAlongLine(startPX,startPY,endPX,endPY,stepSize,level,img,out);
			
			startPX = endPX;
			startPY = endPY;
		}

		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}
	

	public int getLineCount() {
		return numLines;
	}
	public void setLineCount(int value) {
		if(value<1) value=1;
		numLines = value;
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
