package com.marginallyclever.makelangelo.turtle;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;


/**
 * A {@link Turtle} is a collection of instructions which, combined, form a drawing on a 2D surface.
 * The name is based on the Commodore 64 turtle from the LOGO programming language, and movement is very similar.
 * Commands include:
 * <ul>
 *     <li>lifting and lowering the turtle's tail</li>
 *     <li>turning relative or absolute amounts</li>
 *     <li>moving forward or backward relative amounts</li>
 *     <li>moving relative or absolute amounts regardless of direction</li>
 *     <li>changing the tool (color and diameter)</li>
 * </ul>
 *
 * @author Dan Royer
 * @since 7.0?
 */
public class Turtle implements Cloneable {
	private static final Logger logger = LoggerFactory.getLogger(Turtle.class);
	
	public final List<TurtleMove> history = new ArrayList<>();

	private final transient ReentrantLock lock = new ReentrantLock();

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
		history.clear();
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
	 * Returns this {@link Turtle} to mint condition.  Erases history and resets all parameters.  Called by constructor.
	 * @param c The starting color for this {@link Turtle}.
	 */
	private void reset(ColorRGB c) {
		px = 0;
		py = 0;
		setAngle(0);
		penUp();
		history.clear();
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
		history.add( new TurtleMove(color.toInt(),diameter,MovementType.TOOL_CHANGE) );
	}
	
	public ColorRGB getColor() {
		return color;
	}
	
	public void setDiameter(double d) {
		if(diameter==d) return;
		diameter=d;
		history.add( new TurtleMove(color.toInt(),diameter,MovementType.TOOL_CHANGE) );
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
		history.add( new TurtleMove(x, y, isUp ? MovementType.TRAVEL : MovementType.DRAW_LINE) );
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
			if(m.type == MovementType.DRAW_LINE) {
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
			if ( m.type != MovementType.TOOL_CHANGE){
				old=m;
			}
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
				case DRAW_LINE, TRAVEL -> {
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
				case DRAW_LINE, TRAVEL -> {
					m.x += dx;
					m.y += dy;
				}
				default -> {}
			}
		}
	}

	/**
	 * Translate all draw and move segments by degrees
	 * @param degrees relative ccw rotation
	 */
	public void rotate(double degrees) {
		double r = Math.toRadians(degrees);
		double c = Math.cos(r);
		double s = Math.sin(r);

		for( TurtleMove m : history ) {
			switch (m.type) {
				case DRAW_LINE, TRAVEL -> {
					double ox=m.x;
					double oy=m.y;
					m.x = ox * c + oy * -s;
					m.y = ox * s + oy *  c;
				}
				default -> {}
			}
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
			if (mov.type == MovementType.DRAW_LINE) {
				if(first == 1 || mov.x < xmin) xmin=mov.x;
				if(first == 1 || mov.y < ymin) ymin=mov.y;
				if(first == 1 || mov.x > xmax) xmax=mov.x;
				if(first == 1 || mov.y > ymax) ymax=mov.y;
				first=0;
			}
		}
		logger.debug("extent is ({}/{} {}/{}", xmin, ymin, xmax, ymax);
	}

