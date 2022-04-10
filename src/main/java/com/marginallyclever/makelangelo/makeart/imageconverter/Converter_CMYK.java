package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imageFilter.Filter_CMYK;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;

/**
 * See also http://the-print-guide.blogspot.ca/2009/05/halftone-screen-angles.html
 * @author Dan Royer
 */
public class Converter_CMYK extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_CMYK.class);
	static protected int passes=1;// passes value have to be >=1.
	
	@Override
	public String getName() {
		return Translator.get("ConverterCMYKName");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("passes")) setPasses((int)evt.getNewValue());
	}
	
	public int getPasses() {
		return passes;
	}
	
	/**
	 * Passing a value lower than 1 set passes value to 1.
	 * @param value 
	 */
	public void setPasses(int value) {
		passes = Math.max(1, value);
	}
	
	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void finish() {
		Filter_CMYK cmyk = new Filter_CMYK();
		cmyk.filter(myImage);
		
		turtle = new Turtle();
		// remove extra change color at the start of the turtle
		turtle.history.clear();
		
		logger.debug("Yellow...");		outputChannel(cmyk.getY(),0 ,new ColorRGB(255,255,  0));
		logger.debug("Cyan...");		outputChannel(cmyk.getC(),15,new ColorRGB(  0,255,255));
		logger.debug("Magenta...");		outputChannel(cmyk.getM(),75,new ColorRGB(255,  0,255));
		logger.debug("Black...");		outputChannel(cmyk.getK(),45,new ColorRGB(  0,  0,  0));
	}
	
	protected void outputChannel(TransformedImage img, float angle, ColorRGB newColor) {
		double dx = Math.cos(Math.toRadians(angle));
		double dy = Math.sin(Math.toRadians(angle));
		double [] channelCutoff = {0,153,51,102,204};
		
		turtle.setColor(newColor);

		// figure out how many lines we're going to have on this image.
		double stepSize = passes;

		// from top to bottom of the margin area...
		double height  = myPaper.getMarginTop() - myPaper.getMarginBottom();
		double width   = myPaper.getMarginRight() - myPaper.getMarginLeft();
		double maxLen  = Math.sqrt(width*width+height*height);

		double a;
		int i=0;
		for(a = -maxLen;a<maxLen;a+=stepSize) {
			double px = dx * a;
			double py = dy * a;
			// p0-p1 is at a right angle to dx/dy
			double x0 = px - dy * maxLen;
			double y0 = py + dx * maxLen;
			double x1 = px + dy * maxLen;
			double y1 = py - dx * maxLen;

			double cutoff=channelCutoff[i%channelCutoff.length];
			if ((i % 2) == 0) {
				convertAlongLine(x0,y0,x1,y1,stepSize,cutoff,img);
			} else {
				convertAlongLine(x1,y1,x0,y0,stepSize,cutoff,img);
			}
			++i;
		}
	}
}
