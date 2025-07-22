package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterCMYK;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectBoolean;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;

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
		toCorners.addSelectListener(evt->{
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

		FilterCMYK cmyk = new FilterCMYK(myImage);
		cmyk.filter();

		double separation; 
		float h2 = (float)myPaper.getPaperHeight();
		float w2 = (float)myPaper.getPaperWidth();
		separation = (w2<h2) ? w2/4 : h2/4;

		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

		// remove extra change color at the start of the turtle
		turtle.getLayers().clear();
		
		logger.debug("Yellow...");		outputChannel(cmyk.getY(),Color.YELLOW,45    ,separation);
		logger.debug("Cyan...");		outputChannel(cmyk.getC(),Color.CYAN,45+ 90,separation);
		logger.debug("Magenta...");		outputChannel(cmyk.getM(),Color.MAGENTA,45+180,separation);
		logger.debug("Black...");		outputChannel(cmyk.getK(),Color.BLACK,45+270,separation);

		fireConversionFinished();
	}

	protected void outputChannel(TransformedImage img, Color newColor, double angle, double separation) {
		double cx = Math.cos(Math.toRadians(angle))*separation;
		double cy = Math.sin(Math.toRadians(angle))*separation;
		turtle.setStroke(newColor);
		
		double maxr;
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		if (convertToCorners) {
			// go right to the corners
			double h2 = rect.getHeight();
			double w2 = rect.getWidth();
			maxr = Math.sqrt(h2 * h2 + w2 * w2) + 1.0;
		} else {
			// do the largest circle that still fits in the image.
			double w = rect.getWidth()/2.0f;
			double h = rect.getHeight()/2.0f;
			maxr = Math.min(h, w);
		}

		double px = myPaper.getCenterX();
		double py = myPaper.getCenterY();
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

				if(rect.contains(fx, fy)) {
					try {
						z = img.sample(fx, fy,1);
					} catch(Exception e) {
						logger.error("Failed to sample", e);
					}

					level = (level1-level0)*p + level0;
					if(z<level) turtle.penDown();
					else turtle.penUp();
				} else turtle.penUp();
				turtle.moveTo(px+fx, py+fy);
			}
			r -= toolDiameter;
			++numRings;
		}

		logger.debug("{} rings.", numRings);
	}
}