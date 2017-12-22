package com.marginallyclever.makelangeloRobot.converters;


import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

import javax.swing.JPanel;
import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_BlackAndWhite;


// create random lines across the image.  Raise and lower the pen to darken the appropriate areas
public class Converter_Wander extends ImageConverter {
	static protected int numLines = 9000;
	
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
		machine.writeChangeToDefaultColor(out);

		float stepSize = machine.getPenDiameter()*5;
		if (stepSize < 1) stepSize = 1;
		float halfStep = stepSize/2;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).
		double level = 255.0 / 4.0;

		// from top to bottom of the margin area...
		float yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin();
		float yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin();
		float xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin();
		float xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin();

		// find numLines number of random points darker than the cutoff value
		double height = yTop - yBottom-1;
		double width = xRight - xLeft-1;
		Point2D a = null;
		
		Log.info("Creating buckets in a Z pattern...");
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

		Log.info("Finding points...");
		for(int i=0;i<numLines;++i) {
			level = 55 + 50.0 * (double)i / (double)numLines;  // set the cutoff value
			int v, tries=0;
			double endPX,endPY; 
			do {
				endPX = xLeft   + (Math.random() * width)+0.5; 
				endPY = yBottom + (Math.random() * height)+0.5; 
				v = img.sample(
						endPX - halfStep, endPY - halfStep, 
						endPX + halfStep, endPY + halfStep);
				++tries;
			} while(v>level && tries<1000);
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
		Log.info("Sorting "+actualPoints+" points...");
		for(int j=0;j<buckets.size();++j) {
			Log.info(j+" of "+buckets.size()+ " has "+buckets.get(j).unsortedPoints.size()+" points");

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
		Log.info("Drawing points...");
		liftPen(out);
		boolean isFirst=true;

		for(int j=0;j<buckets.size();++j) {
			Bucket b = buckets.get(j);
			while(!b.sortedPoints.isEmpty()) {
				a = b.sortedPoints.pop();
				moveTo(out,a.getX(),a.getY(),isFirst);
				isFirst=false;
			}
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
