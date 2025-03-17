package com.marginallyclever.convenience;

import com.marginallyclever.convenience.linecollection.LineCollection;

/**
 * A sequence of 2D lines forming a polyline.
 * The end of lines.get(n).b should match lines.get(n+1).a.
 * if end of lines.get(last).b matches lines.get(0).a then this is a closed loop.
 * @author Dan Royer
 *
 */
@Deprecated(since="7.30.0")
public class Sequence2D {
	public LineCollection lines = new LineCollection();
	public boolean isClosed=false;
	
	public Sequence2D() {
		super();
	}
	
	public void flip() {
		lines.flip();
	}
}