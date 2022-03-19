package com.marginallyclever.convenience;

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
	 * @param dx x component
	 * @param dy y component
	 * @return Length of vector (dx,dy) 
	 */
	public static double length(double dx,double dy) {
		return (float)Math.sqrt(lengthSquared(dx,dy));
	}
}
