package com.marginallyclever.convenience;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of Point2D describing a line that is drawn with the pen down.
 * @author danroyer
 *
 */
public class Line2D {
	// is closed loop?
	// is filled?
	
	public List<Point2D> points;
	
	public Line2D() {
		points = new ArrayList<Point2D>();
	}
}
