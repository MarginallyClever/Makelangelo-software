package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.linecollection.LineCollection;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Take an existing drawing, scan across it horizontally.  Add new lines between every pair of lines found.
 * This ignores closed loops, and just looks at line segments.  It will work with open or closed loops but it will be
 * confused.
 * It may sometimes make mistakes if it hits the very end of a line.
 */
public class InfillTurtle {
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
			result.setStroke(t.getColor(), t.getDiameter());
			result.addLineSegments(segments);
		}

		return result;
	}

	@SuppressWarnings("unused")
	private void confirmTurtleIsClosedLoop(Turtle input) throws Exception {
		throw new Exception("I cannot confirm this Turtle path is a closed loop.");
	}

    /**
     * Given a {@link Turtle} containing lines, alternate drawing lines across the shape
     * Using the bounding box of the shape as a guide, draw lines across the shape.
     * As the line crosses the shape, add line segments between each pair of intersections, making sure to count
     * intersections that overlap.
     *
     * @param input A {@link Turtle} consisting of lines that are not guaranteed to be closed loops.
     * @return A {@link LineCollection} of infill lines.
     */
	private LineCollection infillFromTurtle(Turtle input) {
		// make sure line segments don't start on another line, leading to an odd number of intersections.
		Rectangle2D.Double bounds = addPaddingToBounds(input.getBounds(), 2.0);
		LineCollection results = new LineCollection();

		// do this once here instead of once per line.
		LineCollection convertedPath = input.getAsLineCollection();
		// working variable
		LineSegment2D line = new LineSegment2D(new Point2d(), new Point2d(), input.getColor());

		double size = Math.max(bounds.getHeight(), bounds.getWidth());
		Vector2d majorDir = new Vector2d(Math.cos(Math.toRadians(angle   )), Math.sin(Math.toRadians(angle   )));
        Vector2d minorDir = new Vector2d(majorDir.y, -majorDir.x); // perpendicular

        Vector2d majorStart = new Vector2d();
        Vector2d majorEnd = new Vector2d();

		Vector2d minorStart = new Vector2d(bounds.getCenterX(),bounds.getCenterY());
		minorStart.scaleAdd(-size/2,minorDir,minorStart);

		for(double i=0; i<size; i+=penDiameter) {
            // move the start point along the minor axis.
			majorStart.scaleAdd(-size/2,majorDir,minorStart);
            // move the end point along the minor axis.
			majorEnd.scaleAdd(size/2,majorDir,minorStart);

			line.start.set(majorStart);
			line.end.set(majorEnd);

            //results.add(new LineSegment2D(line.start, line.end, line.color));
            results.addAll(trimLineToPath(line, convertedPath));

            // increment the minor axis position.
			minorStart.scaleAdd(penDiameter,minorDir,minorStart);
		}

		return results;
	}

	/**
	 * Add padding to a {@link Rectangle2D.Double} bounding rectangle.
	 * 
	 * @param before the original rectangle
	 * @param amount the mm to add.
	 * @return the larger bounds
	 */
	private Rectangle2D.Double addPaddingToBounds(Rectangle2D.Double before, double amount) {
        return new Rectangle2D.Double(
                before.x - amount,
                before.y - amount,
                before.width + amount * 2,
                before.height + amount * 2
        );
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
	private LineCollection trimLineToPath(LineSegment2D line, LineCollection convertedPath) {
		List<Point2d> intersections = new ArrayList<>();
		for (LineSegment2D s : convertedPath) {
			Point2d p = getIntersection(line, s);
			if (p != null) intersections.add(p);
		}

        Vector2d direction = new Vector2d();
        direction.sub(line.end, line.start);
        direction.normalize();

        LineCollection results = new LineCollection();
        results.addAll(sortIntersectionsIntoSegments(intersections, line.color, direction, line.start));

		return results;
	}

	/**
	 * @param intersections A list of intersections. guaranteed to be 2 or more even
	 *                      number of intersections.
	 * @param color         Color to assign to line
	 * @return return Intersections sorted by ascending x value. If x values match,
	 *         sort by ascending y value.
	 */
	private LineCollection sortIntersectionsIntoSegments(List<Point2d> intersections, Color color, Vector2d direction, Point2d origin) {
        LineCollection results = new LineCollection();

        List<Vector2d> list = new ArrayList<>();
        for( var p : intersections ) {
            var v = new Vector2d();
            v.sub(p,origin);
            list.add(v);
        }
        // sort by distance
        list.sort((v1, v2) -> {
            return Double.compare(v1.dot(direction), v2.dot(direction));
        });


        // get the first point
        Point2d first = null;
        boolean in = false;

        while(!list.isEmpty()) {
            Vector2d p = list.removeFirst();
            Point2d second = new Point2d(p);
            second.add(origin);
            if(!in) {
                first = second;
            } else {
                // found a pair
                results.add(new LineSegment2D(first,second,color));
            }
            in = !in;
        }

		return results;
	}

	/**
	 * <p>It is based on an algorithm in Andre LaMothe's "Tricks of the Windows Game Programming Gurus". See
	 * <a href="https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect">Stackoverflow</a></p>
	 * <p>TODO move this to com.marginallyclever.convenience.LineHelper?</p>
     * <p>Note that this does not intersect collinear lines.</p>
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
        if( denominator == 0 ) return null; // parallel or collinear lines

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
