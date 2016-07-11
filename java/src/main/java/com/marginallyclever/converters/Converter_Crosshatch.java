package com.marginallyclever.converters;


import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.basictypes.TransformedImage;
import com.marginallyclever.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.Translator;


/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_Crosshatch extends ImageConverter {
	private double xStart, yStart, xEnd, yEnd;

	@Override
	public String getName() {
		return Translator.get("Crosshatch");
	}


	/**
	 * The main entry point
	 *
	 * @param img the image to convert.
	 */
	public boolean convert(TransformedImage img,Writer out) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);

		imageStart(out);
		tool = machine.getCurrentTool();
		liftPen(out);
		tool.writeChangeTo(out);
		
		convertPaperSpace(img, out);
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);

		return true;
	}


	protected void convertAlongLine(TransformedImage img, Writer out,double x1,double y1,double x2,double y2,double stepSize, double level) throws IOException {
		double dx = x2-x1;
		double dy = y2-y1;
		
		double len = Math.sqrt(dx*dx+dy*dy);
		double steps;
		
		if(len>0) steps = Math.ceil(len/stepSize);
		else steps=1;
		
		double halfStep = stepSize/2.0;
		float px,py;
		int v;
		
		lineTo(out, x1, y1, true);
		
		for(float i=0;i<=steps;++i) {
			px = (float)(x1 + dx * (i/steps));
			py = (float)(y1 + dy * (i/steps));
			if( isInsidePaperMargins(px, py)) {
				v = img.sample( px - halfStep, py - halfStep, px + halfStep, py + halfStep);
			} else {
				v=255;
			}
			lineTo(out, px, py, v >= level);
		}

		lineTo(out, x2, y2, true);
	}
	
	protected void convertPaperSpace(TransformedImage img, Writer out) throws IOException {
		double leveladd = 255.0 / 6.0;
		double level = leveladd;

		// if the image were projected on the paper, where would the top left corner of the image be in paper space?
		// image(0,0) is (-paperWidth/2,-paperHeight/2)*paperMargin

		yStart = (float)machine.getPaperBottom() * (float)machine.getPaperMargin() * 10;
		yEnd   = (float)machine.getPaperTop()    * (float)machine.getPaperMargin() * 10;
		xStart = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin() * 10;
		xEnd   = (float)machine.getPaperRight()  * (float)machine.getPaperMargin() * 10;
		previousX = 0;
		previousY = 0;

		double stepSize = tool.getDiameter() * 3.0;
		double x, y;
		boolean flip=true;

		// vertical
		for (y = yStart; y <= yEnd; y += stepSize) {
			if(flip) {
				convertAlongLine(img,out,xStart,y,xEnd,y,stepSize,level);
			} else {
				convertAlongLine(img,out,xEnd,y,xStart,y,stepSize,level);
			}
			flip = !flip;
		}

		level += leveladd;
		
		// horizontal
		for (x = xStart; x <= xEnd; x += stepSize) {
			if(flip) {
				convertAlongLine(img,out,x,yStart,x,yEnd,stepSize,level);
			} else {
				convertAlongLine(img,out,x,yEnd,x,yStart,stepSize,level);
			}
			flip = !flip;
		}

		level += leveladd;
		
		// diagonal 1
		double dy = yEnd-yStart;
		double dx = xEnd-xStart;
		double len = dx > dy? dx:dy;

		double x1 = -len;
		double y1 = -len;
		
		double x2 = +len;
		double y2 = +len;

		double len2 = Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
		double steps;
		if(len2>0) steps = len2/stepSize;
		else steps=1;
		double i;
		
		for(i=0;i<steps;++i) {
			double px = x1+(x2-x1)*(i/steps);
			double py = y1+(y2-y1)*(i/steps);
			
			double x3 = px-len;
			double y3 = py+len;
			double x4 = px+len;
			double y4 = py-len;

			if(flip) {
				convertAlongLine(img,out,x3,y3,x4,y4,stepSize,level);
			} else {
				convertAlongLine(img,out,x4,y4,x3,y3,stepSize,level);
			}
			flip = !flip;
		}
		
		level += leveladd;

		// diagonal 2

		x1 = +len;
		y1 = -len;
		
		x2 = -len;
		y2 = +len;

		for(i=0;i<steps;++i) {
			double px = x1+(x2-x1)*(i/steps);
			double py = y1+(y2-y1)*(i/steps);
			
			double x3 = px+len;
			double y3 = py+len;
			double x4 = px-len;
			double y4 = py-len;

			if(flip) {
				convertAlongLine(img,out,x3,y3,x4,y4,stepSize,level);
			} else {
				convertAlongLine(img,out,x4,y4,x3,y3,stepSize,level);
			}
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
