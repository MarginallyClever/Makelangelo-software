package com.marginallyclever.convenience;


/**
 * A simple turtle implementation to make generating pictures and learning programming easier.
 * @author Admin
 *
 */
public class Turtle {
	private double turtleX, turtleY;
	private double turtleDx, turtleDy;
	private double angle;
	private boolean isUp;
	
	public Turtle() {
		reset();
	}
	
	public void reset() {
		turtleX = 0;
		turtleY = 0;
		setAngle(0);
	}
	
	public void moveTo(double x,double y) {
		turtleX=x;
		turtleY=y;
	}
	public void setX(double arg0) {		turtleX = arg0;	}
	public void setY(double arg0) {		turtleY = arg0;	}
	public double getX() {		return turtleX;	}
	public double getY() {		return turtleY;	}
	
	public void raisePen() {
		isUp=true;
	}
	public void lowerPen() {
		isUp=false;
	}
	public void penUp() {
		isUp=true;
	}
	public void penDown() {
		isUp=false;
	}
	public boolean isUp() {
		return isUp;
	}

	public void turn(double degrees) {
		setAngle(angle+degrees);
	}

	public double getAngle() {
		return angle;
	}
	
	/**
	 * @param degrees degrees
	 */
	public void setAngle(double degrees) {
		angle=degrees;
		turtleDx = (double)Math.cos(Math.toRadians(angle));
		turtleDy = (double)Math.sin(Math.toRadians(angle));
	}

	public void move(double stepSize) {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		turtleX += (turtleDx * (double)stepSize );
		turtleY += (turtleDy * (double)stepSize );
	}
}
