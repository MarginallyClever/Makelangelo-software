package com.marginallyclever.makelangeloRobot.generators;


/**
 * A simple turtle implementation to make generating pictures and learning programming easier.
 * @author Admin
 *
 */
public class Turtle {
	private float turtleX, turtleY;
	private float turtleDx, turtleDy;
	private float angle;
	
	public Turtle() {
		reset();
	}
	
	public void reset() {
		turtleX = 0;
		turtleY = 0;
		setAngle(0);
	}
	
	public void setX(float arg0) {		turtleX = arg0;	}
	public void setY(float arg0) {		turtleY = arg0;	}
	public float getX() {		return turtleX;	}
	public float getY() {		return turtleY;	}

	public void turn(float degrees) {
		setAngle(angle+degrees);
	}

	public float getAngle() {
		return angle;
	}
	
	/**
	 * @param degrees degrees
	 */
	public void setAngle(float degrees) {
		angle=degrees;
		turtleDx = (float)Math.cos(Math.toRadians(angle));
		turtleDy = (float)Math.sin(Math.toRadians(angle));
	}

	public void move(float stepSize) {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		turtleX += (turtleDx * (float)stepSize );
		turtleY += (turtleDy * (float)stepSize );
	}
}
