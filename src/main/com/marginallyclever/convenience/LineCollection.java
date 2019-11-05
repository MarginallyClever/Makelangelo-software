package com.marginallyclever.convenience;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of line2Ds
 * @author danroyer
 *
 */
public class LineCollection {
	// color for this set of lines
	public ColorRGB color;
	
	public double tx_mm, ty_mm;  // translate
	public double sx_mm, sy_mm;  // scale
	public double rot_deg;       // rotate
	
	// the lines themselves
	public List<Line2D> lines;
	
	public LineCollection() {
		lines = new ArrayList<Line2D>();
		tx_mm=0;
		ty_mm=0;
		sx_mm=0;
		sy_mm=0;
		rot_deg=0;
	}
}
