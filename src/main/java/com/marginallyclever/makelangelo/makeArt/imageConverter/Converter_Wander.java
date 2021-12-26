package com.marginallyclever.makelangelo.makeArt.imageConverter;


import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import com.marginallyclever.makelangelo.makeArt.imageFilter.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.makeArt.imageFilter.Filter_CMYK;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.util.LinkedList;


/**
 * create random lines across the image.  Raise and lower the pen to darken the appropriate areas
 * @author Dan Royer
 */
public class Converter_Wander extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_Wander.class);
	
	static protected int numLines = 9000;
	static protected boolean isCMYK = false;
	
	class Bucket {
		public Point2D a,b;
		public LinkedList<Point2D> unsortedPoints;
		public LinkedList<Point2D> sortedPoints;
		
		public Bucket() {
			a = new Point2D.Double();
			b = new Point2D.Double();
			unsortedPoints = new LinkedList<Point2D>();
			sortedPoints = new LinkedList<Point2D>();
		}
	};

	private LinkedList<Bucket> buckets;
	
	
	@Override
	public String getName() {
		return Translator.get("ConverterWanderName");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("count")) setLineCount((int)evt.getNewValue());
		if(evt.getPropertyName().equals("cmyk")) setCMYK((boolean)evt.getNewValue());
	}

	@Override
	public void finish() {
		if(isCMYK) {
			finishCMYK();
		} else {
			finishBlackAndWhite();
		}
	}

	protected int outputChannel(TransformedImage img,ColorRGB newColor,int pointsPerChannel,double cutoff) {
		double stepSize = 5.0;
		if (stepSize < 1) stepSize = 1;
		double halfStep = stepSize/2;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > cutoff.

		// from top to bottom of the margin area...
		double yBottom = myPaper.getMarginBottom();
		double yTop    = myPaper.getMarginTop()   ;
		double xLeft   = myPaper.getMarginLeft()  ;
		double xRight  = myPaper.getMarginRight() ;

		// find numLines number of random points darker than the cutoff value
		double height = yTop - yBottom-1;
		double width = xRight - xLeft-1;
		Point2D a = null;
		
		//logger.debug("Creating buckets in a Z pattern...");
		buckets = new LinkedList<Bucket>();
		int actualPoints=0;
		double wMod = width/5.0;
		double hMod = height/10.0;
		double by,bx;
		for(by=0;by<height;by+=hMod) {
			for(bx=0;bx<width;bx+=wMod) {
				Bucket b = new Bucket();
				b.a.setLocation(xLeft+bx     , yBottom+by     );
				b.b.setLocation(xLeft+bx+wMod, yBottom+by+hMod);
				buckets.push(b);
			}
			by+=hMod;
			for(bx=width-wMod;bx>=-1;bx-=wMod) {
				Bucket b = new Bucket();
				b.a.setLocation(xLeft+bx     , yBottom+by     );
				b.b.setLocation(xLeft+bx+wMod, yBottom+by+hMod);
				buckets.push(b);
			}
		}

		//logger.debug("Finding points...");
		for(int i=0;i<pointsPerChannel;++i) {
			int v, tries=0;
			double endPX,endPY; 
			do {
				endPX = xLeft   + (Math.random() * width)+0.5; 
				endPY = yBottom + (Math.random() * height)+0.5; 
				v = img.sample(
						endPX - halfStep, endPY - halfStep, 
						endPX + halfStep, endPY + halfStep);
				++tries;
			} while(v>cutoff && tries<1000);
			if(tries==1000) break;  // ran out of points to try?

			int j;
			for(j=0;j<buckets.size();++j) {
				Bucket b = buckets.get(j);
				if( b.a.getX()<=endPX && b.b.getX()>endPX && 
				    b.a.getY()<=endPY && b.b.getY()>endPY ) {
					b.unsortedPoints.addLast(new Point2D.Double(endPX,endPY));
					++actualPoints;
					break;
				}
			}
		}

		// sort the points by nearest neighbor first.
		logger.debug("Sorting {} points...", actualPoints);
		for(int j=0;j<buckets.size();++j) {
			//logger.debug(j+" of "+buckets.size()+ " has "+buckets.get(j).unsortedPoints.size()+" points");

			// assume we start at the center of the image, for those machines with no pen up option.
			a = new Point2D.Double(0,0);
			
			Bucket b = buckets.get(j);
			if(!b.unsortedPoints.isEmpty()) {
				while(!b.unsortedPoints.isEmpty()) {
					double bestLen = Double.MAX_VALUE;
					int bestI=0;
					for(int i=0;i<b.unsortedPoints.size();++i) {
						double len = a.distanceSq(b.unsortedPoints.get(i));
						if(bestLen > len) {
							bestLen = len;
							bestI = i;
						}
					}
					a = b.unsortedPoints.remove(bestI);
					b.sortedPoints.addLast(a);
				}
			}
		}
		
		
		// draw the sorted list of points.
		logger.debug("Drawing points...");	
		turtle.setColor(newColor);
	
		for(int j=0;j<buckets.size();++j) {
			Bucket b = buckets.get(j);
			while(!b.sortedPoints.isEmpty()) {
				a = b.sortedPoints.pop();
				turtle.moveTo(a.getX(),a.getY());
				turtle.penDown();
			}
		}
		
		return actualPoints;
	}
	
	protected void finishCMYK() {
		Filter_CMYK cmyk = new Filter_CMYK();
		cmyk.filter(myImage);
		
		turtle = new Turtle();
		
		logger.debug("Yellow...");		outputChannel(cmyk.getY(),new ColorRGB(255,255,  0),numLines,255.0*3.0/4.0);
		logger.debug("Cyan...");			outputChannel(cmyk.getC(),new ColorRGB(  0,255,255),numLines,128.0);
		logger.debug("Magenta...");		outputChannel(cmyk.getM(),new ColorRGB(255,  0,255),numLines,128.0);
		logger.debug("Black...");		outputChannel(cmyk.getK(),new ColorRGB(  0,  0,  0),numLines,128.0);
		logger.debug("Finishing...");
	}
	
	protected void finishBlackAndWhite() {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(myImage);
		
		outputChannel(img,new ColorRGB(0,0,0),numLines,255.0/4.0);
	}
	

	public int getLineCount() {
		return numLines;
	}

	public void setLineCount(int value) {
		if(value<1) value=1;
		numLines = value;
	}
	
	public boolean isCMYK() {
		return isCMYK;
	}
	
	public void setCMYK(boolean arg0) {
		isCMYK = arg0;
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
