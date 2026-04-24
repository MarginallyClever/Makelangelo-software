package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.linecollection.LineCollection;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Take an existing drawing, scan across it horizontally.  Add new lines between every pair of lines found.
 * It may sometimes make mistakes if it hits the very end of a line.
 * @author Dan Royer
 * @since 7.31.0
 */
public class InfillTurtle {
	private static final Logger logger = LoggerFactory.getLogger(InfillTurtle.class);

	public static final double MINIMUM_PEN_DIAMETER = 0.1;

	private double penDiameter = 0.8;

	/**
	 * Direction of lines to draw. 0 is horizontal. 90 is vertical.
	 */
	private double angle = 90.0;

	public InfillTurtle() {}

	public Turtle run(Turtle input) {
		// confirmTurtleIsClosedLoop(input);

		Turtle result = new Turtle();

		List<Turtle> list = input.splitByToolChange();
		for(Turtle t : list) {
			LineCollection segments = infillFromTurtle(t);
			//result.setStroke(t.getColor(), t.getDiameter());
			result.addLineSegments(segments);
		}


		return result;
	}

	@SuppressWarnings("unused")
	private void confirmTurtleIsClosedLoop(Turtle input) throws Exception {
		throw new Exception("I cannot confirm this Turtle path is a closed loop.");
	}

	private LineCollection infillFromTurtle(Turtle input) {
		// make sure line segments don't start on another line, leading to an odd number
		// of intersections.
		Rectangle2D.Double bounds = addPaddingToBounds(input.getBounds(), 2.0);

		LineCollection results = new LineCollection();

		// do this once here instead of once per line.
		LineCollection convertedPath = input.getAsLineCollection();

		// working variable
		LineSegment2D line = new LineSegment2D(new Point2d(), new Point2d(), input.getColor());

		double size = Math.max(bounds.getHeight(), bounds.getWidth());
		Vector2d majorDir = new Vector2d(Math.cos(Math.toRadians(angle   )), Math.sin(Math.toRadians(angle   )));
		Vector2d minorDir = new Vector2d(Math.cos(Math.toRadians(angle+90)), Math.sin(Math.toRadians(angle+90)));
		Vector2d minorStart = new Vector2d(bounds.getCenterX(),bounds.getCenterY());
		minorStart.scaleAdd(-size,minorDir,minorStart);
		Vector2d minorTemp = new Vector2d();
		Vector2d majorStart = new Vector2d();
		Vector2d majorEnd = new Vector2d();

		var end = size*2;

		for(double i=0;i<end;i+=penDiameter) {
			minorTemp.scaleAdd(i,minorDir,minorStart);
			majorStart.scaleAdd(-size,majorDir,minorTemp);
			majorEnd.scaleAdd(size,majorDir,minorTemp);
			line.start.set(majorStart.x,majorStart.y);
			line.end.set(majorEnd.x,majorEnd.y);
			results.addAll(trimLineToPath(line, convertedPath));
		}

		return results;
	}

	/**
	 * Add padding to a {@link Rectangle2D.Double} bounding rectangle.
	 * 
	 * @param before the original rectangle
	 * @param percent the added percentage. 0...100
	 * @return the larger bounds
	 */
	private Rectangle2D.Double addPaddingToBounds(Rectangle2D.Double before, double percent) {
		percent*=0.01;
		Rectangle2D.Double after = new Rectangle2D.Double();
		after.x = before.x - before.width * percent/2.0;
		after.y = before.y - before.height * percent/2.0;
		after.height = before.height * (1.0 + percent);
		after.width = before.width * (1.0 + percent);
		return after;
	}

