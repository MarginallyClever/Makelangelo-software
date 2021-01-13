package com.marginallyclever.artPipeline;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;

/** 
 * line segments with color 
 * @author Dan Royer
 *
 */
public class Line2D {
	public Point2D a, b;
	
	public ColorRGB c;
	// used while processing line segments.
	public boolean flag;

	public Line2D(Point2D a, Point2D b, ColorRGB c) {
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
	public double physicalLengthSquared() {
		double dx=a.x-b.x;
		double dy=a.y-b.y;
		return dx*dx + dy*dy;
	}
	
	public double ptSegDistSq(Point2D point) {
		// The distance measured is the distance between the specified point,
		// and the closest point between the start and end points of line a. 
		return java.awt.geom.Line2D.ptSegDistSq(a.x, a.y, b.x, b.y, point.x, point.y);
	}
	
	public double ptLineDistSq(Point2D point) {
		// The distance measured is the distance between the specified point,
		// and the closest point on the infinite extension of line a.
		return java.awt.geom.Line2D.ptLineDistSq(a.x, a.y, b.x, b.y, point.x, point.y);
	}
}