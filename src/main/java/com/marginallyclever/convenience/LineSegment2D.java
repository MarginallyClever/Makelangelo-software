package com.marginallyclever.convenience;

import org.jetbrains.annotations.Contract;

import javax.vecmath.Point2d;
import java.awt.*;

/**
 * {@link LineSegment2D} represents two connected points and line color. 
 * @author Dan Royer
 *
 */
public class LineSegment2D {
	public Point2d start, end;
	public Color color;

	public LineSegment2D(Point2d start, Point2d end, Color color) {
		super();
		this.start = new Point2d(start);
		this.end = new Point2d(end);
		this.color = color;
	}
	
	public void flip() {
		Point2d temp= end;
		end = start;
		start = temp;
	}

	@Contract(pure = true)
	public String toString() {
		return "("+ start.x+","+ start.y+")-("+ end.x+","+ end.y+")";
	}

	@Contract(pure = true)
	public double lengthSquared() {
		double dx= start.x- end.x;
		double dy= start.y- end.y;
		return dx*dx + dy*dy;
	}

	/**
	 * @param point the specified point
	 * @return the distance between the specified point and the closest point on the line segment.
	 */
	@Contract(pure = true)
	public double ptSegDistSq(Point2d point) {
		return java.awt.geom.Line2D.ptSegDistSq(start.x, start.y, end.x, end.y, point.x, point.y);
	}

	/**
	 * @param point the specified point
	 * @return the distance between the specified point and the closest point on the line.  If the point is on
	 * 			the line the distance is 0, even if it is not between the two ends.
	 */
	@Contract(pure = true)
	public double ptLineDistSq(Point2d point) {
		return java.awt.geom.Line2D.ptLineDistSq(start.x, start.y, end.x, end.y, point.x, point.y);
	}
}