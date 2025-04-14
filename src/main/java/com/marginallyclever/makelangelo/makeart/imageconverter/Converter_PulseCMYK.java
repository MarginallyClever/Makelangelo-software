package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterCMYK;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.donatello.select.SelectOneOfMany;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * straight lines pulsing like a heartbeat.  height and density of pulse vary with image intensity.
 * @author Dan Royer
 */
public class Converter_PulseCMYK extends ImageConverter {
	private static double blockScale = 4.0f;
	private static int direction = 0;
	private final String[] directionChoices = new String[]{Translator.get("horizontal"), Translator.get("vertical") };
	private int cutOff = 16;
	private double sampleRate = 0.2;

	public Converter_PulseCMYK() {
		super();

		SelectDouble    selectSize = new SelectDouble("size",Translator.get("HilbertCurveSize"),getScale());
		SelectOneOfMany selectDirection = new SelectOneOfMany("direction",Translator.get("Direction"),getDirections(),getDirectionIndex());
		SelectSlider    selectCutoff = new SelectSlider("cutoff",Translator.get("Converter_VoronoiStippling.Cutoff"),255,0,getCutoff());
		SelectDouble    selectSampleRate = new SelectDouble("sampleRate",Translator.get("Converter_PulseCMYK.SampleRate"),sampleRate);

		add(selectSize);
		add(selectDirection);
		add(selectCutoff);
		add(selectSampleRate);

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
		selectSampleRate.addSelectListener(evt->{
			sampleRate = (double) evt.getNewValue();
			fireRestart();
		});
	}

	@Override
	public String getName() {
		return Translator.get("Converter_PulseCMYK.Name");
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

	/**
	 * Convert a line of the image into a pulse line.
	 * @param img the source image to sample from
	 * @param halfLineHeight the width of the pulse line.
	 * @param a the start of the line
	 * @param b the end of the line
	 */
	protected void convertLine(TransformedImage img, double halfLineHeight, Point2d a, Point2d b) {
		var normal = new Vector2d(b.x-a.x,b.y-a.y);
		double len = normal.length();
		normal.scale(1.0/len);

		Point2d orthogonal = new Point2d(-normal.y,normal.x);

		double cx = myPaper.getCenterX();
		double cy = myPaper.getCenterY();
		turtle.jumpTo(
				cx+a.x + orthogonal.x*halfLineHeight,
				cy+a.y + orthogonal.y*halfLineHeight
		);

		double sum=0;
		//double previous=0;
		for (double p = 0; p <= len; p += sampleRate*2) {
			double x = a.x + normal.x * p;
			double y = a.y + normal.y * p;
			// read a block of the image and find the average intensity in this block
			double z = (255.0f - img.sample( x, y, sampleRate));

			// if the is too high, the sum will refuse to update.
			if(z<cutOff) {
				// the image intensity controls the rate of change.
				sum += z/255.0 * Math.PI * 0.5;
			}

			// the sum controls the height of the pulse.
			var h = Math.cos(sum) * halfLineHeight;
			double px = cx + x + orthogonal.x * h;
			double py = cy + y + orthogonal.y * h;
			turtle.moveTo(px,py);
		}
	}

	/**
	 * Converts images into zigzags in paper space instead of image space
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterCMYK cmyk = new FilterCMYK(myImage);
		cmyk.filter();

		turtle = new Turtle();

		outputChannel(cmyk.getY(),Color.YELLOW);
		outputChannel(cmyk.getC(),Color.CYAN);
		outputChannel(cmyk.getM(),Color.MAGENTA);
		outputChannel(cmyk.getK(),Color.BLACK);

		fireConversionFinished();
	}

	protected void outputChannel(TransformedImage img,Color channel) {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xLeft   = rect.getMinX();
		double yBottom = rect.getMinY();
		double xRight  = rect.getMaxX();
		double yTop    = rect.getMaxY();
		
		// figure out how many lines we're going to have on this image.
		double lineHeight = blockScale;
		double halfLineHeight = lineHeight / 2.0f;

		// from top to bottom of the image...
		double x, y = 0;
		int i=0;

		Point2d a = new Point2d();
		Point2d b = new Point2d();

		turtle.setStroke(channel);
		
		if (direction == 0) {
			// horizontal
			for (y = yBottom; y < yTop; y += lineHeight) {
				++i;

				if ((i % 2) == 0) {
					a.set(xLeft,y);
					b.set(xRight,y);
					convertLine(img,halfLineHeight,a,b);
				} else {
					a.set(xRight,y);
					b.set(xLeft,y);
					convertLine(img,halfLineHeight,a,b);
				}
			}
		} else {
			// vertical
			for (x = xLeft; x < xRight; x += lineHeight) {
				++i;

				if ((i % 2) == 0) {
					a.set(x,yBottom);
					b.set(x,yTop);
					convertLine(img,halfLineHeight,a,b);
				} else {
					a.set(x,yTop);
					b.set(x,yBottom);
					convertLine(img,halfLineHeight,a,b);
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
