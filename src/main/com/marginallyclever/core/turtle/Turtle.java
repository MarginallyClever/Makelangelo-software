package com.marginallyclever.core.turtle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.marginallyclever.core.ColorRGB;
import com.marginallyclever.core.Point2D;
import com.marginallyclever.core.log.Log;


/**
 * A simple turtle implementation to make generating pictures and learning programming easier.
 * @author Dan Royer
 *
 */
public class Turtle implements Cloneable {
	public ArrayList<TurtleMove> history = new ArrayList<TurtleMove>();

	private ReentrantLock lock;

	// current state
	private double turtleX, turtleY;
	private double turtleDx, turtleDy;
	private double angle;
	private boolean isUp;
	
	// tail tip color.  only one per Turtle.
	private ColorRGB color;
	
	
	public Turtle() {
		super();
		lock = new ReentrantLock();

		// default turtle color is black.
		setColor(new ColorRGB(0,0,0));
		
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
		
		for( TurtleMove m : history ) {
			t.history.add(new TurtleMove(m));
		}
		return t;
	}
	
	/**
	 * Erases history and resets all parameters.  Called by constructor.
	 * Does not reset turtle tail color or radius.
	 */
	public void reset() {
		turtleX = 0;
		turtleY = 0;
		setAngle(0);
		penUp();
		history.clear();
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
		bottom.x=Double.MAX_VALUE;
		bottom.y=Double.MAX_VALUE;
		top.x=-Double.MAX_VALUE;
		top.y=-Double.MAX_VALUE;
		
		for( TurtleMove m : history ) {
			if(!m.isUp) {
				top.x = Math.max(top.x,m.x);
				top.y = Math.max(top.y,m.y);
				bottom.x = Math.min(bottom.x,m.x);
				bottom.y = Math.min(bottom.y,m.y);
			}
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
	
	public void render(TurtleRenderer tr) {
		if(isLocked()) return;
		try {
			lock();
						
			// the first and last command to show (in case we want to isolate part of the drawing)
			int first = 0;
			int last = history.size();
			if(last>0) {
				// where we're at in the drawing (to check if we're between first & last)
				int showCount = 0;
				
				tr.start();
					
				Iterator<TurtleMove> i = history.iterator();
				TurtleMove previousMove = i.next();
				while(i.hasNext()) {
					TurtleMove m = i.next();
					boolean inShow = (showCount >= first && showCount < last);
					if (inShow && previousMove != null) {
						if(m.isUp) {
							tr.travel(previousMove, m);
						} else {
							tr.draw(previousMove, m);
						}
					}
					showCount++;
					previousMove = m;
				}
				
				tr.end();
			}
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

	/**
	 * Find the bounding box of all turtles.
	 * @param turtles list of turtles to bound
	 * @param totalTop where to store the top/right corner
	 * @param totalBottom where to store the bottom/left corner
	 */
	static public void getBounds(List<Turtle> turtles,Point2D totalTop,Point2D totalBottom) {
		totalBottom.set(Double.MAX_VALUE,Double.MAX_VALUE);
		totalTop.set(-Double.MAX_VALUE,-Double.MAX_VALUE);
		Point2D bottom = new Point2D();
		Point2D top = new Point2D();

		for( Turtle t : turtles ) {
			t.getBounds(top, bottom);
			totalBottom.x = Math.min(bottom.x, totalBottom.x);
			totalBottom.y = Math.min(bottom.y, totalBottom.y);
			totalBottom.x = Math.min(top.x, totalBottom.x);
			totalBottom.y = Math.min(top.y, totalBottom.y);
			
			totalTop.x = Math.max(bottom.x, totalTop.x);
			totalTop.y = Math.max(bottom.y, totalTop.y);
			totalTop.x = Math.max(top.x, totalTop.x);
			totalTop.y = Math.max(top.y, totalTop.y);
		}
	}


	public void rotate(Double degrees) {
		double r = Math.toRadians(degrees);
		double c = Math.cos(r);
		double s = Math.sin(r);

		for( TurtleMove m : history ) {
			double x=m.x;
			double y=m.y;
			m.x = c*x - s*y;
			m.y = s*x + c*y;
		}
	}
}
