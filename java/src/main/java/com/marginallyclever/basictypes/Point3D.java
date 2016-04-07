package com.marginallyclever.basictypes;

public class Point3D {
	public float x, y, z;

	public Point3D() {
		set(0, 0, 0);
	}

	public Point3D(Point3D p) {
		set(p.x, p.y, p.z);
	}

	public Point3D(float xx, float yy, float zz) {
		set(xx, yy, zz);
	}

	public Point3D(double xx, double yy, double zz) {
		set((float) xx, (float) yy, (float) zz);
	}

	public void set(float xx, float yy, float zz) {
		x = xx;
		y = yy;
		z = zz;
	}

	public void normalize() {
		float d = lengthSquared();
		x/=d;
		y/=d;
		z/=d;
	}

	public float lengthSquared() {
		return x*x+y*y+z*z;
	}

	public float length() {
		return (float)Math.sqrt(lengthSquared());
	}
}

/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Makelangelo. If not, see <http://www.gnu.org/licenses/>.
 */
