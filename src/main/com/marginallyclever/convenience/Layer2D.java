package com.marginallyclever.convenience;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of line2Ds
 * @author danroyer
 *
 */
public class Layer2D {
	// color for this set of lines
	public ColorRGB color;
	
	// the lines themselves
	public List<Line2D> lines;
	
	public Layer2D() {
		lines = new ArrayList<Line2D>();
	}
}
