package com.marginallyclever.convenience;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class LineInterpolator {
	static public final double SMALL_VALUE = 1e-5;
	
	protected Point2d start = new Point2d();
	protected Point2d end = new Point2d();
	
	public LineInterpolator() {}
	
	public LineInterpolator(Point2d start,Point2d end) {
		this.start.set(start);
		this.end.set(end);
	}
	
	/**
	 * Override this method for more sophisticated lines.
	 * @param t [0...1]
	 * @param p will be to set to (b-a)*t+a 
	 */
	public void getPoint(double t,Point2d p) {
		p.x = (end.x - start.x) * t + start.x;
		p.y = (end.y - start.y) * t + start.y;
	}
	
	/**
	 * @param t [0...1]
	 * @param v set to the approximate tangent to the line at at t
	 */
	public void getTangent(double t, Vector2d v) {
		if(t<0) t=0;
		if(t>1-SMALL_VALUE) t=1-SMALL_VALUE;

		double t0 = t;
		double t1 = t + SMALL_VALUE;

		Point2d c0 = new Point2d();
		Point2d c1 = new Point2d();
		getPoint(t0,c0);
		getPoint(t1,c1);
		v.x = c1.x-c0.x;
		v.y = c1.y-c0.y;

		// try to normalize the vector
		double len = v.length();
		if(len!=0) {
			v.scale(1.0/len);
		}
	}

	/**
	 * @param t [0...1]
	 * @param n set to the normal to the approximate tangent to the line at at t
	 */
	public void getNormal(double t,Vector2d n) {
		getTangent(t,n);
		double z = n.y;
		n.y = -n.x;
		n.x = z;
	}

	public Point2d getStart() {
		return start;
	}

	public void setStart(Point2d start) {
		this.start = start;
	}

	public Point2d getEnd() {
		return end;
	}

	public void setEnd(Point2d end) {
		this.end = end;
	}
}
