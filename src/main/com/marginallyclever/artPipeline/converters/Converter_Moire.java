package com.marginallyclever.artPipeline.converters;

import java.util.ArrayList;
import java.util.ListIterator;

import com.marginallyclever.artPipeline.TransformedImage;
import com.marginallyclever.artPipeline.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.convenience.LineInterpolator;
import com.marginallyclever.convenience.LineInterpolatorSinCurve;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;


public class Converter_Moire extends ImageConverter {
	private static float blockScale = 4.0f;
	private static int direction = 0;
	private String[] directionChoices = new String[]{Translator.get("horizontal"), Translator.get("vertical") }; 
	
	
	@Override
	public String getName() {
		return Translator.get("MoireName");
	}

	@Override
	public ImageConverterPanel getPanel() {
		return new Converter_Moire_Panel(this);
	}
	
	public float getScale() {
		return blockScale;
	}
	public void setScale(float value) {
		if(value<1) value=1;
		blockScale = value;
	}
	public String[] getDirections() {
		return directionChoices;
	}
	public int getDirectionIndex() {
		return direction;
	}
	public void setDirectionIndex(int value) {
		if(value<0) value=0;
		if(value>=directionChoices.length) value=directionChoices.length-1;
		direction = value;
	}

	
	protected void convertLine(TransformedImage img,float spaceBetweenLines,float halfStep,Point2D a,Point2D b) {
		LineInterpolatorSinCurve line = new LineInterpolatorSinCurve(a,b);
		line.setAmplitude(0.4);
		
		double CUTOFF = 1.0/255.0;
		double iterStepSize = 0.002;//machine.getPenDiameter()/2;
		
		// examine the line once.  all Z values will be in the range 0...1
		ArrayList<Double> zList = new ArrayList<Double>();
		
		Point2D p = new Point2D();
		//Point2D n = new Point2D();
		double maxPixel=0;
		
		
		for (double t = 0; t <= 1.0; t += iterStepSize) {
			line.getPoint(t, p);
			// read a block of the image and find the average intensity in this block
			double pixel = img.sample( p.x, p.y, halfStep*2 );
			// scale the intensity value
			double pixelNormalized = (255.0f - pixel) / 255.0f;
			pixelNormalized = Math.max(Math.min(pixelNormalized,1), 0);
			zList.add(pixelNormalized);
			if(maxPixel<pixelNormalized) maxPixel=pixelNormalized;
		}
		
		// find the maximum number of passes for any given line
		double pd = machine.getPenDiameter()*0.7;
		int maxPasses = (int)Math.floor( spaceBetweenLines / pd )-1;
		// adjust to the maximum number used in *this* line.
		int passesThisLine = (int)(maxPasses * maxPixel);

		//Log.message(passesThisLine+"/"+maxPasses);
		
		if(passesThisLine==0) return;  // empty line!

		int ziMeta = 0;
		int ziStart = -1;
		int ziEnd = -1;

		ListIterator<Double> zi = zList.listIterator(ziMeta);
		while(zi.hasNext()) {
			double z = zi.next();
			ziMeta++;
			if(ziStart == -1) {
				// is this the start of a segment?
				if(z>CUTOFF) {
					// yes
					ziStart = ziMeta;
					continue;
				}
			} else {
				// is this the end of a segment? (either image light enough OR end of the line)
				if(z<=CUTOFF || !zi.hasNext()) {
					// yes
					ziEnd = ziMeta;
					if(!zi.hasNext()) ziEnd--;
					
					// now draw the segment.
					// find the number of passes in this segment
					ListIterator<Double> zi2 = zList.listIterator(ziStart);
					maxPixel=0;
					for(int zc=ziStart; zc<ziEnd; ++zc) {
						z = zi2.next();
						if(maxPixel<z) maxPixel=z;
					}
					int passesThisSegment = (int)(maxPasses * maxPixel);
					if(passesThisSegment>0) {
						// jump to the start of the segment
						double t = ziStart * iterStepSize;
						line.getPoint(t, p);
						double x=p.x;
						double y=p.y;
						turtle.jumpTo(x,y);
						
						// draw back and forth over the segment, each line a little offset from the one before.
						double halfSpace = pd*(double)passesThisSegment/2.0;
						int direction=1;
						
						for(int k=0;k<passesThisSegment;++k) {
							double maxPulseNow = pd * k - halfSpace;

							int zc=0;
							if(direction==1) {
								zi2 = zList.listIterator(ziStart);
								for(zc=ziStart; zc<ziEnd; ++zc) {
									z = zi2.next();
									lineInternal(maxPulseNow,z,line,zc*iterStepSize);
								}
							} else {
								zi2 = zList.listIterator(ziEnd);
								for(zc=ziEnd-1; zc>=ziStart; --zc) {
									z = zi2.previous();
									lineInternal(maxPulseNow,z,line,zc*iterStepSize);
								}
							}
							direction=-direction;
						}
					}
					// reset to go again.
					ziStart = -1;
				}
			}
		}
	}
	
	protected void lineInternal(double maxPulseNow,double z,LineInterpolator line,double t) {
		double pulseSize = maxPulseNow * z;
		Point2D p = new Point2D();
		Point2D n = new Point2D();
		line.getPoint(t, p);
		line.getNormal(t, n);
		double x=p.x + n.x*pulseSize;
		double y=p.y + n.y*pulseSize;
		turtle.moveTo(x,y);
	}

	@Override
	public void finish() {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(sourceImage);
		
		double yBottom = machine.getMarginBottom();
		double yTop    = machine.getMarginTop();
		double xLeft   = machine.getMarginLeft();
		double xRight  = machine.getMarginRight();

		double h=yTop-yBottom;
		double w=xRight-xLeft;
		
		// figure out how many lines we're going to have on this image.
		float halfStep = machine.getPenDiameter();
		float spaceBetweenLines = blockScale;

		// from top to bottom of the image...
		Point2D a = new Point2D();
		Point2D b = new Point2D();
		
		turtle = new Turtle();
		
		Log.message("Moire start");
		if (direction == 0) {
			// horizontal
			yBottom -= h;
			yTop    += h;
			for (double y = yBottom; y < yTop; y += spaceBetweenLines) {
				a.set(xRight,y);
				b.set(xLeft,y);
				convertLine(img,spaceBetweenLines,halfStep,a,b);
			}
		} else {
			// vertical
			xLeft  -= w;
			xRight += w;
			for (double x = xLeft; x < xRight; x += spaceBetweenLines) {
				a.set(x,yTop);
				b.set(x,yBottom);
				convertLine(img,spaceBetweenLines,halfStep,a,b);
			}
		}
		Log.message("Moire end");
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
