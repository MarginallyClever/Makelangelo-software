package com.marginallyclever.makelangelo.makeart;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Take an existing drawing, scan across it horizontally.  Add new lines between every pair of lines found.
 * It may sometimes make mistakes if it hits the very end of a line.
 * 
 * @author Dan Royer
 * @since 7.31.0
 */
public class InfillTurtle {
	private static final Logger logger = LoggerFactory.getLogger(InfillTurtle.class);
	
	public static final double MINIMUM_PEN_DIAMETER = 0.1;
	private double penDiameter = 0.8; // TODO adjust me before running infill

	public InfillTurtle() {}

	public Turtle run(Turtle input) throws Exception {
		logger.debug("InfillTurtle.run()");
		// confirmTurtleIsClosedLoop(input);

		Turtle result = new Turtle();

		List<Turtle> list = input.splitByToolChange();
		for(Turtle t : list) {
			List<LineSegment2D> segments = infillFromTurtle(t);
			Turtle t2 = new Turtle();
			t2.addLineSegments(segments);
			result.add(t2);
		}

		return result;
	}

	@SuppressWarnings("unused")
	private void confirmTurtleIsClosedLoop(Turtle input) throws Exception {
		throw new Exception("I cannot confirm this Turtle path is a closed loop.");
	}

	private List<LineSegment2D> infillFromTurtle(Turtle input) {
		logger.debug("  infillFromTurtle()");
		// make sure line segments don't start on another line, leading to an odd number
		// of intersections.
		Rectangle2D.Double bounds = addPaddingToBounds(input.getBounds(), 2.0);

		List<LineSegment2D> results = new ArrayList<>();

		// do this once here instead of once per line.
		List<LineSegment2D> convertedPath = input.getAsLineSegments();
		// working variable
		LineSegment2D line = new LineSegment2D(new Point2D(), new Point2D(), input.getColor());

		for (double y = bounds.getMinY(); y < bounds.getMaxY(); y += penDiameter) {
			line.start.set(bounds.getMinX(), y);
			line.end.set(bounds.getMaxX(), y);
			results.addAll(trimLineToPath(line, convertedPath));
		}

		return results;
	}

	/**
	 * Add padding to a {@link Rectangle2D.Double} bounding rectangle.
	 * 
	 * @param before the original rectangle
	 * @param percent the added percentage.
	 * @return the larger bounds
	 */
	private Rectangle2D.Double addPaddingToBounds(Rectangle2D.Double before, double percent) {
		logger.debug("  addPaddingToBounds()");
		percent*=0.01;
		Rectangle2D.Double after = new Rectangle2D.Double();
		after.x = before.x - before.width * percent/2.0;
		after.y = before.y - before.height * percent/2.0;
		after.height = before.height * (1.0 + percent);
		after.width = before.width * (1.0 + percent);
		logger.debug("    before={}", before);
		logger.debug("    after={}", after);
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
	 * 
	 * @param line  A {@link LineSegment2D} to clip
	 * @param convertedPath The boundary line, which must be a closed loop
	 * @return a list of remaining {@link LineSegment2D}.
	 */
	private List<LineSegment2D> trimLineToPath(LineSegment2D line, List<LineSegment2D> convertedPath) {
		List<Point2D> intersections = new ArrayList<>();

		for (LineSegment2D s : convertedPath) {
			Point2D p = getIntersection(line, s);
			if (p != null) intersections.add(p);
		}

		List<LineSegment2D> results = new ArrayList<>();
		int size = intersections.size();
		if(size%2==0) {
			if (size == 2) {
				results.add(new LineSegment2D(intersections.get(0), intersections.get(1), line.color));
			} else if (size > 2) {
				results.addAll(sortIntersectionsIntoSegments(intersections, line.color));
			}
		}

		return results;
	}

	/**
	 * @param intersections A list of intersections. guaranteed to be 2 or more even
	 *                      number of intersections.
	 * @param color         Color to assign to line
	 * @return return Intersections sorted by ascending x value. If x values match,
	 *         sort by ascending y value.
	 */
	private List<LineSegment2D> sortIntersectionsIntoSegments(List<Point2D> intersections, ColorRGB color) {
		logger.debug("  sortIntersectionsIntoSegments() {}", intersections.size());
		Point2D first = intersections.get(0);
		Point2D second = intersections.get(1);
		if (Double.compare(first.x, second.x) == 0) {
			intersections.sort(new ComparePointsByY());
		} else {
			intersections.sort(new ComparePointsByX());
		}

		List<LineSegment2D> results = new ArrayList<>();
		int i = 0;
		while (i < intersections.size()-1) {
			results.add(new LineSegment2D(intersections.get(i), intersections.get(i + 1), color));
			i += 2;
		}

		return results;
	}

	class ComparePointsByY implements Comparator<Point2D> {
		@Override
		public int compare(Point2D o1, Point2D o2) {
			return Double.compare(o1.y, o2.y);
		}
	}

	class ComparePointsByX implements Comparator<Point2D> {
		@Override
		public int compare(Point2D o1, Point2D o2) {
			return Double.compare(o1.x, o2.x);
		}
	}

	/**
	 * It is based on an algorithm in Andre LaMothe's "Tricks of the Windows Game
	 * Programming Gurus". See
	 * https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
	 * TODO move this to com.marginallyclever.convenience.LineHelper?
	 * 
	 * @param alpha first line segment
	 * @param beta second line segment
	 * @return intersection {@link Point2D} or null
	 */
	private Point2D getIntersection(LineSegment2D alpha, LineSegment2D beta) {
		double s1_x = alpha.end.x - alpha.start.x;
		double s1_y = alpha.end.y - alpha.start.y;
		double s2_x = beta.end.x - beta.start.x;
		double s2_y = beta.end.y - beta.start.y;

		double denominator = (-s2_x * s1_y + s1_x * s2_y);
		double s = (-s1_y * (alpha.start.x - beta.start.x) + s1_x * (alpha.start.y - beta.start.y)) / denominator;
		double t = ( s2_x * (alpha.start.y - beta.start.y) - s2_y * (alpha.start.x - beta.start.x)) / denominator;

		if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
			// hit!
			return new Point2D(alpha.start.x + (t * s1_x), alpha.start.y + (t * s1_y));
		}
		return null;
	}

	public double getPenDiameter() {
		return penDiameter;
	}

	public void setPenDiameter(double penDiameter) {
		this.penDiameter = Math.max(penDiameter, MINIMUM_PEN_DIAMETER);
	}
}
