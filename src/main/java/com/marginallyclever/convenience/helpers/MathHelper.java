package com.marginallyclever.convenience.helpers;

import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;
import javax.vecmath.Vector2d;
import java.security.InvalidParameterException;

public class MathHelper {

	/**
	 * Round a float off to 3 decimal places.
	 * @param v a value
	 * @return Value rounded off to 3 decimal places
	 */
	public static double roundOff3(double v) {
		double SCALE = 1000.0f;
		
		return Math.round(v*SCALE)/SCALE;
	}

	/**
	 * @param dx x component
	 * @param dy y component
	 * @return Square of length of vector (dx,dy) 
	 */
	public static double lengthSquared(double dx,double dy) {
		return dx*dx+dy*dy;
	}

	/**
	 * Returns true if c is on the line between a and b
	 * @param a point
	 * @param b	point
	 * @param c	point	
	 * @param epsilon acceptable error
	 * @return true if c is on the line between a and b
	 */
	public static boolean between(Tuple2d a, Tuple2d b, Tuple2d c, double epsilon) {
		Vector2d ba = new Vector2d(b.x - a.x, b.y - a.y);
		Vector2d ca = new Vector2d(c.x - a.x, c.y - a.y);

		// the cross product will tell us if C is on the infinite line A-B.
		double crossproduct = ca.y * ba.x - ca.x * ba.y;
		// Check if cross product is outside acceptable range
		if (Math.abs(crossproduct) > epsilon) {
			return false;
		}

		// the dot product will tell us if C is between A and B.
		double dotproduct = ca.x * ba.x + ca.y * ba.y;
		if (dotproduct < 0) {
			return false;
		}
		// Check squared length
		double squaredlengthba = ba.x*ba.x + ba.y*ba.y;
		if (dotproduct > squaredlengthba) {
			return false;
		}

		return true;
	}

	/**
	 * Returns true if the two line segments match.
	 * @param a0 point on segment a
	 * @param a1 point on segment a
	 * @param b0 point on segment b
	 * @param b1 point on segment b
	 * @param epsilon margin of error
	 * @return true if (a0=b0 and a1=b1) or (a0=b1 and a1=b0).
	 */
	public static boolean equals(Tuple2d a0, Tuple2d a1, Tuple2d b0, Tuple2d b1, double epsilon) {
		Vector2d c = new Vector2d(a0.x - b0.x, a0.y - b0.y);
		Vector2d d = new Vector2d(a1.x - b1.x, a1.y - b1.y);
		if(Math.abs(c.x)<epsilon && Math.abs(d.y)<epsilon) return true;
		c.set(a0.x - b1.x, a0.y - b1.y);
		d.set(a1.x - b0.x, a1.y - b0.y);
		return (Math.abs(c.x)<epsilon && Math.abs(d.y)<epsilon);
	}

	/**
	 * Linear interpolation between a and b.
	 * @param t 0...1
	 * @param a
	 * @param b
	 * @return a + t * (b - a)
	 */
	public static double lerp(double t, double a, double b) {
		return a + t * (b - a);
	}

	/**
	 * Returns the point on the line segment between a and b at t.
	 * @param a point
	 * @param b point
	 * @param t 0...1
	 * @return a + t * (b - a)
	 */
	public static Point2d lerp(Tuple2d a, Tuple2d b,double t) {
		Point2d ba = new Point2d(b.x - a.x, b.y - a.y);
		ba.scale(t);
		ba.add(a);
		return ba;
	}

	/**
	 * Calculates an intersection of two circles.  Assume the first circle is at the origin and the second is
	 * moved along the positive x axis.
	 * @param r1 radius of circle 0
	 * @param r2 radius of circle 1
	 * @param d distance between the two circles
	 * @return one of the two points where the circles intersect.
	 * @throws InvalidParameterException if r0, r1, or d are negative.
	 * @throws IllegalArgumentException if the circles do not intersect.
	 */
	public static Vector2d intersectionOfCircles(double r1,double r2,double d) {
		if(r1<0) throw new InvalidParameterException("r1 must be >= 0");
		if(r2<0) throw new InvalidParameterException("r2 must be >= 0");
		if(d<0) throw new InvalidParameterException("d must be >= 0");
		if(r1+r2<d) throw new IllegalArgumentException("circles do not intersect");

		double x = (r1*r1 - r2*r2 + d*d) / (2.0*d);
		double y = Math.sqrt(r1*r1 - x*x);
		return new Vector2d(x,y);
	}
}
