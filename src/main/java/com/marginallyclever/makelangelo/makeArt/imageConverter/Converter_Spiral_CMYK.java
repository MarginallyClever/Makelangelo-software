package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyClever.convenience.ColorRGB;
import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.makeArt.TransformedImage;
import com.marginallyClever.makelangelo.makeArt.imageFilter.Filter_CMYK;
import com.marginallyClever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *  
 * Inspired by reddit user bosny
 * 
 * @author Dan
 */
public class Converter_Spiral_CMYK extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_Spiral_CMYK.class);
	private static boolean convertToCorners = false;  // draw the spiral right out to the edges of the square bounds.

	@Override
	public String getName() {
		return Translator.get("SpiralCMYKName");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("toCorners")) setToCorners((boolean)evt.getNewValue());
	}

	public boolean getToCorners() {
		return convertToCorners;
	}
	
	public void setToCorners(boolean arg0) {
		convertToCorners=arg0;
	}
	
	/**
	 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void finish() {
		Filter_CMYK cmyk = new Filter_CMYK();
		cmyk.filter(myImage);

		double separation; 
		float h2 = (float)myPaper.getPaperHeight();
		float w2 = (float)myPaper.getPaperWidth();
		separation = (w2<h2) ? w2/4 : h2/4;

		turtle = new Turtle();
		// remove extra change color at the start of the turtle
		turtle.history.clear();
		
		logger.debug("Yellow...");
		outputChannel(cmyk.getY(),new ColorRGB(255,255,  0),Math.cos(Math.toRadians(45    ))*separation,Math.sin(Math.toRadians(45    ))*separation);
		logger.debug("Cyan...");
		outputChannel(cmyk.getC(),new ColorRGB(  0,255,255),Math.cos(Math.toRadians(45+ 90))*separation,Math.sin(Math.toRadians(45+ 90))*separation);
		logger.debug("Magenta...");
		outputChannel(cmyk.getM(),new ColorRGB(255,  0,255),Math.cos(Math.toRadians(45+180))*separation,Math.sin(Math.toRadians(45+180))*separation);
		logger.debug("Black...");
		outputChannel(cmyk.getK(),new ColorRGB(  0,  0,  0),Math.cos(Math.toRadians(45+270))*separation,Math.sin(Math.toRadians(45+270))*separation);
	}

	protected void outputChannel(TransformedImage img, ColorRGB newColor, double cx, double cy) {
		turtle.setColor(newColor);
		
		double maxr;
		if (convertToCorners) {
			// go right to the corners
			double h2 = myPaper.getMarginHeight();
			double w2 = myPaper.getMarginWidth();
			maxr = Math.sqrt(h2 * h2 + w2 * w2) + 1.0;
		} else {
			// do the largest circle that still fits in the image.
			double w = myPaper.getMarginWidth()/2.0f;
			double h = myPaper.getMarginHeight()/2.0f;
			maxr = h < w ? h : w;
		}

		double toolDiameter = 1;

		int i, j;
		int steps = 4;
		double leveladd = 255.0 / (double)(steps+1);
		double level;
		int z = 0;

		double r = maxr;
		double fx, fy;
		int numRings = 0;
		j = 0;
		while (r > toolDiameter) {
			++j;
			double level0 = leveladd * (1+(j%steps));
			double level1 = leveladd * (1+((j+1)%steps));
			// find circumference of current circle
			double c1 = Math.floor((2.0f * r - toolDiameter) * Math.PI);
			
			for (i = 0; i < c1; ++i) {
				double p = (double)i / c1;
				double f = Math.PI * 2.0 * p;
				double r1 = r - toolDiameter * p;
				fx = cx + Math.cos(f) * r1;
				fy = cy + Math.sin(f) * r1;
				
				boolean isInside = isInsidePaperMargins(fx, fy);
				if(isInside) {
					try {
						z = img.sample3x3(fx, fy);
					} catch(Exception e) {
						logger.error("Failed to sample", e);
					}

					level = (level1-level0)*p + level0;
					if(z<level) turtle.penDown();
					else turtle.penUp();
				} else turtle.penUp();
				turtle.moveTo(fx, fy);
			}
			r -= toolDiameter;
			++numRings;
		}

		logger.debug("{} rings.", numRings);
	}
}