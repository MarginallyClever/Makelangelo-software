package com.marginallyclever.basictypes;

public class Line3D {
	protected Point3D start = new Point3D();
	protected Point3D end = new Point3D();
	
	public Line3D() {}
	
	public void setStart(Point3D p) {
		start.set(p.x, p.y, p.z);
	}
	public Point3D getStart() {
		return start;
	}

	public void setEnd(Point3D p) {
		end.set(p.x, p.y, p.z);
	}
	
	public Point3D getEnd() {
		return end;
	}
	
	public float length() {
		float x = end.x - start.x;
		float y = end.y - start.y;
		float z = end.z - start.z;
		
		return (float)Math.sqrt(x*x+y*y+z*z);
	}
	
	/**
	 * get a point (end-start) * v + start where v=0...1
	 */
	public Point3D interpolate(float v) {
		Point3D n = new Point3D();
		n.set( ( end.x - start.x ) * v + start.x,
				( end.y - start.y ) * v + start.y,
				( end.z - start.z ) * v + start.z );
		return n;
	}
}
