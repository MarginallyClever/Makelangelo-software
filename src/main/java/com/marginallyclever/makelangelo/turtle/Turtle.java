package com.marginallyclever.makelangelo.turtle;

import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.convenience.linecollection.LineCollection;

import javax.annotation.Nonnull;
import javax.vecmath.Point2d;
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
	public static final double DEFAULT_DIAMETER = 1.0;

	private final List<StrokeLayer> strokeLayers = new ArrayList<>();

	private final transient ReentrantLock lock = new ReentrantLock();

	// current state
	private final Point2d p = new Point2d();
	private final Vector2d n = new Vector2d();  // normal of angle. aka sin() and cos() of angle.
	private double angle;
	private boolean isUp;
	private Color color;
	private double diameter = 1;

	public Turtle() {
		super();
		reset(Color.BLACK,DEFAULT_DIAMETER);
	}
	
	public Turtle(Turtle t) {
		this();
		set(t);
	}

	public void set(Turtle t) {
		this.p.set(t.p);
		this.n.set(t.n);
		this.angle = t.angle;
		this.isUp = t.isUp;
		this.color = t.color;
		this.diameter = t.diameter;

		deepCopyStrokeLayers(t);
	}
	
	public Turtle(Color firstColor) {
		super();
		reset(firstColor,DEFAULT_DIAMETER);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Turtle t = (Turtle)super.clone();
		deepCopyStrokeLayers(t);
		return t;
	}

	private void deepCopyStrokeLayers(Turtle t) {
		this.strokeLayers.clear();
		for( StrokeLayer cl : t.strokeLayers) {
			this.strokeLayers.add(new StrokeLayer(cl));
		}
	}

	@Override
	public String toString() {
		return "Turtle{" +
				"history=" + countPoints() +
				", p=" + p +
				", n=" + n +
				", angle=" + angle +
				", isUp=" + isUp +
				", color=" + color +
				", diameter=" + diameter +
				'}';
	}

	public int countPoints() {
		int sum = 0;
		for (var cl : strokeLayers) {
			for (var line : cl.getAllLines()) {
				sum += line.size();
			}
		}
		return sum;
	}

	/**
	 * Returns this {@link Turtle} to mint condition.  Erases history and resets all parameters.  Called by constructor.
	 * @param c The starting color for this {@link Turtle}.
	 * @param d The starting diameter for this {@link Turtle}.
	 */
	private void reset(Color c,double d) {
		p.set(0,0);
		setAngle(0);
		penUp();
		setStroke(c,d);
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

	/**
	 * Set the color of the pen.  Creates a new {@link StrokeLayer} if needed.
	 *
	 * @param color        the new color
	 * @param diameter the new diameter
	 */
	public void setStroke(@Nonnull Color color, double diameter) {
		if(this.color != null && this.color.equals(color) && this.diameter == diameter ) return;
		this.color = color;
		this.diameter = diameter;
		strokeLayers.add(new StrokeLayer(this.color, this.diameter));
		if(!isUp) penDown();
	}

	/**
	 * Change the color without altering the diameter.
	 * @param color the new color
	 */
	public void setStroke(Color color) {
		setStroke(color,diameter);
	}
	
	public Color getColor() {
		return color;
	}

	public double getDiameter() {
		return diameter;
	}

	public void setDiameter(double diameter) {
		setStroke(this.color,diameter);
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
		p.set(x,y);
		if(!isUp) {
			var layer = strokeLayers.getLast();
			if(layer.isEmpty()) {
				layer.add(new Line2d());
			}
			layer.getLast().add(new Point2d(x,y));
		}
	}

	/**
	 * @return the current x position of the turtle
	 */
	public double getX() {
		return p.x;
	}

	/**
	 * @return the current y position of the turtle
	 */
	public double getY() {
		return p.y;
	}

	/**
	 * Changes the pen status to up.  Does not add to history.
	 */
	public void penUp() {
		isUp=true;
	}

	/**
	 * Changes the pen status to down.  Does not add to history.
	 */
	public void penDown() {
		if(isUp) {
			isUp = false;
			var layer = strokeLayers.getLast();
			layer.add(new Line2d());
			layer.getLast().add(new Point2d(p.x, p.y));
		}
	}
	
	/**
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
		n.set(Math.cos(radians), Math.sin(radians));
	}

	/**
	 * Relative move forward/back
	 * @param distance how far to travel
	 */
	public void forward(double distance) {
		moveTo(
			p.x + n.x * distance,
			p.y + n.y * distance
		);
	}
	public void strafe(double distance) {
		moveTo(
			p.x + n.y * distance,
			p.y - n.x * distance
		);
	}

	/**
	 * Calculate the limits of drawing lines in this turtle history
	 **/
	public Rectangle2D.Double getBounds() {
		Point2d top = new Point2d(-Double.MAX_VALUE, -Double.MAX_VALUE);
		Point2d bottom = new Point2d(Double.MAX_VALUE, Double.MAX_VALUE);

		int hits = 0;
		for (var cl : strokeLayers) {
			for (var line : cl.getAllLines()) {
				for (var point : line.getAllPoints()) {
					hits++;
					if (top.x < point.x) top.x = point.x;
					if (top.y < point.y) top.y = point.y;
					if (bottom.x > point.x) bottom.x = point.x;
					if (bottom.y > point.y) bottom.y = point.y;
				}
			}
		}

		if (hits == 0) {
			bottom.set(0, 0);
			top.set(0, 0);
		}

		return new Rectangle2D.Double(bottom.x, bottom.y, top.x - bottom.x, top.y - bottom.y);
	}

	/**
	 * Scale all draw and move segments by the given amounts
	 * @param sx the x-axis scale factor.
	 * @param sy the y-axis scale factor.
	 */
	public void scale(double sx, double sy) {
		for (var cl : strokeLayers) {
			for (var line : cl.getAllLines()) {
				for (var point : line.getAllPoints()) {
					point.x *= sx;
					point.y *= sy;
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
		for (var cl : strokeLayers) {
			for (var line : cl.getAllLines()) {
				for (var point : line.getAllPoints()) {
					point.x += dx;
					point.y += dy;
				}
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

		for (var cl : strokeLayers) {
			for (var line : cl.getAllLines()) {
				for (var point : line.getAllPoints()) {
					double ox = point.x;
					double oy = point.y;
					point.x = ox * c + oy * -s;
					point.y = ox * s + oy * c;
				}
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
	 * @return a list of all the pen down lines while remembering their color.
 	 */
	public LineCollection getAsLineCollection() {
		LineCollection result = new LineCollection();

		for (var cl : strokeLayers) {
			for (var line : cl.getAllLines()) {
				if(line.size()<2) continue;
				var iter = line.iterator();
				Point2d prev = iter.next();
				while(iter.hasNext()) {
					Point2d next = iter.next();
					LineSegment2D segment = new LineSegment2D(
							new Point2d(prev.x, prev.y),
							new Point2d(next.x, next.y),
							cl.getColor());
					if (segment.lengthSquared() > 0) {
						result.add(segment);
					}
					prev = next;
				}
			}
		}

		return result;
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

		lock();
		try {
			LineSegment2D first = segments.getFirst();
			if(first == null) {
				return;
			}
			jumpTo(first.start.x, first.start.y);
			moveTo(first.end.x, first.end.y);

			double minJumpSquared = minimumJumpSize * minimumJumpSize;
			double minDrawSquared = minDrawDistance * minDrawDistance;

			for (LineSegment2D line : segments) {
				// change color if needed
				if (line.color != getColor()) {
					setStroke(line.color,DEFAULT_DIAMETER);
				}

				double d = distanceSquared(line.start);
				if (d > minJumpSquared) {
					// The previous line ends too far from the start point of this line,
					// need to make a travel move with the pen up to the start point of this line.
					jumpTo(line.start.x, line.start.y);
				} else if (d > minDrawSquared) {
					moveTo(line.start.x, line.start.y);
				}
				// Make a pen down move to the end of this line
				moveTo(line.end.x, line.end.y);
			}
		} finally {
			unlock();
		}
	}

	private double distanceSquared(Point2d b) {
		double dx = p.x - b.x;
		double dy = p.y - b.y;
		return dx*dx + dy*dy; 
	}
	
	public List<Turtle> splitByToolChange() {
		List<Turtle> result = new ArrayList<>();

		for (var cl : strokeLayers) {
			if(cl.isEmpty()) continue;
			Turtle t = new Turtle();
			t.strokeLayers.add(cl);
			t.color = cl.getColor();
			result.add(t);
		}

		//logger.debug("Turtle.splitByToolChange() {} not-empty sections.", result.size());
		
		return result;
	}
	
	public boolean getHasAnyDrawingMoves() {
		for (var cl : strokeLayers) {
			for (var line : cl.getAllLines()) {
				if(!line.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	public void add(Turtle t) {
		lock.lock();
		try {
			// add all the lines from the other turtle
			for (var cl : t.strokeLayers) {
				StrokeLayer newLayer = new StrokeLayer(cl);
				strokeLayers.add(newLayer);
			}
		} finally {
			lock.unlock();
		}
	}

	public Color getFirstColor() {
		if(strokeLayers.isEmpty()) return Color.BLACK;
		StrokeLayer cl = strokeLayers.getFirst();
		return cl.getColor();
	}

	/**
	 * Returns the total distance of all pen down moves within this {@link Turtle}.
	 * @return the total distance of all pen down moves within this {@link Turtle}.
	 */
    public double getDrawDistance() {
		double sum = 0;
		for (var cl : strokeLayers) {
			for (var line : cl.getAllLines()) {
				if(line.size()<2) continue;
				var iter = line.iterator();
				Point2d prev = iter.next();
				while(iter.hasNext()) {
					Point2d next = iter.next();
					double dx = next.x - prev.x;
					double dy = next.y - prev.y;
					sum += Math.sqrt(dx*dx+dy*dy);
					prev = next;
				}
			}
		}
		return sum;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Turtle turtle = (Turtle) o;
		return  Double.compare(turtle.p.x, p.x) == 0 &&
				Double.compare(turtle.p.y, p.y) == 0 &&
				Double.compare(turtle.n.x, n.x) == 0 &&
				Double.compare(turtle.n.y, n.y) == 0 &&
				Double.compare(turtle.angle, angle) == 0 &&
				isUp == turtle.isUp &&
				Double.compare(turtle.diameter, diameter) == 0 &&
				strokeLayers.equals(turtle.strokeLayers) &&
				Objects.equals(color, turtle.color);
	}

	@Override
	public int hashCode() {
		return Objects.hash(strokeLayers, p.x, p.y, n.x, n.y, angle, isUp, color, diameter);
	}

	public Vector2d getHeading() {
		return new Vector2d(n);
	}

	public Point2d getPosition() {
		return new Point2d(p);
	}


	/**
	 * @return the number of times the pen is lowered to draw a line.
	 */
	public int countLoops() {
		int sum = 0;

		for (var cl : strokeLayers) {
			sum += cl.getAllLines().size();
		}

		return sum;
	}

	/**
	 * @return true if the turtle has any drawing moves.
	 */
	public boolean hasDrawing() {
		for (var cl : strokeLayers) {
			if(!cl.getAllLines().isEmpty()) return true;
		}
		return false;
	}

	/**
	 * @return a new {@link TurtleIterator} which can be used to iterate over the drawing moves in this {@link Turtle}.
	 */
	public TurtleIterator getIterator() {
		return new TurtleIterator(this);
	}

	public String generateHistory() {
		StringBuilder sb = new StringBuilder();
		TurtleIterator iter = getIterator();
		String add ="";
		while(iter.hasNext()) {
			Point2d p = iter.next();

			if(iter.isToolChange()) {
				sb.append(add);
				add = ", ";
				Color c = iter.getLayer().getColor();
				sb.append("TOOL")
						.append(" R").append(c.getRed())
						.append(" G").append(c.getGreen())
						.append(" B").append(c.getBlue())
						.append(" A").append(c.getAlpha())
						.append(" D").append(StringHelper.formatDouble(iter.getLayer().getDiameter()));
			}

			sb.append(add);
			add = ", ";
			sb.append(iter.isTravel() ? "TRAVEL" : "DRAW_LINE")
			  .append(" X").append(StringHelper.formatDouble(p.x))
			  .append(" Y").append(StringHelper.formatDouble(p.y));
		}

		return "[" + sb + "]";
	}

	public List<StrokeLayer> getLayers() {
		return strokeLayers;
	}
}
