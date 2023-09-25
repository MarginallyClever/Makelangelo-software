package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.Clipper2D;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.Filter_CMYK;
import com.marginallyclever.makelangelo.makeart.tools.InfillTurtle;
import com.marginallyclever.makelangelo.makeart.tools.RemoveExtraColorChangesFromTurtle;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See also <a href="http://the-print-guide.blogspot.ca/2009/05/halftone-screen-angles.html">...</a>
 * @author Dan Royer
 */
public class Converter_CMYK_Circles extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_CMYK_Circles.class);
	static protected int maxCircleRadius =5;

	public Converter_CMYK_Circles() {
		super();

		SelectSlider maxCircleSize = new SelectSlider("maxCircleSize", Translator.get("Converter_CMYK_Circles.maxCircleSize"), 10, 1, getMaxCircleSize());
		maxCircleSize.addPropertyChangeListener((evt)->{
			setMaxCircleSize((int)evt.getNewValue());
			fireRestart();
		});
		add(maxCircleSize);

		add(new SelectReadOnlyText("note",Translator.get("Converter_CMYK_Crosshatch.Note")));
	}

	@Override
	public String getName() {
		return Translator.get("Converter_CMYK_Circles.name");
	}

	public int getMaxCircleSize() {
		return maxCircleRadius;
	}
	
	/**
	 * @param value Must be >=1.
	 */
	public void setMaxCircleSize(int value) {
		maxCircleRadius = Math.max(1, value);
	}
	
	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		Filter_CMYK cmyk = new Filter_CMYK(myImage);
		cmyk.filter();
		
		turtle = new Turtle();
		// remove extra change color at the start of the turtle
		turtle.history.clear();
		
		logger.debug("Yellow...");		outputChannel(cmyk.getY(),0 ,new ColorRGB(255,255,  0));
		logger.debug("Cyan...");		outputChannel(cmyk.getC(),15,new ColorRGB(  0,255,255));
		logger.debug("Magenta...");		outputChannel(cmyk.getM(),75,new ColorRGB(255,  0,255));
		logger.debug("Black...");		outputChannel(cmyk.getK(),45,new ColorRGB(  0,  0,  0));

		RemoveExtraColorChangesFromTurtle.run(turtle);
		fireConversionFinished();
	}

	/**
	 * Remove any color changes that are not needed.
	 * TODO could be used on every Turtle generated.
	 * @param turtle the turtle to clean up
	 */
	private void removeRedundantColorChanges(Turtle turtle) {
	}
	
	protected void outputChannel(TransformedImage img, float angle, ColorRGB newColor) {
		double dx = Math.cos(Math.toRadians(angle));
		double dy = Math.sin(Math.toRadians(angle));
		
		turtle.setColor(newColor);

		// from top to bottom of the margin area...
		double height  = myPaper.getMarginTop() - myPaper.getMarginBottom();
		double width   = myPaper.getMarginRight() - myPaper.getMarginLeft();
		double maxLen  = Math.sqrt(width*width+height*height);

		int i=0;
		for(double a = -maxLen;a<maxLen;a+= maxCircleRadius*2) {
			double px = dx * a;
			double py = dy * a;
			// p0-p1 is at a right angle to dx/dy
			double x0 = px - dy * -maxLen;
			double y0 = py + dx * -maxLen;
			double x1 = px - dy * maxLen;
			double y1 = py + dx * maxLen;

			if(i%2==0) {
				circlesAlongLine(x0, y0, x1, y1, img);
			} else {
				circlesAlongLine(x1, y1, x0, y0, img);
			}
			++i;
		}
	}

	private void circlesAlongLine(double x1, double y1, double x0, double y0, TransformedImage img) {
		Point2D P0 = new Point2D(x0,y0);
		Point2D P1 = new Point2D(x1,y1);

		Point2D rMax = new Point2D(myPaper.getMarginRight(),myPaper.getMarginTop());
		Point2D rMin = new Point2D(myPaper.getMarginLeft(),myPaper.getMarginBottom());
		if(!Clipper2D.clipLineToRectangle(P0, P1, rMax, rMin)) {
			// entire line clipped
			return;
		}

		double dx=P1.x-P0.x;
		double dy=P1.y-P0.y;
		double halfStep = maxCircleRadius;
		double distance = Math.sqrt(dx*dx+dy*dy);

		double n,x,y,v;

		double b;
		for( b = 0; b <= distance; b+= maxCircleRadius*2) {
			n = b / distance;
			x = dx * n + P0.x;
			y = dy * n + P0.y;

			v = img.sample( x - halfStep, y - halfStep, x + halfStep, y + halfStep);

			drawCircle(x, y, maxCircleRadius * ((255.0-v)/255.0));
		}
	}

	private void drawCircle(double x,double y,double r) {
		double circumference = Math.ceil(Math.PI*r*2.0);
		Turtle t = new Turtle();
		t.history.clear();
		t.setColor(turtle.getColor());
		t.jumpTo(x+r,y+0);
		for(int i=0;i<circumference;++i) {
			double v = 2.0*Math.PI * (double)i/circumference;
			t.moveTo(
					x+Math.cos(v)*r,
					y+Math.sin(v)*r);
		}
		t.moveTo(x+r,y+0);

		try {
			InfillTurtle filler = new InfillTurtle();
			filler.setPenDiameter(t.getDiameter());
			Turtle t2 = filler.run(t);
			turtle.add(t2);
		} catch(Exception e) {
			// shape was not closed, do nothing.
		}

		turtle.add(t);
	}
}
