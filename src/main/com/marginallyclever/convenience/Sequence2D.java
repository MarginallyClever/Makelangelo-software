package com.marginallyclever.convenience;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A sequence of 2D lines forming a polyline.
 * The end of lines.get(n).b should match lines.get(n+1).a.
 * if end of lines.get(last).b matches lines.get(0).a then this is a closed loop.
 * @author Dan Royer
 *
 */
public class Sequence2D {
	public ArrayList<LineSegment2D> lines;
	public boolean isClosed;
	
	public Sequence2D() {
		lines = new ArrayList<LineSegment2D>();
		isClosed=false;
	}
	
	public void flip() {
		try {
			Collections.reverse(lines);
		} catch(Exception e) {
			e.printStackTrace();
		}
		for( LineSegment2D line : lines ) {
			line.flip();
		}
	}
}