package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imageFilter.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.beans.PropertyChangeEvent;


/**
 * 
 * @author Dan Royer
 */
public class Converter_Pulse extends ImageConverter {
	private static double blockScale = 6.0f;
	private static int direction = 0;
	private String[] directionChoices = new String[]{Translator.get("horizontal"), Translator.get("vertical") };
	private int cutOff = 16;

	@Override
	public String getName() {
		return Translator.get("PulseLineName");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("size")) setScale((double)evt.getNewValue());
		if(evt.getPropertyName().equals("direction")) setDirectionIndex((int)evt.getNewValue());
		if(evt.getPropertyName().equals("cutoff")) setCutoff((int)evt.getNewValue());
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
	
	protected void convertLine(TransformedImage img, double zigZagSpacing, double halfStep, Point2D a, Point2D b) {
		Point2D dir = new Point2D(b.x-a.x,b.y-a.y);
		double len = dir.length();
		dir.scale(1/len);
		Point2D ortho = new Point2D(-dir.y,dir.x);
		
		turtle.jumpTo(
			a.x + ortho.x*halfStep,
			a.y + ortho.y*halfStep
		);

		int n=1;
		for (double p = 0; p <= len; p += zigZagSpacing) {
			double x = a.x + dir.x * p; 
			double y = a.y + dir.y * p; 
			// read a block of the image and find the average intensity in this block
			double z = 255.0f - img.sample( x - zigZagSpacing, y - halfStep, x + zigZagSpacing, y + halfStep);
			// scale the intensity value
			double scale_z = (z) / 255.0f;
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
	public void finish() {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(myImage);
		
		double yBottom = myPaper.getMarginBottom();
		double yTop    = myPaper.getMarginTop()   ;
		double xLeft   = myPaper.getMarginLeft()  ;
		double xRight  = myPaper.getMarginRight() ;
		
		// figure out how many lines we're going to have on this image.
		double stepSize = blockScale;
		double halfStep = stepSize / 2.0f;
		double zigZagSpacing = 1;
		double spaceBetweenLines = stepSize;

		// from top to bottom of the image...
		double x, y = 0;
		int i=0;

		Point2D a = new Point2D();
		Point2D b = new Point2D();
		
		turtle = new Turtle();
		
		if (direction == 0) {
			// horizontal
			for (y = yBottom; y < yTop; y += spaceBetweenLines) {
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
			for (x = xLeft; x < xRight; x += spaceBetweenLines) {
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
