package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterCMYK;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * See also <a href="http://the-print-guide.blogspot.ca/2009/05/halftone-screen-angles.html">...</a>
 * @author Dan Royer
 */
public class Converter_CMYK_Crosshatch extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_CMYK_Crosshatch.class);
	static protected int passes=1;// passes value have to be >=1.

	public Converter_CMYK_Crosshatch() {
		super();

		SelectSlider selectPasses = new SelectSlider("passes", Translator.get("Converter_CMYK_Crosshatch.Passes"), 5, 1, getPasses());
		selectPasses.addSelectListener(evt->{
			setPasses((int)evt.getNewValue());
			fireRestart();
		});
		add(selectPasses);

		add(new SelectReadOnlyText("note",Translator.get("Converter_CMYK_Crosshatch.Note")));
	}

	@Override
	public String getName() {
		return Translator.get("Converter_CMYK_Crosshatch.Name");
	}

	public int getPasses() {
		return passes;
	}
	
	/**
	 * @param value number of passes to make.  Must be >=1.
	 */
	public void setPasses(int value) {
		passes = Math.max(1, value);
	}

	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterCMYK cmyk = new FilterCMYK(myImage);
		cmyk.filter();
		
		turtle = new Turtle();
		// remove extra change color at the start of the turtle
		turtle.strokeLayers.clear();
		
		logger.debug("Yellow...");		outputChannel(cmyk.getY(),0 , Color.YELLOW);
		logger.debug("Cyan...");		outputChannel(cmyk.getC(),15, Color.CYAN);
		logger.debug("Magenta...");		outputChannel(cmyk.getM(),75, Color.MAGENTA);
		logger.debug("Black...");		outputChannel(cmyk.getK(),45, Color.BLACK);

		fireConversionFinished();
	}
	
	protected void outputChannel(TransformedImage img, float angle, Color newColor) {
		double dx = Math.cos(Math.toRadians(angle));
		double dy = Math.sin(Math.toRadians(angle));
		double [] channelCutoff = {0,153,51,102,204};
		
		turtle.setStroke(newColor);

		// figure out how many lines we're going to have on this image.
		double stepSize = passes;

		// from top to bottom of the margin area...
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double height  = rect.getHeight();
		double width   = rect.getWidth();
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
