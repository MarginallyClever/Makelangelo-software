package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.LineInterpolator;
import com.marginallyclever.convenience.LineInterpolatorSinCurve;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.donatello.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.ListIterator;


/**
 * 
 * @author Dan Royer
 */
public class Converter_Moire extends ImageConverter {
	private static double blockScale = 4.0f;
	private static int direction = 0;
	private final String[] directionChoices = new String[]{Translator.get("horizontal"), Translator.get("vertical") };

	public Converter_Moire() {
		super();

		SelectDouble selectSize = new SelectDouble("size",Translator.get("HilbertCurveSize"),getScale());
		SelectOneOfMany selectDirection = new SelectOneOfMany("direction",Translator.get("Direction"),getDirections(),getDirectionIndex());

		add(selectSize);
		add(selectDirection);

		selectSize.addSelectListener(evt->{
			setScale((double)evt.getNewValue());
			fireRestart();
		});
		selectDirection.addSelectListener(evt->{
			setDirectionIndex((int)evt.getNewValue());
			fireRestart();
		});
	}
	
	@Override
	public String getName() {
		return Translator.get("MoireName");
	}

	public double getScale() {
		return blockScale;
	}
	
	public void setScale(double value) {
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

	protected void convertLine(TransformedImage img, double spaceBetweenLines, double halfStep, Point2d a, Point2d b) {
		LineInterpolatorSinCurve line = new LineInterpolatorSinCurve(a,b);
		line.setAmplitude(0.4);
		
		double CUTOFF = 1.0/255.0;
		double iterStepSize = 0.002;//machine.getDouble(PlotterSettings.DIAMETER)/2;
		
		// examine the line once.  all Z values will be in the range 0...1
		ArrayList<Double> zList = new ArrayList<>();
		
		Point2d p = new Point2d();
		//Point2d n = new Point2d();
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
		double pd = 0.7;
		int maxPasses = (int)Math.floor( spaceBetweenLines / pd )-1;
		// adjust to the maximum number used in *this* line.
		int passesThisLine = (int)(maxPasses * maxPixel);

		//logger.debug(passesThisLine+"/"+maxPasses);
		
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
						turtle.jumpTo(myPaper.getCenterX()+x,myPaper.getCenterY()+y);
						
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
		Point2d p = new Point2d();
		Vector2d n = new Vector2d();
		line.getPoint(t, p);
		line.getNormal(t, n);
		double x=myPaper.getCenterX()+p.x + n.x*pulseSize;
		double y=myPaper.getCenterY()+p.y + n.y*pulseSize;
		turtle.moveTo(x,y);
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xLeft   = rect.getMinX();
		double yBottom = rect.getMinY();
		double xRight  = rect.getMaxX();
		double yTop    = rect.getMaxY();

		double h=yTop-yBottom;
		double w=xRight-xLeft;
		
		// figure out how many lines we're going to have on this image.
		double halfStep = 1;
		double spaceBetweenLines = blockScale;

		// from top to bottom of the image...
		Point2d a = new Point2d();
		Point2d b = new Point2d();
		
		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

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

		fireConversionFinished();
	}
}
