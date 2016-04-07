package com.marginallyclever.converters;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.filters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.Translator;


/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_Crosshatch extends ImageConverter {
	private double xStart, yStart;
	private double xEnd, yEnd;
	private double paperWidth, paperHeight;

	@Override
	public String getName() {
		return Translator.get("Crosshatch");
	}


	/**
	 * The main entry point
	 *
	 * @param img the image to convert.
	 */
	public boolean convert(BufferedImage img,Writer out) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);

		imageStart(img, out);

		// set absolute coordinates
		out.write("G00 G90;\n");
		tool.writeChangeTo(out);
		liftPen(out);

		convertPaperSpace(img, out);

		liftPen(out);

		return true;
	}

	protected int sampleScale(BufferedImage img, double x0, double y0, double x1, double y1) {
		return sample(img,
				(x0 - xStart) / (xEnd - xStart) * (double) imageWidth,
				(double) imageHeight - (y1 - yStart) / (yEnd - yStart) * (double) imageHeight,
				(x1 - xStart) / (xEnd - xStart) * (double) imageWidth,
				(double) imageHeight - (y0 - yStart) / (yEnd - yStart) * (double) imageHeight
				);
	}

	protected void moveToPaper(Writer out, double x, double y, boolean up) throws IOException {
		if(lastUp != up) {
			tool.writeMoveTo(out, (float) x, (float) y);
			if (up) liftPen(out);
			else lowerPen(out);
			lastUp = up;
		}
	}

	protected void convertAlongLine(BufferedImage img, Writer out,double x1,double y1,double x2,double y2,double stepSize, double level) throws IOException {
		double dx = x2-x1;
		double dy = y2-y1;
		
		double len = Math.sqrt(dx*dx+dy*dy);
		double steps;
		
		if(len>0) steps = Math.ceil(len/stepSize);
		else steps=1;
		
		double halfStep = stepSize/2.0;
		double px,py;
		int v;
		
		for(double i=0;i<=steps;++i) {
			px = x1 + dx * (i/steps);
			py = y1 + dy * (i/steps);
			if( px>=xStart && px <xEnd && py>=yStart && py<yEnd ) {
				v = sampleScale(img, px - halfStep, py - halfStep, px + halfStep, py + halfStep);
			} else {
				v=255;
			}
			moveToPaper(out, px, py, v >= level);
		}
	}
	
	protected void convertPaperSpace(BufferedImage img, Writer out) throws IOException {
		double leveladd = 255.0 / 6.0;
		double level = leveladd;

		// if the image were projected on the paper, where would the top left corner of the image be in paper space?
		// image(0,0) is (-paperWidth/2,-paperHeight/2)*paperMargin

		paperWidth = machine.getPaperWidth();
		paperHeight = machine.getPaperHeight();

		xStart = -paperWidth / 2.0;
		yStart = xStart * (double) imageHeight / (double) imageWidth;

		if (yStart < -(paperHeight / 2.0)) {
			xStart *= (-(paperHeight / 2.0)) / yStart;
			yStart = -(paperHeight / 2.0);
		}

		xStart *= 10.0 * machine.getPaperMargin();
		yStart *= 10.0 * machine.getPaperMargin();
		xEnd = -xStart;
		yEnd = -yStart;

		previousX = 0;
		previousY = 0;

		double stepSize = tool.getDiameter() * 3.0;
		double x, y;
		boolean flip=true;

		// vertical
		for (y = yStart; y <= yEnd; y += stepSize) {
			if(flip) {
				moveToPaper(out, xStart, y, true);
				convertAlongLine(img,out,xStart,y,xEnd,y,stepSize,level);
				moveToPaper(out, xEnd, y, true);
			} else {
				moveToPaper(out, xEnd, y, true);
				convertAlongLine(img,out,xEnd,y,xStart,y,stepSize,level);
				moveToPaper(out, xStart, y, true);
			}
			flip = !flip;
		}

		level += leveladd;
		
		// horizontal
		for (x = xStart; x <= xEnd; x += stepSize) {
			if(flip) {
				moveToPaper(out, x, yStart, true);
				convertAlongLine(img,out,x,yStart,x,yEnd,stepSize,level);
				moveToPaper(out, x, yEnd, true);
			} else {
				moveToPaper(out, x, yEnd, true);
				convertAlongLine(img,out,x,yEnd,x,yStart,stepSize,level);
				moveToPaper(out, x, yStart, true);
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
				moveToPaper(out, x3, y3, true);
				convertAlongLine(img,out,x3,y3,x4,y4,stepSize,level);
				moveToPaper(out, x4, y4, true);
			} else {
				moveToPaper(out, x4, y4, true);
				convertAlongLine(img,out,x4,y4,x3,y3,stepSize,level);
				moveToPaper(out, x3, y3, true);
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
				moveToPaper(out, x3, y3, true);
				convertAlongLine(img,out,x3,y3,x4,y4,stepSize,level);
				moveToPaper(out, x4, y4, true);
			} else {
				moveToPaper(out, x4, y4, true);
				convertAlongLine(img,out,x4,y4,x3,y3,stepSize,level);
				moveToPaper(out, x3, y3, true);
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
