package com.marginallyclever.basictypes;

public class Arc2D extends Line2D {
	protected Point2D center;
	protected boolean clockwise = true;

	// center of the plane in which the arc occurs
	public void setCenter(Point2D p) {
		center.set(p.x, p.y);// When does Arc#center get initialized? Won't this result in a NPE?
	}

	// center of the plane in which the arc occurs
	public Point2D getCenter() {
		return center;
	}

	// Clockwise if you are looking at the center with the normal pointing towards you.
	public void setClockwise(boolean c) {
		clockwise = c;
	}

	// Clockwise if you are looking at the center with the normal pointing towards you.
	public boolean getClockwise() {
		return clockwise;
	}

	/**
	 * get a point (end-start) * v + start where v=0...1
	 */
	public Point2D interpolate(float v) {
		Point2D n = new Point2D();

		// TODO finish Arc2D interpolation
		Point2D s1 = new Point2D(start.x-center.x,
				start.y-center.y );
		Point2D e1 = new Point2D(end.x-center.x,
				end.y-center.y );
		
		float sLen = s1.length();
		float eLen = e1.length();
		s1.normalize();
		e1.normalize();
		n.cross(s1,e1);
		Point2D ortho = new Point2D();
		ortho.cross(e1,n);
		float newLen = ( eLen - sLen ) * v + sLen;

		return n;
	}
}
