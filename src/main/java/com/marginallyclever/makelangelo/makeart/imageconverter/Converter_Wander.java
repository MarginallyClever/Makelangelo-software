package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterCMYK;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;
import java.util.LinkedList;


/**
 * create random lines across the image.  Raise and lower the pen to darken the appropriate areas
 * @author Dan Royer
 */
public class Converter_Wander extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_Wander.class);
	
	static private int numLines = 9000;
	static private boolean isCMYK = false;
	static private double stepSize = 5.0;
	
	private static class Bucket {
		public Point2D a,b;
		public LinkedList<Point2D> unsortedPoints;
		public LinkedList<Point2D> sortedPoints;
		
		public Bucket() {
			a = new Point2D.Double();
			b = new Point2D.Double();
			unsortedPoints = new LinkedList<>();
			sortedPoints = new LinkedList<>();
		}
	}

	public Converter_Wander() {
		super();
		SelectInteger selectCount = new SelectInteger("count",Translator.get("ConverterWanderLineCount"),getLineCount());
		add(selectCount);
		selectCount.addPropertyChangeListener(evt->{
			setLineCount((int)evt.getNewValue());
			fireRestart();
		});
		SelectBoolean selectCmyk = new SelectBoolean("cmyk",Translator.get("ConverterWanderCMYK"),isCMYK());
		add(selectCmyk);
		selectCmyk.addPropertyChangeListener(evt->{
			setCMYK((boolean)evt.getNewValue());
			fireRestart();
		});
	}
	
	@Override
	public String getName() {
		return Translator.get("ConverterWanderName");
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);
		if(isCMYK) {
			finishCMYK();
		} else {
			finishBlackAndWhite();
		}

		fireConversionFinished();
	}

	protected void outputChannel(TransformedImage img, ColorRGB newColor, int pointsPerChannel, double cutoff) {
		stepSize = Math.max(1,stepSize);
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
		
		//logger.debug("Creating buckets in a Z pattern...");
		LinkedList<Bucket> buckets = new LinkedList<>();
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
			for(j=0; j< buckets.size(); ++j) {
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
		for (Bucket bucket : buckets) {
			//logger.debug(j+" of "+buckets.size()+ " has "+buckets.get(j).unsortedPoints.size()+" points");

			// assume we start at the center of the image, for those machines with no pen up option.
			Point2D a = new Point2D.Double(0, 0);

			if (!bucket.unsortedPoints.isEmpty()) {
				while (!bucket.unsortedPoints.isEmpty()) {
					double bestLen = Double.MAX_VALUE;
					int bestI = 0;
					for (int i = 0; i < bucket.unsortedPoints.size(); ++i) {
						double len = a.distanceSq(bucket.unsortedPoints.get(i));
						if (bestLen > len) {
							bestLen = len;
							bestI = i;
						}
					}
					a = bucket.unsortedPoints.remove(bestI);
					bucket.sortedPoints.addLast(a);
				}
			}
		}
		
		
		// draw the sorted list of points.
		logger.debug("Drawing points...");	
		turtle.setColor(newColor);

		for (Bucket bucket : buckets) {
			while (!bucket.sortedPoints.isEmpty()) {
				Point2D a = bucket.sortedPoints.pop();
				turtle.moveTo(a.getX(), a.getY());
				turtle.penDown();
			}
		}
	}
	
	protected void finishCMYK() {
		FilterCMYK cmyk = new FilterCMYK(myImage);
		cmyk.filter();
		
		turtle = new Turtle();
		
		logger.debug("Yellow...");		outputChannel(cmyk.getY(),new ColorRGB(255,255,  0),numLines,255.0*3.0/4.0);
		logger.debug("Cyan...");		outputChannel(cmyk.getC(),new ColorRGB(  0,255,255),numLines,128.0);
		logger.debug("Magenta...");		outputChannel(cmyk.getM(),new ColorRGB(255,  0,255),numLines,128.0);
		logger.debug("Black...");		outputChannel(cmyk.getK(),new ColorRGB(  0,  0,  0),numLines,128.0);
		logger.debug("Finishing...");
	}
	
	protected void finishBlackAndWhite() {
		// The picture might be in color.  Smash it to 255 shades of grey.
		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();
		
		turtle = new Turtle();
		
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
