package com.marginallyclever.convenience;

import java.awt.*;

/**
 * {@link LineSegment2D} represents two connected points and line color. 
 * @author Dan Royer
 *
 */
public class LineSegment2D {
	public Point2D start, end;
	public Color color;

	public LineSegment2D(Point2D start, Point2D end, Color color) {
		super();
		this.start = new Point2D(start);
		this.end = new Point2D(end);
		this.color = color;
	}
	
	public void flip() {
		Point2D temp= end;
		end = start;
		start = temp;
	}
	
	public String toString() {
		return "("+ start.x+","+ start.y+")-("+ end.x+","+ end.y+")";
	}
	
	public double lengthSquared() {
		double dx= start.x- end.x;
		double dy= start.y- end.y;
		return dx*dx + dy*dy;
	}

	// The distance measured is the distance between the specified point,
	// and the closest point between the start and end points of line a. 
	public double ptSegDistSq(Point2D point) {
		return java.awt.geom.Line2D.ptSegDistSq(start.x, start.y, end.x, end.y, point.x, point.y);
	}

	// The distance measured is the distance between the specified point
	// and the closest point on the infinite extension of line a.
	public double ptLineDistSq(Point2D point) {
		return java.awt.geom.Line2D.ptLineDistSq(start.x, start.y, end.x, end.y, point.x, point.y);
	}
}