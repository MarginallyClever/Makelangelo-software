package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.donatello.select.SelectOneOfMany;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.geom.Rectangle2D;


/**
 * straight lines pulsing like a heartbeat.  height and density of pulse vary with image intensity.
 * @author Dan Royer
 */
public class Converter_Pulse extends ImageConverter {
	private static double blockScale = 6.0f;
	private static int direction = 0;
	private final String[] directionChoices = new String[]{Translator.get("horizontal"), Translator.get("vertical") };
	private int cutOff = 16;

	public Converter_Pulse() {
		super();

		SelectDouble    selectSize = new SelectDouble("size",Translator.get("HilbertCurveSize"),getScale());
		SelectOneOfMany selectDirection = new SelectOneOfMany("direction",Translator.get("Direction"),getDirections(),getDirectionIndex());
		SelectSlider    selectCutoff = new SelectSlider("cutoff",Translator.get("Converter_VoronoiStippling.Cutoff"),255,0,getCutoff());

		add(selectSize);
		add(selectDirection);
		add(selectCutoff);

		selectSize.addSelectListener(evt->{
			setScale((double) evt.getNewValue());
			fireRestart();
		});
		selectDirection.addSelectListener(evt->{
			setDirectionIndex((int) evt.getNewValue());
			fireRestart();
		});
		selectCutoff.addSelectListener(evt->{
			setCutoff((int) evt.getNewValue());
			fireRestart();
		});
	}

	@Override
	public String getName() {
		return Translator.get("PulseLineName");
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
	
	protected void convertLine(TransformedImage img, double zigZagSpacing, double halfStep, Point2d a, Point2d b) {
		var dir = new Vector2d(b.x-a.x,b.y-a.y);
		double len = dir.length();
		dir.scale(1.0/len);
		Point2d ortho = new Point2d(-dir.y,dir.x);

		double cx = myPaper.getCenterX();
		double cy = myPaper.getCenterY();
		turtle.jumpTo(
				cx+a.x + ortho.x*halfStep,
				cy+a.y + ortho.y*halfStep
		);

		int n=1;
		for (double p = 0; p <= len; p += zigZagSpacing) {
			double x = a.x + dir.x * p; 
			double y = a.y + dir.y * p; 
			// read a block of the image and find the average intensity in this block
			double z = 255.0f - img.sample( x, y, halfStep);
			// scale the intensity value
			double scale_z = (z) / 255.0f;
			//scale_z *= scale_z;  // quadratic curve
			double pulseSize = halfStep * scale_z;

			double px=x + ortho.x * pulseSize * n;
			double py=y + ortho.y * pulseSize * n;
			if(z>cutOff) turtle.moveTo(cx+px,cy+py);
			else turtle.jumpTo(cx+px,cy+py);
			n = -n;
		}
	}

	/**
	 * Converts images into zigzags in paper space instead of image space
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();

		convertOneLayer(img);

		fireConversionFinished();
	}

	protected void convertOneLayer(TransformedImage img) {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xLeft   = rect.getMinX();
		double yBottom = rect.getMinY();
		double xRight  = rect.getMaxX();
		double yTop    = rect.getMaxY();
		
		// figure out how many lines we're going to have on this image.
		double stepSize = blockScale;
		double halfStep = stepSize / 2.0f;
		double zigZagSpacing = 1;

		// from top to bottom of the image...
		double x, y = 0;
		int i=0;

		Point2d a = new Point2d();
		Point2d b = new Point2d();
		
		turtle = new Turtle();
		
		if (direction == 0) {
			// horizontal
			for (y = yBottom; y < yTop; y += stepSize) {
				++i;

				if ((i % 2) == 0) {
					a.set(xLeft,y);
					b.set(xRight,y);
					convertLine(img,zigZagSpacing,halfStep,a,b);
				} else {
					a.set(xRight,y);
					b.set(xLeft,y);
					convertLine(img,zigZagSpacing,halfStep,a,b);
				}
			}
		} else {
			// vertical
			for (x = xLeft; x < xRight; x += stepSize) {
				++i;

				if ((i % 2) == 0) {
					a.set(x,yBottom);
					b.set(x,yTop);
					convertLine(img,zigZagSpacing,halfStep,a,b);
				} else {
					a.set(x,yTop);
					b.set(x,yBottom);
					convertLine(img,zigZagSpacing,halfStep,a,b);
				}
			}
		}
	}

    public int getCutoff() {
		return cutOff;
    }

	public void setCutoff(int value) {
		cutOff=value;
	}
}