	/**
	 * @return a list of all the pen-down lines while remembering their color.
 	 */
	public List<LineSegment2D> getAsLineSegments() {
		ArrayList<LineSegment2D> lines = new ArrayList<>();
		TurtleMove previousMovement=null;
		ColorRGB color = new ColorRGB(0,0,0);

		logger.debug("  Found {} instructions.", history.size());
		
		for( TurtleMove m : history ) {
			switch (m.type) {
				case DRAW_LINE -> {
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
				case TRAVEL -> previousMovement = m;
				case TOOL_CHANGE -> color = m.getColor();
			}
		}

		return lines;
	}

	/**
	 * Calls {@code addLineSegments} with a default minimum jump size.
	 * @param segments the list of line segments to add.
	 */
	public void addLineSegments(List<LineSegment2D> segments) {
		addLineSegments(segments,1e-6,1e-6);
	}

	/**
	 * Appends the list of segments to this {@link Turtle}.
	 * @param segments the ordered list of segments to add.
	 * @param minimumJumpSize For any {@link LineSegment2D} N being added, the Turtle will jump if N.b and (N+1).a are more than minimumJumpSize apart.
	 * @param minDrawDistance For any {@link LineSegment2D} N being added, the Turtle will not draw line where N.b-N.a is less than minDrawDistance.
	 */
	public void addLineSegments(List<LineSegment2D> segments, double minimumJumpSize, double minDrawDistance) {
		if(segments.isEmpty()) return;
		
		LineSegment2D first = segments.get(0);
		jumpTo(first.start.x,first.start.y);
		moveTo(first.end.x,first.end.y);
		
		double minJumpSquared = minimumJumpSize*minimumJumpSize;
		double minDrawSquared = minDrawDistance*minDrawDistance;
		
		for( LineSegment2D line : segments ) {
			// change color if needed
			if(line.color !=getColor()) {
				setColor(line.color);
			}

			double d = distanceSquared(line.start);
			if(d > minJumpSquared) {
				// The previous line ends too far from the start point of this line,
				// need to make a travel with the pen up to the start point of this line.
				jumpTo(line.start.x,line.start.y);
			} else if(d>minDrawSquared) {
				moveTo(line.start.x,line.start.y);
			}
			// Make a pen down move to the end of this line
			moveTo(line.end.x,line.end.y);
		}
	}

	private double distanceSquared(Point2D b) {
		double dx = px-b.x;
		double dy = py-b.y;
		return dx*dx + dy*dy; 
	}
	
	public List<Turtle> splitByToolChange() {
		List<Turtle> list = new ArrayList<>();
		Turtle t = new Turtle();
		list.add(t);
		
		for( TurtleMove m : history) {
			if(m.type==MovementType.TOOL_CHANGE) {
				t = new Turtle();
				t.history.clear();
				list.add(t);
			}
			t.history.add(m);
		}
		//logger.debug("Turtle.splitByToolChange() into {} sections.", list.size());

		List<Turtle> notEmptyList = new ArrayList<>();
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
			if(m.type==MovementType.DRAW_LINE) return true;
		}
		return false;
	}

	public void add(Turtle t) {
		this.history.addAll(t.history);
	}

	public ColorRGB getFirstColor() {
		for( TurtleMove m : history) {
			if(m.type==MovementType.TOOL_CHANGE)
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
		TurtleMove prev = new TurtleMove(0,0,MovementType.TRAVEL);
		for( TurtleMove m : history) {
			if(m.type == MovementType.DRAW_LINE) {
				double dx = m.x-prev.x;
				double dy = m.y-prev.y;
				d += Math.sqrt(dx*dx+dy*dy);
				prev = m;
			} else if(m.type == MovementType.TRAVEL) {
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
		TurtleMove prev = new TurtleMove(0,0,MovementType.TRAVEL);
		for( TurtleMove m : history) {
			if(m.type == MovementType.DRAW_LINE) {
				double dx = m.x-prev.x;
				double dy = m.y-prev.y;
				double change = Math.sqrt(dx*dx+dy*dy);
				if(d+change>=t) {
					double v = (t-d==0)? 0 : change/(t-d);
					v = Math.max(Math.min(v,1),0);
					return new Point2D(
							prev.x + dx * v,
							prev.y + dy * v);
				}
				d += change;
				prev = m;
			} else if(m.type == MovementType.TRAVEL) {
				prev = m;
			}
		}
		return new Point2D(prev.x,prev.y);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Turtle turtle = (Turtle) o;
		return Double.compare(turtle.px, px) == 0 &&
				Double.compare(turtle.py, py) == 0 &&
				Double.compare(turtle.nx, nx) == 0 &&
				Double.compare(turtle.ny, ny) == 0 &&
				Double.compare(turtle.angle, angle) == 0 &&
				isUp == turtle.isUp &&
				Double.compare(turtle.diameter, diameter) == 0 &&
				history.equals(turtle.history) &&
				Objects.equals(color, turtle.color);
	}

	@Override
	public int hashCode() {
		return Objects.hash(history, px, py, nx, ny, angle, isUp, color, diameter);
	}
}
