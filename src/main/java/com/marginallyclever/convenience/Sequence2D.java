package com.marginallyclever.convenience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A sequence of 2D lines forming a polyline.
 * The end of lines.get(n).b should match lines.get(n+1).a.
 * if end of lines.get(last).b matches lines.get(0).a then this is a closed loop.
 * @author Dan Royer
 *
 */
@Deprecated(since="7.30.0")
public class Sequence2D {
	private static final Logger logger = LoggerFactory.getLogger(Sequence2D.class);
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
			logger.error("Failed to flip", e);
		}
		for( LineSegment2D line : lines ) {
			line.flip();
		}
	}
}