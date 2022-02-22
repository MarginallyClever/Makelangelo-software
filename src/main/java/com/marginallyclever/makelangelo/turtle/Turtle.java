package com.marginallyclever.makelangelo.turtle;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * A simple turtle implementation to make generating pictures and learning programming easier.
 * @author Dan Royer
 *
 */
public class Turtle implements Cloneable {
	private static final Logger logger = LoggerFactory.getLogger(Turtle.class);
	
	public List<TurtleMove> history;

	private final ReentrantLock lock = new ReentrantLock();

	// current state
	private double px, py;
	private double nx, ny;  // normal of angle. aka sin() and cos() of angle.
	private double angle;
	private boolean isUp;
	private ColorRGB color;
	private double diameter=1;

	public Turtle() {
		super();
		reset(new ColorRGB(0,0,0));
	}
	
	public Turtle(Turtle t) {
		this();
		this.px = t.px;
		this.py = t.py;
		this.nx = t.nx;
		this.ny = t.ny;
		this.angle = t.angle;
		this.isUp = t.isUp;
		this.color.set(t.color);
		this.diameter = t.diameter; 
		// deep copy
		for( TurtleMove m : t.history ) {
			this.history.add(new TurtleMove(m));
		}
	}
	
	public Turtle(ColorRGB firstColor) {
		super();
		reset(firstColor);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Turtle t = (Turtle)super.clone();
		for( TurtleMove m : history ) {
			t.history.add(new TurtleMove(m));
		}
		return t;
	}

	@Override
	public String toString() {
		return "Turtle{" +
				"history=" + history +
				", px=" + px +
				", py=" + py +
				", nx=" + nx +
				", ny=" + ny +
				", angle=" + angle +
				", isUp=" + isUp +
				", color=" + color +
				", diameter=" + diameter +
				'}';
	}

