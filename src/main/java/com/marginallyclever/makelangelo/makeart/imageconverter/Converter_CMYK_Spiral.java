package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.Filter_CMYK;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *  
 * Inspired by reddit user bosny
 * 
 * @author Dan
 */
public class Converter_CMYK_Spiral extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_CMYK_Spiral.class);
	private static boolean convertToCorners = false;  // draw the spiral right out to the edges of the square bounds.

	public Converter_CMYK_Spiral() {
		super();

		SelectBoolean toCorners = new SelectBoolean("toCorners", Translator.get("Spiral.toCorners"), getToCorners());
		toCorners.addPropertyChangeListener(evt->{
			setToCorners((boolean)evt.getNewValue());
			fireRestart();
		});
		add(toCorners);
	}
	
	@Override
	public String getName() {
		return Translator.get("Converter_CMYK_Spiral.Name");
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
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		Filter_CMYK cmyk = new Filter_CMYK();
		cmyk.filter(myImage);

		double separation; 
		float h2 = (float)myPaper.getPaperHeight();
		float w2 = (float)myPaper.getPaperWidth();
		separation = (w2<h2) ? w2/4 : h2/4;

		turtle = new Turtle();
		// remove extra change color at the start of the turtle
		turtle.history.clear();
		
		logger.debug("Yellow...");		outputChannel(cmyk.getY(),new ColorRGB(255,255,  0),45    ,separation);
		logger.debug("Cyan...");		outputChannel(cmyk.getC(),new ColorRGB(  0,255,255),45+ 90,separation);
		logger.debug("Magenta...");		outputChannel(cmyk.getM(),new ColorRGB(255,  0,255),45+180,separation);
		logger.debug("Black...");		outputChannel(cmyk.getK(),new ColorRGB(  0,  0,  0),45+270,separation);

		fireConversionFinished();
	}

	protected void outputChannel(TransformedImage img, ColorRGB newColor, double angle, double separation) {
		double cx = Math.cos(Math.toRadians(angle))*separation;
		double cy = Math.sin(Math.toRadians(angle))*separation;
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
			maxr = Math.min(h, w);
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

				if(myPaper.isInsidePaperMargins(fx, fy)) {
					try {
						z = img.sample(fx, fy,1);
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