package com.marginallyclever.converters;


import java.awt.GridLayout;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.basictypes.TransformedImage;
import com.marginallyclever.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.Translator;


public class Converter_Multipass extends ImageConverter {
	static private float angle=0;
	static private int passes=4;
	
	@Override
	public String getName() {
		return Translator.get("ConverterMultipassName");
	}

	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 *
	 * @param img the image to convert.
	 */
	@Override
	public boolean convert(TransformedImage img,Writer out) throws IOException {
		final JTextField angleField = new JTextField(Float.toString(angle));
		final JTextField passesField = new JTextField(Integer.toString(passes));

		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.add(new JLabel(Translator.get("ConverterMultipassAngle")));
		panel.add(angleField);
		panel.add(new JLabel(Translator.get("ConverterMultipassLevels")));
		panel.add(passesField);

		int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			angle = Float.parseFloat(angleField.getText());
			passes = Integer.parseInt(passesField.getText());
			if(passes<=2) passes=2;
			
			return convertNow(img,out);
		}
		return false;
	}
	
	protected boolean convertNow(TransformedImage img,Writer out) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);

		double majorX = Math.cos(Math.toRadians(angle));
		double majorY = Math.sin(Math.toRadians(angle));

		// Set up the conversion from image space to paper space, select the current tool, etc.
		imageStart(out);
		tool = machine.getCurrentTool();
		liftPen(out);
		tool.writeChangeTo(out);

		// figure out how many lines we're going to have on this image.
		float steps = tool.getDiameter();
		if (steps < 1) steps = 1;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).
		double level = 255.0 / (double)(passes+1);

		// from top to bottom of the margin area...
		double yBottom = machine.getPaperBottom() * machine.getPaperMargin() * 10;
		double yTop    = machine.getPaperTop()    * machine.getPaperMargin() * 10;
		double xLeft   = machine.getPaperLeft()   * machine.getPaperMargin() * 10;
		double xRight  = machine.getPaperRight()  * machine.getPaperMargin() * 10;
		double dy = yTop - yBottom;
		double dx = xRight - xLeft;
		double radius = Math.sqrt(dx*dx+dy*dy);
		double r2     = radius*2;

		int i=0;
		for(double a = -radius;a<radius;a+=steps) {
			double majorPX = majorX * a;
			double majorPY = majorY * a;
			double startPX = majorPX - majorY * radius;
			double startPY = majorPY + majorX * radius;
			double endPX   = majorPX + majorY * radius;
			double endPY   = majorPY - majorX * radius;
		
			double l2 = level * (1 + (i % passes));
			if ((i % 2) == 0) {
				convertAlongLine(startPX,startPY,endPX,endPY,steps,r2,l2,img,out);
			} else {
				convertAlongLine(endPX,endPY,startPX,startPY,steps,r2,l2,img,out);
			}
			++i;
		}

	    lineTo(out, machine.getHomeX(), machine.getHomeY(), true);
	    
		return true;
	}
	
	protected void convertAlongLine(double x0,double y0,double x1,double y1,double stepSize,double r2,double level,TransformedImage img,Writer out) throws IOException {
		double b;
		double dx=x1-x0;
		double dy=y1-y0;
		double halfStep = stepSize/2;
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
			lineTo(out, x, y, v>=level);
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
