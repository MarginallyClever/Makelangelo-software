package com.marginallyclever.convenience;

/** 
 * {@link LineSegment2D} represents two connected points and line color. 
 * @author Dan Royer
 *
 */
public class LineSegment2D {
	public Point2D a, b;
	
	public ColorRGB c;
	// used while processing line segments.
	public boolean flag;

	public LineSegment2D(Point2D a, Point2D b, ColorRGB c) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public void flip() {
		Point2D temp=b;
		b=a;
		a=temp;
	}
	
	public String toString() {
		return "("+a.x+","+a.y+")-("+b.x+","+b.y+")";
	}
	
	public double lengthSquared() {
		double dx=a.x-b.x;
		double dy=a.y-b.y;
		return dx*dx + dy*dy;
	}

	// The distance measured is the distance between the specified point,
	// and the closest point between the start and end points of line a. 
	public double ptSegDistSq(Point2D point) {
		return java.awt.geom.Line2D.ptSegDistSq(a.x, a.y, b.x, b.y, point.x, point.y);
	}

	// The distance measured is the distance between the specified point
	// and the closest point on the infinite extension of line a.
	public double ptLineDistSq(Point2D point) {
		return java.awt.geom.Line2D.ptLineDistSq(a.x, a.y, b.x, b.y, point.x, point.y);
	}
}