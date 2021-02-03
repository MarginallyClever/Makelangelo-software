package com.marginallyclever.convenience.turtle;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Vector2d;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.log.Log;


/**
 * A simple turtle implementation to make generating pictures and learning programming easier.
 * @author Dan Royer
 *
 */
public class Turtle implements Cloneable {
	public ArrayList<TurtleMove> history;

	private ReentrantLock lock;

	// current state
	private double turtleX, turtleY;
	private double turtleDx, turtleDy;
	private double angle;
	private boolean isUp;
	
	// tail tip color.  only one per Turtle.
	private ColorRGB color;
	
	// tail tip radius.
	private double radius;

	private Vector2d limitMax = new Vector2d();
	private Vector2d limitMin = new Vector2d();
	
	public Turtle() {
		super();
		lock = new ReentrantLock();
		reset();
	}
	
	public Turtle(Turtle t) {
		this();
		turtleX = t.turtleX;
		turtleY = t.turtleY;
		turtleDx = t.turtleDx;
		turtleDy = t.turtleDy;
		angle = t.angle;
		isUp = t.isUp;
		t.color.set(t.color);

		for( TurtleMove m : t.history ) {
			history.add(new TurtleMove(m));
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Turtle t = (Turtle)super.clone();
		limitMax = (Vector2d)limitMax.clone();
		limitMin = (Vector2d)limitMin.clone();
		
		for( TurtleMove m : history ) {
			t.history.add(new TurtleMove(m));
		}
		return t;
	}
	
	/**
	 * Return this Turtle to mint condition.  Erases history and resets all parameters.  Called by constructor.
	 */
	protected void reset() {
		turtleX = 0;
		turtleY = 0;
		setAngle(0);
		penUp();
		history = new ArrayList<TurtleMove>();
		// default turtle color is black.
		setColor(new ColorRGB(0,0,0));
		setRadius(1.0);
	}
	
	public void setRadius(double r) {
		radius=r;
	}
	
	public double getRadius() { 
		return radius;
	}
	
	// multithreading lock safety
	public boolean isLocked() {
		return lock.isLocked();
	}
	
	// multithreading lock safety
	public void lock() {
		lock.lock();
	}
	
	// multithreading lock safety
	public void unlock() {
		if(lock.isLocked()) {  // prevents "illegal state exception - not locked"
			lock.unlock();
		}
	}

	public void setColor(ColorRGB c) {
		if(color!=null) {
			if(color.red==c.red && color.green==c.green && color.blue==c.blue) return;
			color.set(c);
		} else {
			color = new ColorRGB(c);
		}
	}
	
	public ColorRGB getColor() {
		return color;
	}
	
	/**
	 * Absolute position change, make sure pen is up before move and put pen down after move.
	 * @param x  
	 * @param y 
	 */
	public void jumpTo(double x,double y) {
		penUp();
		moveTo(x,y);
		penDown();
	}
	
	/**
	 * Absolute position change, do not adjust pen status
	 * @param x  
	 * @param y 
	 */
	public void moveTo(double x,double y) {
		turtleX=x;
		turtleY=y;
		history.add( new TurtleMove(x, y, isUp) );
	}
	
	/**
	 * Absolute position
	 * @param arg0 x axis
	 */
	public void setX(double arg0) {
		moveTo(arg0,turtleY);
	}
	
	/**
	 * Absolute position
	 * @param arg0 y axis
	 */
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
	
	/**
	 * @return true if pen is up
	 */
	public boolean isUp() {
		return isUp;
	}

	/**
	 * Relative turn
	 * @param degrees
	 */
	public void turn(double degrees) {
		setAngle(angle+degrees);
	}

	// Get absolute angle degrees
	public double getAngle() {
		return angle;
	}
	
	/**
	 * Set absolute angle
	 * @param degrees degrees
	 */
	public void setAngle(double degrees) {
		angle=degrees;
		double radians=Math.toRadians(angle);
		turtleDx = Math.cos(radians);
		turtleDy = Math.sin(radians);
	}

	/**
	 * Relative move forward/back
	 * @param stepSize
	 */
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
		TurtleMove old=null;
		
		for( TurtleMove m : history ) {
			if(!m.isUp) {
				if(top.x<m.x) top.x=m.x;
				if(top.y<m.y) top.y=m.y;
				if(bottom.x>m.x) bottom.x=m.x;
				if(bottom.y>m.y) bottom.y=m.y;
				if(old != null)
				{
					if(top.x<old.x) top.x=old.x;
					if(top.y<old.y) top.y=old.y;
					if(bottom.x>old.x) bottom.x=old.x;
					if(bottom.y>old.y) bottom.y=old.y;
				}
			}
			old=m;
		}
	}

	/**
	 * Scale all draw and move segments by parameters
	 * @param sx
	 * @param sy
	 */
	public void scale(double sx, double sy) {
		for( TurtleMove m : history ) {
			m.x*=sx;
			m.y*=sy;
		}
	}

	/**
	 * Translate all draw and move segments by parameters
	 * @param dx relative move x
	 * @param dy relative move y
	 */
	public void translate(double dx, double dy) {
		for( TurtleMove m : history ) {
			m.x+=dx;
			m.y+=dy;
		}
	}

	/**
	 * Log smallest bounding rectangle for Turtle path.
	 */
	public void showExtent() {
		int i;
		double xmin=0,xmax=0,ymin=0,ymax=0;
		int first=1;
		for(i=0;i<history.size();i++) {
			TurtleMove mov=history.get(i);
			if(!mov.isUp) {
				if(first == 1 || mov.x < xmin) xmin=mov.x;
				if(first == 1 || mov.y < ymin) ymin=mov.y;
				if(first == 1 || mov.x > xmax) xmax=mov.x;
				if(first == 1 || mov.y > ymax) ymax=mov.y;
				first=0;
			}
		}
		Log.message("extent is ("+xmin+"/"+ymin+" "+xmax+"/"+ymax+" ");
	}
	
	// TODO move this to TurtleRenderer?
	public void render(TurtleRenderer tr) {
		if(isLocked()) return;
		try {
			lock();
			
			TurtleMove previousMove = null;
			
			// the first and last command to show (in case we want to isolate part of the drawing)
			int first = 0;
			int last = history.size();
			// where we're at in the drawing (to check if we're between first & last)
			int showCount = 0;
			
			tr.start();
			
			showCount++;

			for (TurtleMove m : history) {
				boolean inShow = (showCount >= first && showCount < last);
				if(m.isUp) {
					// travel move
					if (inShow && previousMove != null) {
						tr.travel(previousMove, m);
					}
					showCount++;
					previousMove = m;
				} else {
					// draw move
					if (inShow && previousMove != null) {
						tr.draw(previousMove, m);
					}
					showCount++;
					previousMove = m;
					break;
				}
			}
			
			tr.end();
		}
		catch(Exception e) {
			Log.error(e.getMessage());
		}
		finally {
			if(isLocked()) {
				unlock();
			}
		}
	}
}
