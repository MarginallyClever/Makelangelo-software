package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.marginallyclever.makelangelo.Translator;

import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

/**
 * Draw {@link com.marginallyclever.makelangelo.turtle.Turtle} such that each time the pen is lowered the color begins
 * with red and fades to blue as the pen is lifted.  This illustrates each "loop" of the drawing and the direction.
 * @author Dan Royer
 * @since 7.48.0
 */
public class DirectionLoopTurtleRenderer implements TurtleRenderer {
	private Graphics2D gl2;
	private Color colorTravel = Color.GREEN;
	private boolean showTravel = false;
	private float penDiameter = 1;
	private final ArrayList<Point2d> points = new ArrayList<>();
	private final Line2D line = new Line2D.Double();
		
	@Override
	public void start(Graphics2D gl2) {
		this.gl2 = gl2;

		// set pen diameter
		gl2.setStroke(new BasicStroke(penDiameter));
	}

	@Override
	public void end() {
		drawPoints();
	}
	
	@Override
	public void draw(Point2d p0, Point2d p1) {
		points.add(p0);
		points.add(p1);
	}

	private void drawPoints() {
		if(!points.isEmpty()) {
			int size = points.size();

			for(int i=0;i<size;i+=2) {
				Point2d p0 = points.get(i);
				Point2d p1 = points.get(i+1);
				double r = (double)i/(double)size;
				double b = 1.0 - r;
				gl2.setColor(new Color((int)(r*255),0,(int)(b*255)));
				line.setLine(p0.x, p0.y, p1.x, p1.y);
				gl2.draw(line);
			}
			points.clear();
		}
	}

	@Override
	public void travel(Point2d p0, Point2d p1) {
		drawPoints();
		if(showTravel) {
			gl2.setColor(colorTravel);
			line.setLine(p0.x, p0.y, p1.x, p1.y);
			gl2.draw(line);
		}
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
        return Translator.get("DirectionLoopTurtleRenderer.name");
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