	/**
	 * Trim a {@link LineSegment2D} against a path and return a list
	 * of remaining line segments.
	 * <p>
	 * If the polygon is convex, there will be two intersection points. These two
	 * points are the end points of the trimmed version of the line.
	 * </p>
	 * <p>
	 * If the polygon is not convex, there will be an even number of intersection
	 * points ≥2. Sort these intersection points (by increasing x value, for
	 * example). Then, taken in pairs, they give you the end points of the segments
	 * of the line that lie inside the polygon.
	 * </p>
	 * <p>Collect intersections and compute their parametric 't' along the infill line.
	 * Sorting by projection (t) is more robust than sorting by x or y, and we
	 * remove nearly-duplicate intersections caused by shared segment endpoints
	 * or floating point noise.</p>
	 * 
	 * @param line  A {@link LineSegment2D} to clip
	 * @param convertedPath The boundary line, which must be a closed loop
	 * @return a list of remaining {@link LineSegment2D}.
	 */
	private LineCollection trimLineToPath(LineSegment2D line, LineCollection convertedPath) {
		record Hit(Point2d p,double t) {}
		List<Hit> hits = new ArrayList<>();
		Vector2d s1 = new Vector2d(line.end);
		s1.sub(line.start);
		double s1_len2 = s1.lengthSquared();

		for (LineSegment2D s : convertedPath) {
			Point2d p = getIntersection(line, s);
			if (p != null) {
				double t = 0.0;
				if (s1_len2 > 0) {
					t = ((p.x - line.start.x) * s1.x + (p.y - line.start.y) * s1.y) / s1_len2;
				}
				hits.add(new Hit(p, t));
			}
		}

		LineCollection results = new LineCollection();
		if (hits.isEmpty()) return results;

		// sort by t and deduplicate near-equal t values
		hits.sort((a,b) -> Double.compare(a.t,b.t));
		List<Hit> unique = new ArrayList<>();
		double EPS = 1e-8;
		for (Hit h : hits) {
			if (unique.isEmpty() || Math.abs(h.t - unique.getLast().t) > EPS) {
				unique.add(h);
			}
		}

		int size = unique.size();
		if (size % 2 == 0) {
			for (int i = 0; i < size - 1; i += 2) {
				results.add(new LineSegment2D(unique.get(i).p, unique.get(i+1).p, line.color));
			}
		} else {
			// odd number of intersections — something unexpected; log for debugging
			logger.error("infill: odd intersection count=" + size);
		}

		return results;
	}

	/**
	 * It is based on an algorithm in Andre LaMothe's "Tricks of the Windows Game Programming Gurus". See
	 * <a href="https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect">Stackoverflow</a>
	 * TODO move this to com.marginallyclever.convenience.LineHelper?
	 * @param alpha first line segment
	 * @param beta second line segment
	 * @return intersection {@link Point2d} or null
	 */
	private Point2d getIntersection(LineSegment2D alpha, LineSegment2D beta) {
		double s1_x = alpha.end.x - alpha.start.x;
		double s1_y = alpha.end.y - alpha.start.y;
		double s2_x = beta.end.x - beta.start.x;
		double s2_y = beta.end.y - beta.start.y;

		double denominator = (-s2_x * s1_y + s1_x * s2_y);
		// avoid division by (near) zero -> parallel or nearly-parallel segments
		if (Math.abs(denominator) < 1e-12) return null;

		double s = (-s1_y * (alpha.start.x - beta.start.x) + s1_x * (alpha.start.y - beta.start.y)) / denominator;
		double t = ( s2_x * (alpha.start.y - beta.start.y) - s2_y * (alpha.start.x - beta.start.x)) / denominator;

		if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
			// hit!
			return new Point2d(alpha.start.x + (t * s1_x), alpha.start.y + (t * s1_y));
		}
		return null;
	}

	public double getPenDiameter() {
		return penDiameter;
	}

	public void setPenDiameter(double penDiameter) {
		this.penDiameter = Math.max(penDiameter, MINIMUM_PEN_DIAMETER);
	}

	public double getAngle() {
		return angle;
	}

	/**
	 * Direction of lines to draw. 0 is horizontal. 90 is vertical.
	 */
	public void setAngle(double angle) {
		this.angle = angle;
	}
}
