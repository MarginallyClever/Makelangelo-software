package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.donatello.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.makeart.turtletool.WaveByIntensity;
import com.marginallyclever.makelangelo.paper.Paper;
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

		add(selectSize);
		add(selectDirection);

		selectSize.addSelectListener(evt->{
			setScale((double) evt.getNewValue());
			fireRestart();
		});
		selectDirection.addSelectListener(evt->{
			setDirectionIndex((int) evt.getNewValue());
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

		turtle.jumpTo(a.x,a.y);


		int n=1;
		for (double p = 0; p <= len; p += zigZagSpacing) {
			double x = a.x + dir.x * p; 
			double y = a.y + dir.y * p; 
			// read a block of the image and find the average intensity in this block
			double z = 255.0f - img.sample( x, y, halfStep);
			// scale the intensity value
			double scale_z = z / 255.0;
			//scale_z *= scale_z;  // quadratic curve
			double pulseSize = halfStep * scale_z;

			double px=x + ortho.x * pulseSize * n;
			double py=y + ortho.y * pulseSize * n;
			if(z>cutOff) turtle.moveTo(px,py);
			else turtle.jumpTo(px,py);
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

		// from top to bottom of the image...
		double x, y = 0;
		int i=0;

		Point2d a = new Point2d();
		Point2d b = new Point2d();
		
		turtle = new Turtle();

		var wave = new WaveByIntensity(img,blockScale/2,1);

		if (direction == 0) {
			// horizontal
			for (y = yBottom; y < yTop; y += blockScale) {
				++i;
				if ((i % 2) == 0) {
					a.set(xLeft,y);
					b.set(xRight,y);
				} else {
					a.set(xRight,y);
					b.set(xLeft,y);
				}
				turtle.add(wave.lineToWave(a,b));
			}
		} else {
			// vertical
			for (x = xLeft; x < xRight; x += blockScale) {
				++i;
				if ((i % 2) == 0) {
					a.set(x,yBottom);
					b.set(x,yTop);
				} else {
					a.set(x,yTop);
					b.set(x,yBottom);
				}
				turtle.add(wave.lineToWave(a,b));
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
