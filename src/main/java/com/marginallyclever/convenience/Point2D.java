package com.marginallyclever.convenience;

public class Point2D {
	public double x,y;
	
	public Point2D() {}

	public Point2D(Point2D b) {
		this(b.x,b.y);
	}

	public Point2D(double x0,double y0) {
		x=x0;
		y=y0;
	}
	
	public void set(double x0,double y0) {
		x=x0;
		y=y0;
	}
	public void set(Point2D p) {
		x=p.x;
		y=p.y;
	}
	public double lengthSquared() {
		return x*x+y*y;
	}
	
	public double length() {
		return Math.sqrt(lengthSquared());
	}
	
	public void scale(double scale) {
		x*=scale;
		y*=scale;
	}
	
	public double distanceSquared(Point2D p) {
		double dx = x-p.x;
		double dy = y-p.y;
		return dx*dx + dy*dy;
	}

	public double distance(Point2D p) {
		return Math.sqrt(distanceSquared(p));
	}

	public void normalize() {
		double len = length();
		if(len!=0) {
			double ilen = 1.0/len;
			x*=ilen;
			y*=ilen;
		}
	}

	public boolean equalsEpsilon(Point2D b, double epsilon) {
		return distance(b) < epsilon;
	}
}
