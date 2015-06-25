package com.marginallyclever.basictypes;

public class Arc extends Line3D {
	protected Point3D center;
	protected boolean clockwise = true;

	public void setCenter(Point3D p) {
		center.set(p.x, p.y, p.z);// When does Arc#center get initialized? Won't this result in a NPE?
	}
	public Point3D getCenter() {
		return center;
	}
	
	public void setClockwise(boolean c) {
		clockwise=c;
	}
	
	public boolean getClockwise() {
		return clockwise;
	}
	
	/**
	 * get a point (end-start) * v + start where v=0...1
	 */
	public Point3D interpolate(float v) {
		Point3D n = new Point3D();

		// TODO finish Arc3D interpolation
		
		return n;
	}
}
