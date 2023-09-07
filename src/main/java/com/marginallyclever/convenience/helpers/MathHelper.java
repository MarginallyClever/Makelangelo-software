package com.marginallyclever.convenience.helpers;

import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;
import javax.vecmath.Vector2d;

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

	public static double lerp(double t, double a, double b) {
		return a + t * (b - a);
	}

	/**
	 * Returns the point on the line between a and b at t.
	 * @param a point
	 * @param b point
	 * @param t 0...1
	 * @return point on the line between a and b at t.
	 */
	public static Point2d lerp(Tuple2d a, Tuple2d b,double t) {
		Point2d ba = new Point2d(b.x - a.x, b.y - a.y);
		ba.scale(t);
		ba.add(a);
		return ba;
	}
}
