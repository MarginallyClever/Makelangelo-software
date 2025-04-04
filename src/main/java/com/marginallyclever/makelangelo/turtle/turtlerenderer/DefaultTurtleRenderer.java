package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.marginallyclever.makelangelo.Translator;

import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Draws Turtle instructions one line segment at a time.
 * @author Dan Royer
 *
 */
public class DefaultTurtleRenderer implements TurtleRenderer {
	private Graphics2D gl2;
	private Color colorTravel = Color.GREEN;
	private Color colorDraw = Color.BLACK;
	private boolean showTravel = false;
	private float penDiameter = 1;
	private boolean isPenUp = true;
	private final Line2D line = new Line2D.Double();
	
	@Override
	public void start(Graphics2D gl2) {
		this.gl2 = gl2;

		// set pen diameter
		gl2.setStroke(new BasicStroke(penDiameter));

		isPenUp = true;

		// set pen diameter
		gl2.setStroke(new BasicStroke(penDiameter));
	}

	@Override
	public void end() {}

	@Override
	public void draw(Point2d p0, Point2d p1) {
		if(isPenUp) {
			gl2.setColor(colorDraw);
			isPenUp = false;
		}

		line.setLine(p0.x, p0.y, p1.x, p1.y);
		gl2.draw(line);
	}

	@Override
	public void travel(Point2d p0, Point2d p1) {
		if(!isPenUp) {
			isPenUp = true;
			if(showTravel) {
				gl2.setColor(colorTravel);
			}
		}
		if(!showTravel) return;

		line.setLine(p0.x, p0.y, p1.x, p1.y);
		gl2.draw(line);
	}

	@Override
	public void setPenDownColor(Color color) {
		colorDraw = color;
	}

	@Override
	public void setPenUpColor(Color color) {
		colorTravel = color;
	}
	
	@Override
	public void setPenDiameter(double penDiameter) {
		this.penDiameter = (float)penDiameter;
	}

	@Override
	public String getTranslatedName() {
		return Translator.get("DefaultTurtleRenderer.name");
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