	/**
	 * Return this Turtle to mint condition.  Erases history and resets all parameters.  Called by constructor.
	 */
	private void reset(ColorRGB c) {
		px = 0;
		py = 0;
		setAngle(0);
		penUp();
		history = new ArrayList<>();
		// default turtle color is black.
		setColor(c);
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
		}
		color = new ColorRGB(c);
		history.add( new TurtleMove(color.toInt(),diameter,TurtleMove.TOOL_CHANGE) );
	}
	
	public ColorRGB getColor() {
		return color;
	}
	
	public void setDiameter(double d) {
		if(diameter==d) return;
		diameter=d;
		history.add( new TurtleMove(color.toInt(),diameter,TurtleMove.TOOL_CHANGE) );
	}
	
	public double getDiameter() {
		return diameter;
	}
	
	/**
	 * Absolute position change, make sure pen is up before move and put pen down after move.
	 * @param x absolute x position
	 * @param y absolute y position
	 */
	public void jumpTo(double x,double y) {
		penUp();
		moveTo(x,y);
		penDown();
	}
	
	/**
	 * Absolute position change, do not adjust pen status
	 * @param x relative x position
	 * @param y relative y position
	 */
	public void moveTo(double x,double y) {
		px=x;
		py=y;
		history.add( new TurtleMove(x, y, isUp ? TurtleMove.TRAVEL : TurtleMove.DRAW_LINE) );
	}
		
	/**
	 * Absolute position
	 * @param arg0 x axis
	 */
	public void setX(double arg0) {
		moveTo(arg0,py);
	}
	
	/**
	 * Absolute position
	 * @param arg0 y axis
	 */
	public void setY(double arg0) {
		moveTo(px,arg0);
	}
	
	public double getX() {
		return px;
	}
	
	public double getY() {
		return py;
	}
	
	public void penUp() {
		isUp=true;
	}
	
	public void penDown() {
		isUp=false;
	}
	
	/**
	 * Returns true if pen is up.
	 * @return true if pen is up
	 */
	public boolean isUp() {
		return isUp;
	}

	/**
	 * Relative turn in degrees.
	 * @param degrees relative change in degrees.  Positive is counter clockwise.
	 */
	public void turn(double degrees) {
		setAngle(angle+degrees);
	}

	// Get absolute angle degrees
	public double getAngle() {
		return angle;
	}
	
	/**
	 * Set absolute angle in degrees.
	 * @param degrees absolute degrees.
	 */
	public void setAngle(double degrees) {
		angle=degrees;
		double radians=Math.toRadians(angle);
		nx = Math.cos(radians);
		ny = Math.sin(radians);
	}

	/**
	 * Relative move forward/back
	 * @param distance how far to travel
	 */
	public void forward(double distance) {
		moveTo(
			px + nx * distance,
			py + ny * distance
		);
	}

	/**
	 * Calculate the limits of drawing lines in this turtle history
	 **/
	public Rectangle2D.Double getBounds() {
		Point2D top = new Point2D();
		Point2D bottom = new Point2D();
		getBounds(top,bottom);
		
		Rectangle2D.Double r = new Rectangle.Double();
		r.x=bottom.x;
		r.y=bottom.y;
		r.width=top.x-bottom.x;
		r.height=top.y-bottom.y;
		
		return r;
	}
        
	/**
	 * Calculate the limits of drawing lines in this turtle history
	 * @param top maximum limits
	 * @param bottom minimum limits
	 */
	private void getBounds(Point2D top,Point2D bottom) {
		bottom.x=Float.MAX_VALUE;
		bottom.y=Float.MAX_VALUE;
		top.x=-Float.MAX_VALUE;
		top.y=-Float.MAX_VALUE;
		TurtleMove old=null;
		
		int hits=0;
		
		for( TurtleMove m : history ) {
			if(m.type == TurtleMove.DRAW_LINE) {
				hits++;
				if(top.x<m.x) top.x=m.x;
				if(top.y<m.y) top.y=m.y;
				if(bottom.x>m.x) bottom.x=m.x;
				if(bottom.y>m.y) bottom.y=m.y;
				if(old != null) {
					if(top.x<old.x) top.x=old.x;
					if(top.y<old.y) top.y=old.y;
					if(bottom.x>old.x) bottom.x=old.x;
					if(bottom.y>old.y) bottom.y=old.y;
				}
			}
			old=m;
		}
		
		if(hits==0) {
			bottom.set(0,0);
			top.set(0,0);
		}
	}

	/**
	 * Scale all draw and move segments by the given amounts
	 * @param sx the x axis scale factor.
	 * @param sy the y axis scale factor.
	 */
	public void scale(double sx, double sy) {
		for( TurtleMove m : history ) {
			switch (m.type) {
				case TurtleMove.DRAW_LINE, TurtleMove.TRAVEL -> {
					m.x *= sx;
					m.y *= sy;
				}
				default -> {
				}
			}
		}
	}

	/**
	 * Translate all draw and move segments by parameters
	 * @param dx relative move x
	 * @param dy relative move y
	 */
	public void translate(double dx, double dy) {
		for( TurtleMove m : history ) {
			switch (m.type) {
				case TurtleMove.DRAW_LINE, TurtleMove.TRAVEL -> {
					m.x += dx;
					m.y += dy;
				}
				default -> {
				}
			}
		}
	}

	/**
	 * @return a list of all the pen-down lines while remembering their color.
 	 */
	public ArrayList<LineSegment2D> getAsLineSegments() {
		ArrayList<LineSegment2D> lines = new ArrayList<>();
		TurtleMove previousMovement=null;
		ColorRGB color = new ColorRGB(0,0,0);

		logger.debug("  Found {} instructions.", history.size());
		
		for( TurtleMove m : history ) {
			switch (m.type) {
				case TurtleMove.DRAW_LINE -> {
					if (previousMovement != null) {
						LineSegment2D line = new LineSegment2D(
								new Point2D(previousMovement.x, previousMovement.y),
								new Point2D(m.x, m.y),
								color);
						if (line.lengthSquared() > 0) {
							lines.add(line);
						}
					}
					previousMovement = m;
				}
				case TurtleMove.TRAVEL -> previousMovement = m;
				case TurtleMove.TOOL_CHANGE -> color = m.getColor();
			}
		}

		return lines;
	}

	/**
	 * Calls {@code addLineSegments} with a default minimum jump size.
	 * @param segments the list of line segments to add.
	 */
	public void addLineSegments(ArrayList<LineSegment2D> segments) {
		addLineSegments(segments,1e-6,1e-6);
	}

	/**
	 * Appends the list of segments to this {@link Turtle}.
	 * @param segments the ordered list of segments to add.
	 * @param minimumJumpSize For any {@link LineSegment2D} N being added, the Turtle will jump if N.b and (N+1).a are more than minimumJumpSize apart.
	 * @param minDrawDistance For any {@link LineSegment2D} N being added, the Turtle will not draw line where N.b-N.a is less than minDrawDistance.
	 */
	public void addLineSegments(ArrayList<LineSegment2D> segments, double minimumJumpSize, double minDrawDistance) {
		if(segments.isEmpty()) return;
		
		LineSegment2D first = segments.get(0);
		jumpTo(first.a.x,first.a.y);
		moveTo(first.b.x,first.b.y);
		
		double minJumpSquared = minimumJumpSize*minimumJumpSize;
		double minDrawSquared = minDrawDistance*minDrawDistance;
		
		for( LineSegment2D line : segments ) {
			// change color if needed
			if(line.c!=getColor()) {
				setColor(line.c);
			}

			double d = distanceSquared(line.a);
			if(d > minJumpSquared) {
				// The previous line ends too far from the start point of this line,
				// need to make a travel with the pen up to the start point of this line.
				jumpTo(line.a.x,line.a.y);
			} else if(d>minDrawSquared) {
				moveTo(line.a.x,line.a.y);
			}
			// Make a pen down move to the end of this line
			moveTo(line.b.x,line.b.y);
		}
	}

	private double distanceSquared(Point2D b) {
		double dx = px-b.x;
		double dy = py-b.y;
		return dx*dx + dy*dy; 
	}
	
	public ArrayList<Turtle> splitByToolChange() {
		ArrayList<Turtle> list = new ArrayList<>();
		Turtle t = new Turtle();
		list.add(t);
		
		for( TurtleMove m : history) {
			if(m.type==TurtleMove.TOOL_CHANGE) {
				t = new Turtle();
				t.history.clear();
				list.add(t);
			}
			t.history.add(m);
		}
		logger.debug("Turtle.splitByToolChange() into {} sections.", list.size());

		ArrayList<Turtle> notEmptyList = new ArrayList<>();
		for( Turtle t2 : list ) {
			if(t2.getHasAnyDrawingMoves()) {
				notEmptyList.add(t2);
			}
		}
		logger.debug("Turtle.splitByToolChange() {} not-empty sections.", notEmptyList.size());
		
		return notEmptyList;
	}
	
	public boolean getHasAnyDrawingMoves() {
		for( TurtleMove m : history) {
			if(m.type==TurtleMove.DRAW_LINE) return true;
		}
		return false;
	}

	public void add(Turtle t) {
		this.history.addAll(t.history);
	}

	public ColorRGB getFirstColor() {
		for( TurtleMove m : history) {
			if(m.type==TurtleMove.TOOL_CHANGE)
				return m.getColor();
		}
		
		return new ColorRGB(0,0,0);
	}

	/**
	 * Returns the total distance of all pen-down moves within this {@link Turtle}.
	 * @return the total distance of all pen-down moves within this {@link Turtle}.
	 */
    public double getDrawDistance() {
		double d=0;
		TurtleMove prev = new TurtleMove(0,0,TurtleMove.TRAVEL);
		for( TurtleMove m : history) {
			if(m.type == TurtleMove.DRAW_LINE) {
				double dx = m.x-prev.x;
				double dy = m.y-prev.y;
				d += Math.sqrt(dx*dx+dy*dy);
				prev = m;
			} else if(m.type == TurtleMove.TRAVEL) {
				prev = m;
			}
		}
		return d;
    }

	/**
	 * Returns a point along the drawn lines of this {@link Turtle}
	 * @param t a value from 0...{@link Turtle#getDrawDistance()}, inclusive.
	 * @return a point along the drawn lines of this {@link Turtle}
	 */
	public Point2D interpolate(double t) {
		double d=0;
		TurtleMove prev = new TurtleMove(0,0,TurtleMove.TRAVEL);
		for( TurtleMove m : history) {
			if(m.type == TurtleMove.DRAW_LINE) {
				double dx = m.x-prev.x;
				double dy = m.y-prev.y;
				double change = Math.sqrt(dx*dx+dy*dy);
				if(d+change>=t) {
					double v = (t-d)/change;
					v = Math.max(Math.min(v,1),0);
					return new Point2D(
							prev.x + dx * v,
							prev.y + dy * v);
				}
				d += change;
				prev = m;
			} else if(m.type == TurtleMove.TRAVEL) {
				prev = m;
			}
		}
		return new Point2D(prev.x,prev.y);
	}
}
