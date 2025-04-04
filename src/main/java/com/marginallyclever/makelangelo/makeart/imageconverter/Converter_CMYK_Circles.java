package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.Clipper2D;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterCMYK;
import com.marginallyclever.makelangelo.makeart.turtletool.InfillTurtle;
import com.marginallyclever.makelangelo.makeart.turtletool.RemoveExtraColorChangesFromTurtle;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectBoolean;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * See also <a href="http://the-print-guide.blogspot.ca/2009/05/halftone-screen-angles.html">...</a>
 * @author Dan Royer
 */
public class Converter_CMYK_Circles extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_CMYK_Circles.class);
	protected static int maxCircleRadius =5;
	protected static boolean fillCircles = false;

	public Converter_CMYK_Circles() {
		super();

		SelectSlider maxCircleSize = new SelectSlider("maxCircleSize", Translator.get("Converter_CMYK_Circles.maxCircleSize"), 10, 1, getMaxCircleSize());
		maxCircleSize.addSelectListener((evt)->{
			setMaxCircleSize((int)evt.getNewValue());
			fireRestart();
		});
		add(maxCircleSize);
		SelectBoolean fillCircles = new SelectBoolean("fillCircles",Translator.get("Converter_CMYK_Circles.fillCircles"),this.fillCircles);
		fillCircles.addSelectListener((evt)->{
			Converter_CMYK_Circles.fillCircles = (boolean)evt.getNewValue();
			fireRestart();
		});
		add(fillCircles);

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

		FilterCMYK cmyk = new FilterCMYK(myImage);
		cmyk.filter();
		
		turtle = new Turtle();
		// remove extra change color at the start of the turtle
		turtle.strokeLayers.clear();
		
		logger.debug("Yellow...");		outputChannel(cmyk.getY(),0 ,Color.YELLOW);
		logger.debug("Cyan...");		outputChannel(cmyk.getC(),15,Color.CYAN);
		logger.debug("Magenta...");		outputChannel(cmyk.getM(),75,Color.MAGENTA);
		logger.debug("Black...");		outputChannel(cmyk.getK(),45,Color.BLACK);

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
	
	protected void outputChannel(TransformedImage img, float angle, Color newColor) {
		double dx = Math.cos(Math.toRadians(angle));
		double dy = Math.sin(Math.toRadians(angle));
		
		turtle.setStroke(newColor);

		// from top to bottom of the margin area...
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double height  = rect.getHeight();
		double width   = rect.getWidth();
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
		Point2d P0 = new Point2d(x0,y0);
		Point2d P1 = new Point2d(x1,y1);

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		Point2d rMax = new Point2d(rect.getMaxX(),rect.getMaxY());
		Point2d rMin = new Point2d(rect.getMinX(),rect.getMinY());
		if(!Clipper2D.clipLineToRectangle(P0, P1, rMax, rMin)) {
			// entire line clipped
			return;
		}

		double cx = myPaper.getCenterX();
		double cy = myPaper.getCenterY();
		double dx=P1.x-P0.x;
		double dy=P1.y-P0.y;
		double halfStep = maxCircleRadius;
		double distance = Math.sqrt(dx*dx+dy*dy);

		double n,x,y,v;
		double b;
		for( b = 0; b <= distance; b+= maxCircleRadius*2) {
			n = b / distance;
			x = dx * n + P0.x + halfStep;
			y = dy * n + P0.y;

			v = img.sample( x, y, halfStep);

			drawCircle(cx + x, cy + y, maxCircleRadius * ((255.0-v)/255.0));
		}
	}

	private void drawCircle(double x,double y,double r) {
		double circumference = Math.ceil(Math.PI*r*2.0);
		Turtle t = new Turtle();
		t.setStroke(turtle.getColor());
		t.jumpTo(x+r,y+0);
		for(int i=0;i<circumference;++i) {
			double v = 2.0*Math.PI * (double)i/circumference;
			t.moveTo(
					x+Math.cos(v)*r,
					y+Math.sin(v)*r);
		}
		t.moveTo(x+r,y+0);

		if(fillCircles) {
			try {
				InfillTurtle filler = new InfillTurtle();
				filler.setPenDiameter(t.getDiameter());
				Turtle t2 = filler.run(t);
				turtle.add(t2);
			} catch (Exception e) {
				// shape was not closed, do nothing.
			}
		}

		turtle.add(t);
	}
}
