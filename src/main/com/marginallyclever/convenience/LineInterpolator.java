package com.marginallyclever.convenience;

public class LineInterpolator {
	static public final double SMALL_VALUE = 1e-5;
	
	protected Point2D a = new Point2D();
	protected Point2D b = new Point2D();
	
	public LineInterpolator() {}
	
	public LineInterpolator(Point2D start,Point2D end) {
		a.set(start);
		b.set(end);
	}
	
	/**
	 * Override this method for more sophisticated lines.
	 * @param t [0...1]
	 * @param p will be to set to (b-a)*t+a 
	 */
	public void getPoint(double t,Point2D p) {
		p.x = (b.x-a.x)*t+a.x;
		p.y = (b.y-a.y)*t+a.y;
	}
	
	/**
	 * @param t [0...1]
	 * @param v set to the approximate tangent to the line at at t
	 */
	public void getTangent(double t,Point2D v) {
		Point2D c0 = new Point2D();
		Point2D c1 = new Point2D();
		double t0,t1;
		if(t<1) {
			t0=t;
			t1=t+SMALL_VALUE;
		} else {
			t1=t;
			t0=t-SMALL_VALUE;
		}
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

	public Point2D getA() {
		return a;
	}

	public void setA(Point2D a) {
		this.a = a;
	}

	public Point2D getB() {
		return b;
	}

	public void setB(Point2D b) {
		this.b = b;
	}
}
