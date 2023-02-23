package com.marginallyclever.convenience;

public class LineInterpolator {
	static public final double SMALL_VALUE = 1e-5;
	
	protected Point2D start = new Point2D();
	protected Point2D end = new Point2D();
	
	public LineInterpolator() {}
	
	public LineInterpolator(Point2D start,Point2D end) {
		this.start.set(start);
		this.end.set(end);
	}
	
	/**
	 * Override this method for more sophisticated lines.
	 * @param t [0...1]
	 * @param p will be to set to (b-a)*t+a 
	 */
	public void getPoint(double t,Point2D p) {
		p.x = (end.x - start.x) * t + start.x;
		p.y = (end.y - start.y) * t + start.y;
	}
	
	/**
	 * @param t [0...1]
	 * @param v set to the approximate tangent to the line at at t
	 */
	public void getTangent(double t,Point2D v) {
		if(t<0) t=0;
		if(t>1-SMALL_VALUE) t=1-SMALL_VALUE;

		double t0 = t;
		double t1 = t + SMALL_VALUE;

		Point2D c0 = new Point2D();
		Point2D c1 = new Point2D();
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
	public void getNormal(double t,Point2D n) {
		getTangent(t,n);
		double z = n.y;
		n.y = -n.x;
		n.x = z;
	}

	public Point2D getStart() {
		return start;
	}

	public void setStart(Point2D start) {
		this.start = start;
	}

	public Point2D getEnd() {
		return end;
	}

	public void setEnd(Point2D end) {
		this.end = end;
	}
}
