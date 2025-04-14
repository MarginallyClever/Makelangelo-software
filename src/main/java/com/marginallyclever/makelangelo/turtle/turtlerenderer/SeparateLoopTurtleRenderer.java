package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;

import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Draw {@link com.marginallyclever.makelangelo.turtle.Turtle} with a new color every time the pen is lowered.
 * This illustrates each "loop" of the drawing.
 * @author Dan Royer
 */
public class SeparateLoopTurtleRenderer implements TurtleRenderer {
	private Graphics2D gl2;
	
	private Color colorTravel = Color.GREEN;
	private boolean showTravel = false;
	private float penDiameter = 1;
	private int moveCounter;
	private final Line2D line = new Line2D.Double();
		
	@Override
	public void start(Graphics2D gl2) {
		this.gl2=gl2;
		showTravel = GFXPreferences.getShowPenUp();

		// set pen diameter
		gl2.setStroke(new BasicStroke(penDiameter));

		moveCounter=0;
	}

	@Override
	public void end() {}
	
	private void setDrawColor() {
		switch(moveCounter%7) {
		case 0 -> gl2.setColor(new Color(255,   0,   0));
		case 1 -> gl2.setColor(new Color(  0, 102,   0));  // 255*0.4 = 102
		case 2 -> gl2.setColor(new Color(  0,   0, 255));
		case 3 -> gl2.setColor(new Color(255, 255,   0));
		case 4 -> gl2.setColor(new Color(255,   0, 255));
		case 5 -> gl2.setColor(new Color(  0, 255, 255));
		case 6 -> gl2.setColor(new Color(  0,   0,   0));
		}
		moveCounter++;
	}
	
	@Override
	public void draw(Point2d p0, Point2d p1) {
		line.setLine(p0.x, p0.y, p1.x, p1.y);
		gl2.draw(line);
	}

	@Override
	public void travel(Point2d p0, Point2d p1) {
		if(showTravel) {
			gl2.setColor(colorTravel);
			line.setLine(p0.x, p0.y, p1.x, p1.y);
			gl2.draw(line);
		}
		setDrawColor();
	}

	@Override
	public void setPenDownColor(Color color) {}

	@Override
	public void setPenUpColor(Color color) {
		colorTravel=(color);
	}

	@Override
	public void setPenDiameter(double penDiameter) {
		this.penDiameter =(float)penDiameter;
	}

    @Override
    public String getTranslatedName() {
        return Translator.get("SeparateLoopTurtleRenderer.name");
    }

	/**
	 * Reset any internal state to defaults.  This makes sure rendering optimizations cleaned
	 * up when the turtle is changed.
	 */
	@Override
	public void reset() {}

	@Override
	public void setShowTravel(boolean showTravel) {
		this.showTravel = showTravel;
	}
}
