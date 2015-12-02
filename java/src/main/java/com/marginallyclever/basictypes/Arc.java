package com.marginallyclever.basictypes;

public class Arc extends Line3D {
	protected Point3D center;
	protected boolean clockwise = true;

	// center of the plane in which the arc occurs
	public void setCenter(Point3D p) {
		center.set(p.x, p.y, p.z);// When does Arc#center get initialized? Won't this result in a NPE?
	}

	// center of the plane in which the arc occurs
	public Point3D getCenter() {
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
	public Point3D interpolate(float v) {
		Point3D n = new Point3D();

		// TODO finish Arc3D interpolation
		Point3D s1 = new Point3D();
		Point3D e1 = new Point3D();
		s1.set( start.x-center.x,
				start.y-center.y,
				start.z-center.z );
		e1.set( end.x-center.x,
				end.y-center.y,
				end.z-center.z );
		/*
		float sLen = s1.length();
		float eLen = e1.length();
		s1.normalize();
		e1.normalize();
		n.cross(s1,e1);
		Point3D ortho = new Point3D();
		ortho.cross(e1,n);
		float newLen = ( eLen - sLen ) * v + sLen;
		
		*/

		return n;
	}
}
