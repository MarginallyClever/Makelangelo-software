package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.marginallyclever.makelangelo.Translator;

import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Draws Turtle in red/blue sequence to show line segments.
 * @author Dan Royer
 *
 */
public class BarberPoleTurtleRenderer implements TurtleRenderer {
	private Graphics2D gl2;
	
	private Color colorTravel = Color.GREEN;
	private boolean showTravel = false;
	private float penDiameter =1;
	private int moveCounter;
	private final Line2D line = new Line2D.Double();

	@Override
	public void start(Graphics2D gl2) {
		this.gl2 = gl2;

		// set pen diameter
		gl2.setStroke(new BasicStroke(penDiameter, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		moveCounter=0;
	}

	@Override
	public void end() {}
	
	private void setDrawColor() {
		if(moveCounter%2==0) gl2.setColor(Color.RED);
		//else if(moveCounter%3==1) gl2.setColor(Color.PURPLE);
		else gl2.setColor(Color.BLUE);
		moveCounter++;
	}
	
	@Override
	public void draw(Point2d p0, Point2d p1) {
		setDrawColor();
		line.setLine(p0.x, p0.y, p1.x, p1.y);
		gl2.draw(line);
	}

	@Override
	public void travel(Point2d p0, Point2d p1) {
		if(!showTravel) return;
		
		gl2.setColor(colorTravel);
		line.setLine(p0.x, p0.y, p1.x, p1.y);
		gl2.draw(line);
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
        return Translator.get("BarberPoleTurtleRenderer.name");
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
