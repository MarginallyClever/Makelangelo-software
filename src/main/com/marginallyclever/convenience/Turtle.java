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
		DRAW,  // move while drawing
		TOOL_CHANGE;
	}
	
	public class Movement {
		public MoveType type;
		public double x,y;  // destination
		
		public Movement(double x0,double y0,MoveType type0) {
			x=x0;
			y=y0;
			type=type0;
		}
		
		public Movement(Movement m) {
			this.x=m.x;
			this.y=m.y;
			this.type=m.type;
		}

		public ColorRGB getColor() {
			return new ColorRGB((int)x);
		}
	};
	
	public ArrayList<Movement> history;

	// current state
	private double turtleX, turtleY;
	private double turtleDx, turtleDy;
	private double angle;
	private boolean isUp;
	private ColorRGB color;

	
	public Turtle() {
		reset();
	}
	
	protected void reset() {
		turtleX = 0;
		turtleY = 0;
		setAngle(0);
		penUp();
		history = new ArrayList<Movement>();
	}

	public void setColor(ColorRGB c) {
		if(color!=null) {
			if(color.red==c.red && color.green==c.green && color.blue==c.blue) return;
			color.set(c);
		} else {
			color = new ColorRGB(c);
		}
		history.add( new Movement(c.toInt(),0/*tool diameter?*/,MoveType.TOOL_CHANGE) );
	}
	public ColorRGB getColor() {
		return color;
	}
	
	public void jumpTo(double x,double y) {
		penUp();
		moveTo(x,y);
		penDown();
	}
	
	public void moveTo(double x,double y) {
		turtleX=x;
		turtleY=y;
		history.add( new Movement(x, y, isUp ? MoveType.TRAVEL : MoveType.DRAW) );
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

	/**
	 * Calculate the limits of drawing lines in this turtle history
	 * @param top maximum limits
	 * @param bottom minimum limits
	 */
	public void getBounds(Point2D top,Point2D bottom) {
		bottom.x=Float.MAX_VALUE;
		bottom.y=Float.MAX_VALUE;
		top.x=-Float.MAX_VALUE;
		top.y=-Float.MAX_VALUE;
		
		for( Movement m : history ) {
			switch(m.type) {
			case DRAW:
				if(top.x<m.x) top.x=m.x;
				if(top.y<m.y) top.y=m.y;
				if(bottom.x>m.x) bottom.x=m.x;
				if(bottom.y>m.y) bottom.y=m.y;
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Scale all draw and move segments by parameters
	 * @param sx
	 * @param sy
	 */
	public void scale(double sx, double sy) {
		for( Movement m : history ) {
			switch(m.type) {
			case DRAW:
			case TRAVEL:
				m.x*=sx;
				m.y*=sy;
				break;
			default:
				break;
			}
		}
	}
}
