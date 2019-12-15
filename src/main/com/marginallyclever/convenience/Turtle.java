package com.marginallyclever.convenience;

import java.util.ArrayList;


/**
 * A simple turtle implementation to make generating pictures and learning programming easier.
 * @author Admin
 *
 */
public class Turtle {
	public enum MoveType {
		TRAVEL,  // move without drawing 
		DRAW;  // move while drawing
	}
	
	public class Movement {
		public MoveType type;
		public double x,y;  // destination
		
		Movement(double x0,double y0,MoveType type0) {
			x=x0;
			y=y0;
			type=type0;
		}
	};
	
	public ArrayList<Movement> history;

	// current state
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
		penUp();
		history = new ArrayList<Movement>();
	}

	public void jumpTo(double x,double y) {
		penUp();
		moveTo(x,y);
		penDown();
	}
	
	public void moveTo(double x,double y) {
		turtleX=x;
		turtleY=y;
		history.add(new Movement(x,y,isUp ? MoveType.TRAVEL:MoveType.DRAW));
	}
	
	public void setX(double arg0) {
		moveTo(arg0,turtleY);
	}
	
	public void setY(double arg0) {
		moveTo(turtleX,arg0);
	}
	
	public double getX() {
		return turtleX;
	}
	
	public double getY() {
		return turtleY;
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
		double radians=Math.toRadians(angle);
		turtleDx = Math.cos(radians);
		turtleDy = Math.sin(radians);
	}

	public void forward(double stepSize) {
		moveTo(
			turtleX + turtleDx * stepSize,
			turtleY + turtleDy * stepSize
		);
	}
}
