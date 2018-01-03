package com.marginallyclever.makelangeloRobot.generators;


/**
 * A simple turtle implementation to make generating pictures and learning programming easier.
 * @author Admin
 *
 */
public class Turtle {
	private float turtleX, turtleY;
	private float turtleDx, turtleDy;

	
	public Turtle() {
		reset();
	}
	
	public void reset() {
		turtleX = 0;
		turtleY = 0;
		turtleDx = 0;
		turtleDy = -1;
	}
	
	public void setX(float arg0) {		turtleX = arg0;	}
	public void setY(float arg0) {		turtleY = arg0;	}
	public float getX() {		return turtleX;	}
	public float getY() {		return turtleY;	}

	public void turn(float degrees) {
		double n = degrees * Math.PI / 180.0;
		double newx = Math.cos(n) * turtleDx + Math.sin(n) * turtleDy;
		double newy = -Math.sin(n) * turtleDx + Math.cos(n) * turtleDy;
		double len = Math.sqrt(newx * newx + newy * newy);
		assert (len > 0);
		turtleDx = (float) (newx / len);
		turtleDy = (float) (newy / len);
	}


	public void move(float stepSize) {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		turtleX += (turtleDx * (float)stepSize );
		turtleY += (turtleDy * (float)stepSize );
	}
}
