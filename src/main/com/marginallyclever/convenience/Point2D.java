package com.marginallyclever.convenience;

public class Point2D {
	public double x,y;
	
	public double lengthSquared() {
		return x*x+y*y;
	}
	
	public double length() {
		return Math.sqrt(lengthSquared());
	}
}
