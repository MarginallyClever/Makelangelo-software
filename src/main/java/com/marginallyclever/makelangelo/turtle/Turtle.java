package com.marginallyclever.makelangelo.turtle;

import com.marginallyclever.convenience.LineCollection;
import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.Point2D;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.security.InvalidParameterException;
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
 * The turtle's starting angle is 0 degrees, which is to the right.  The turtle starts with the tail down.
 *
 * @author Dan Royer
 * @since 7.0?
 */
public class Turtle implements Cloneable {
	public final List<TurtleMove> history = new ArrayList<>();
	private final transient ReentrantLock lock = new ReentrantLock();

	// current state
	private double px, py;
	private double nx, ny;  // normal of angle. aka sin() and cos() of angle.
	private double angle;
	private boolean isUp;
	private Color color;
	private double diameter=1;

	public Turtle() {
		super();
		reset(Color.BLACK);
	}
	
	public Turtle(Turtle t) {
		this();
		this.px = t.px;
		this.py = t.py;
		this.nx = t.nx;
		this.ny = t.ny;
		this.angle = t.angle;
		this.isUp = t.isUp;
		this.color=t.color;
		this.diameter = t.diameter; 
		// deep copy
		history.clear();
		for( TurtleMove m : t.history ) {
			this.history.add(new TurtleMove(m));
		}
	}
	
	public Turtle(Color firstColor) {
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
				"history=" + history.size() +
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
	private void reset(Color c) {
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

	public void setColor(Color c) {
		if(color!=null) {
			if(color.getRed()==c.getRed() &&
				color.getGreen()==c.getGreen() &&
				color.getBlue()==c.getBlue()) return;
		}
		color = c;
		history.add( new TurtleMove(color.hashCode(),diameter,MovementType.TOOL_CHANGE) );
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setDiameter(double d) {
		if(diameter==d) return;
		diameter=d;
		history.add( new TurtleMove(color.hashCode(),diameter,MovementType.TOOL_CHANGE) );
	}
	
	public double getDiameter() {
		return diameter;
	}
	
	/**
	 * Absolute position change. Raise the pen before move and lower pen after move.
	 * @param x absolute x position
	 * @param y absolute y position
	 */
	public void jumpTo(double x,double y) {
		penUp();
		moveTo(x,y);
		penDown();
	}
	
	/**
	 * Absolute position change, do not change current pen status
	 * @param x absolute x position
	 * @param y absolute y position
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
	 * @param degreesCCW relative change in degrees.  Positive is counter clockwise.
	 */
	public void turn(double degreesCCW) {
		setAngle(angle+degreesCCW);
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
	public void strafe(double distance) {
		moveTo(
			px + ny * distance,
			py - nx * distance
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
		TurtleMove lastTravelMove=null;
		
		int hits=0;

		for( TurtleMove m : history ) {
			switch(m.type) {
				case TRAVEL -> {
					lastTravelMove = m;
				}
				case DRAW_LINE -> {
					if (lastTravelMove != null) {
						hits++;
						getBoundsInternal(top,bottom,lastTravelMove);
						lastTravelMove = null;
					}
					hits++;
					getBoundsInternal(top,bottom,m);
				}
			}
		}
		
		if(hits==0) {
			bottom.set(0,0);
			top.set(0,0);
		}
	}

	private void getBoundsInternal(Point2D top,Point2D bottom,TurtleMove m) {
		if (top.x < m.x) top.x = m.x;
		if (top.y < m.y) top.y = m.y;
		if (bottom.x > m.x) bottom.x = m.x;
		if (bottom.y > m.y) bottom.y = m.y;
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
	 * Draw an arc.
	 * @param cx absolute center of arc
	 * @param cy absolute center of arc
	 * @param radius of arc
	 * @param a1 start angle, in radians
	 * @param a2 end angle, in radians
	 * @param steps must be greater than zero.
	 */
	public void drawArc(double cx, double cy, double radius, double a1, double a2,int steps) {
		if(steps<=0) throw new InvalidParameterException("steps must be greater than zero.");

		double delta = (a2 - a1) / (double) steps;

		for (int i = 0; i <= steps; i++) {
			double f = a1 + delta * i;
			double x2 = cx + Math.cos(f) * radius;
			double y2 = cy + Math.sin(f) * radius;
			if(i==0) this.jumpTo(x2, y2);
			else     this.moveTo(x2, y2);
		}
	}

	/**
	 * @return a list of all the pen-down lines while remembering their color.
 	 */
	public LineCollection getAsLineSegments() {
		LineCollection lines = new LineCollection();
		TurtleMove previousMovement=null;
		Color color = Color.BLACK;

		//logger.debug("  Found {} instructions.", history.size());
		
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
	public void addLineSegments(LineCollection segments) {
		addLineSegments(segments,1e-6,1e-6);
	}

	/**
	 * Appends the list of segments to this {@link Turtle}.
	 * @param segments the ordered list of segments to add.
	 * @param minimumJumpSize For any {@link LineSegment2D} N being added, the Turtle will jump if N.b and (N+1).a are more than minimumJumpSize apart.
	 * @param minDrawDistance For any {@link LineSegment2D} N being added, the Turtle will not draw line where N.b-N.a is less than minDrawDistance.
	 */
	public void addLineSegments(LineCollection segments, double minimumJumpSize, double minDrawDistance) {
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
		TurtleMove lastToolChange = null;
		
		for( TurtleMove m : history) {
			if(m.type==MovementType.TOOL_CHANGE) {
				if(lastToolChange==null
						|| !lastToolChange.getColor().equals(m.getColor())
						|| lastToolChange.getDiameter() != m.getDiameter() ) {
					t = new Turtle();
					t.history.clear();
					list.add(t);
					lastToolChange = m;
				}
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
		//logger.debug("Turtle.splitByToolChange() {} not-empty sections.", notEmptyList.size());
		
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

	public Color getFirstColor() {
		for( TurtleMove m : history) {
			if(m.type==MovementType.TOOL_CHANGE)
				return m.getColor();
		}
		
		return Color.BLACK;
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
				if(d+change>=t) {  // d < t < d+change
					double v = (t-d==0)? 0 : (t-d) / change;
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

	public Vector2d getHeading() {
		return new Vector2d(nx,ny);
	}

	public Vector2d getPosition() {
		return new Vector2d(px,py);
	}


	/**
	 * @return the number of times the pen is lowered to draw a line.
	 */
	public int countLoops() {
		int sum=0;
		MovementType before = MovementType.TRAVEL;

		for( TurtleMove m : history) {
			if(m.type==before) continue;
			if(m.type==MovementType.DRAW_LINE && before==MovementType.TRAVEL) {
				sum++;
			}
			before = m.type;
		}
		return sum;
	}
}
