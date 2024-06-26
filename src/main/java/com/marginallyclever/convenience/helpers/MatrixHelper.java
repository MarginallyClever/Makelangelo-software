package com.marginallyclever.convenience.helpers;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/**
 * Convenience methods for matrixes
 *
 */
public class MatrixHelper {

	public enum EulerSequence {
		YXZ,
		YZX,
		XZY,
		XYZ,
		ZYX,
		ZYZ,
		ZXY,
	}

	public static Matrix4d createScaleMatrix4(double scale) {
		var m = new Matrix4d();
		m.m00 = m.m11 = m.m22 = scale;
		m.m33 = 1;
		return m;
	}

	/**
	 * Convenience method to call matrixToEuler() with only the rotational component.
	 * Assumes the rotational component is a valid rotation matrix.
	 * Eulers are using the YXZ convention.
	 * @param mat the Matrix4d to convert.
	 * @return a valid Euler solution to the matrix.
	 */
	public static Vector3d matrixToEuler(Matrix4d mat, EulerSequence sequenceIndex) {
		Matrix3d m3 = new Matrix3d();
		mat.get(m3);
		return matrixToEuler(m3,sequenceIndex);
	}

	/**
	 * Assumes the rotational component is a valid rotation matrix.
	 * Eulers are using the YXZ convention.
	 * @param mat the Matrix3d to convert.
	 * @param sequenceIndex a {@link EulerSequence} value.
	 * @return a valid Euler solution to the matrix.
	 */
	public static Vector3d matrixToEuler(Matrix3d mat, EulerSequence sequenceIndex) {
		double sy = Math.sqrt(mat.m00 * mat.m00 + mat.m10 * mat.m10);

		boolean singular = sy < 1e-6; // If sy is close to zero, the matrix is singular
		double x, y, z;
		if (!singular) {
			switch (sequenceIndex) {
				case YXZ:
					x = Math.atan2(mat.m21, mat.m22);
					y = Math.atan2(-mat.m20, sy);
					z = Math.atan2(mat.m10, mat.m00);
					break;
				case YZX:
					x = Math.atan2(-mat.m12, mat.m11);
					y = Math.atan2(mat.m10, sy);
					z = Math.atan2(-mat.m20, mat.m00);
					break;
				case XZY:
					x = Math.atan2(-mat.m21, mat.m20);
					y = Math.atan2(mat.m22, sy);
					z = Math.atan2(-mat.m01, mat.m00);
					break;
				case XYZ:
					x = Math.atan2(mat.m12, mat.m11);
					y = Math.atan2(-mat.m10, sy);
					z = Math.atan2(mat.m20, mat.m00);
					break;
				case ZYX:
					x = Math.atan2(-mat.m01, mat.m00);
					y = Math.atan2(mat.m02, sy);
					z = Math.atan2(-mat.m12, mat.m10);
					break;
				case ZYZ:
					x = Math.atan2(mat.m12, -mat.m10);
					y = Math.atan2(mat.m02, sy);
					z = Math.atan2(mat.m21, mat.m20);
					break;
				case ZXY:
					x = Math.atan2(mat.m01, mat.m00);
					y = Math.atan2(-mat.m02, sy);
					z = Math.atan2(mat.m12, mat.m10);
					break;
				default:
					throw new IllegalArgumentException("Invalid Euler sequence");
			}
		} else {
			// Singular case
			x = Math.atan2(-mat.m12, mat.m11);
			y = Math.atan2(-mat.m20, sy);
			z = 0;
		}
		return new Vector3d(x, y, z);
	}

	/**
	 * Converts Euler angles to a rotation matrix based on the specified Euler sequence.
	 * See also <a href="https://www.learnopencv.com/rotation-matrix-to-euler-angles/">...</a>
	 * @param radians radian rotation values
	 * @param sequenceIndex a {@link EulerSequence} value
	 * @return resulting matrix
	 */
	public static Matrix3d eulerToMatrix(Vector3d radians, EulerSequence sequenceIndex) {
		Matrix3d rX = new Matrix3d();		rX.rotX(radians.x);
		Matrix3d rY = new Matrix3d();		rY.rotY(radians.y);
		Matrix3d rZ = new Matrix3d();		rZ.rotZ(radians.z);
		Matrix3d result = new Matrix3d();
		switch (sequenceIndex) {
			case YXZ:  result.mul(rY, rX);  result.mul(rZ, result);  break;
			case YZX:  result.mul(rY, rZ);  result.mul(rX, result);  break;
			case XZY:  result.mul(rX, rZ);  result.mul(rY, result);  break;
			case XYZ:  result.mul(rX, rY);  result.mul(rZ, result);  break;
			case ZYX:  result.mul(rZ, rY);  result.mul(rX, result);  break;
			case ZYZ:  result.mul(rZ, rY);  result.mul(rZ, result);  break;
			case ZXY:  result.mul(rZ, rX);  result.mul(rY, result);  break;
			default:  throw new IllegalArgumentException("Invalid Euler sequence");
		}
		return result;
	}

	/**
	 * Interpolate between two 4d matrixes, (end-start)*i + start where i=[0...1]
	 * @param start start matrix
	 * @param end end matrix
	 * @param alpha double value in the range [0...1]
	 * @param result where to store the resulting matrix
	 * @return True if the operation succeeds.  False if the inputs are bad or the operation fails. 
	 */
	public static boolean interpolate(final Matrix4d start,final Matrix4d end,double alpha,Matrix4d result) {
		if(alpha<0 || alpha>1) return false;
		
		// spherical interpolation (slerp) between the two matrix orientations
		Quat4d qStart = new Quat4d();
		qStart.set(start);
		Quat4d qEnd = new Quat4d();
		qEnd.set(end);
		Quat4d qInter = new Quat4d();
		qInter.interpolate(qStart, qEnd, alpha);
		
		// linear interpolation between the two matrix translations
		Vector3d tStart = new Vector3d();
		start.get(tStart);
		Vector3d tEnd = new Vector3d();
		end.get(tEnd);
		Vector3d tInter = new Vector3d();
		tInter.interpolate(tStart, tEnd, alpha);
		
		// build the result matrix
		result.set(qInter);
		result.setTranslation(tInter);
		
		// report ok
		return true;
	}

	/**
	 * Build a "look at" matrix.  The X+ axis is pointing (to-from) normalized.
	 * The Z+ starts as pointing up.  Y+ is cross product of X and Z.  Z is then
	 * recalculated based on the correct X and Y.
	 * This will fail if to-from is parallel to up.
	 *  
	 * @param from where i'm at
	 * @param to what I'm looking at
	 * @return a matrix that will transform a point to the "look at" orientation
	 */
	public static Matrix3d lookAt(final Vector3d from, final Vector3d to) {
		Vector3d forward = new Vector3d();
		Vector3d left = new Vector3d();
		Vector3d up = new Vector3d();
		
		forward.sub(to,from);
		forward.normalize();
		if(forward.z==1) {
			up.set(0,1,0);
		} else if(forward.z==-1) {
			up.set(0,-1,0);
		} else {
			up.set(0,0,1);
		}
		left.cross(up, forward);
		left.normalize();
		up.cross(forward, left);
		up.normalize();

		return new Matrix3d(
				left.x,up.x,forward.x,
				left.y,up.y,forward.y,
				left.z,up.z,forward.z);
	}

	/**
	 * Build a "look at" matrix.  The X+ axis is pointing (to-from) normalized.
	 * The Z+ starts as pointing up.  Y+ is cross product of X and Z.  Z is then
	 * recalculated based on the correct X and Y.
	 * This will fail if to-from is parallel to up.
	 *  
	 * @param from where i'm at
	 * @param to what I'm looking at
	 * @return a matrix that will transform a point to the "look at" orientation
	 */
	public static Matrix4d lookAt(final Vector3d from, final Vector3d to,final Vector3d up) {
		Vector3d forward = new Vector3d();
		Vector3d left = new Vector3d();
		
		forward.sub(to,from);
		forward.normalize();
		left.cross(up, forward);
		left.normalize();
		up.cross(forward, left);
		up.normalize();

		return new Matrix4d(
				left.x,up.x,forward.x,from.x,
				left.y,up.y,forward.y,from.y,
				left.z,up.z,forward.z,from.z,
				0,0,0,1);
	}

	public static Vector3d getXAxis(Matrix4d m) {
		return new Vector3d(m.m00, m.m10, m.m20);
	}
	public static Vector3d getYAxis(Matrix4d m) {
		return new Vector3d(m.m01, m.m11, m.m21);
	}
	public static Vector3d getZAxis(Matrix4d m) {
		return new Vector3d(m.m02, m.m12, m.m22);
	}
	public static Vector3d getPosition(Matrix4d m) {
		return new Vector3d(m.m03, m.m13, m.m23);
	}

	public static void setXAxis(Matrix4d m,Vector3d v) {
		m.m00=v.x;  m.m10=v.y;  m.m20=v.z;
	}
	public static void setYAxis(Matrix4d m,Vector3d v) {
		m.m01=v.x;  m.m11=v.y;  m.m21=v.z;
	}
	public static void setZAxis(Matrix4d m,Vector3d v) {
		m.m02=v.x;  m.m12=v.y;  m.m22=v.z;
	}
	public static void setPosition(Matrix4d m,Vector3d v) {
		m.m03=v.x;  m.m13=v.y;  m.m23=v.z;
	}

	/**
	 * normalize the 3x3 component of the mTarget matrix.  Do not affect position. 
	 * @param mTarget the matrix that will be normalized.
	 */
	public static void normalize3(Matrix4d mTarget) {
		Matrix3d m3 = new Matrix3d();
		Vector3d v3 = new Vector3d();
		mTarget.get(v3);
		mTarget.get(m3);
		m3.normalize();
		mTarget.set(m3);
		mTarget.setTranslation(v3);
	}

	public static Matrix4d createIdentityMatrix4() {
		var m = new Matrix4d();
		m.setIdentity();
		return m;
	}

	/**
     * see <a href="https://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_angle">Wikipedia</a>
     * @param axis a normalized vector
     * @param degrees angle in degrees
     * @return a rotation matrix
     */
	public static Matrix3d getMatrixFromAxisAndRotation(Vector3d axis,double degrees) {
		Matrix3d m = new Matrix3d();
		
		double radians = Math.toRadians(degrees);
		double c = Math.cos(radians);
		double s = Math.sin(radians);
		double oneMinusC = 1-c;
		double x = axis.x;
		double y = axis.y;
		double z = axis.z;
		
		double xzc = x*z*oneMinusC;
		double yzc = y*z*oneMinusC;
		double xyc = x*y*oneMinusC;
		
		m.m00 = c + x*x*oneMinusC;
		m.m01 = xyc - z*s;
		m.m02 = xzc + y*s;

		m.m10 = xyc +z*s;
		m.m11 = c + y*y*oneMinusC;
		m.m12 = yzc - x*s;
		
		m.m20 = xzc - y*s;
		m.m21 = yzc + x*s;
		m.m22 = c + z*z*oneMinusC;
			
		return m;
	}

	/**
     * returns Q and D such that Diagonal matrix D = QT * A * Q;  and  A = Q*D*QT
     * see <a href="https://en.wikipedia.org/wiki/Jacobi_eigenvalue_algorithm">Jacobi_eigenvalue_algorithm</a>
     * see <a href="https://en.wikipedia.org/wiki/Diagonalizable_matrix#Diagonalization">Diagonalization</a>
     * @param a a symmetric matrix.
     * @param dOut where to store the results
     * @param qOut where to store the results
     */
	public static void diagonalize(Matrix3d a,Matrix3d dOut,Matrix3d qOut) {
		Matrix3d d = new Matrix3d();
		d.setIdentity();
		Matrix3d q = new Matrix3d(a);

		double offMatrixNorm2 = q.m01*q.m01 + q.m02*q.m02 + q.m12*q.m12;

		final int ite_max = 1024;
		int ite = 0;
		while (offMatrixNorm2 > 1e-6 && ite++ < ite_max) {
			double e01 = q.m01 * q.m01;
			double e02 = q.m02 * q.m02;
			double e12 = q.m12 * q.m12;
			// Find the pivot element
			int i, j;
			if (e01 > e02) {
				if (e12 > e01) {
					i = 1;
					j = 2;
				} else {
					i = 0;
					j = 1;
				}
			} else {
				if (e12 > e02) {
					i = 1;
					j = 2;
				} else {
					i = 0;
					j = 2;
				}
			}

			// Compute the rotation angle
			double angle;
			if(Math.abs(q.getElement(j,j) - q.getElement(i,i)) < 1e-6) {
				angle = Math.PI / 4.0;
			} else {
				angle = 0.5 * Math.atan(2 * q.getElement(i,j) / (q.getElement(j,j) - q.getElement(i,i)));
			}

			// Compute the rotation matrix
			Matrix3d rot = new Matrix3d();
			rot.setIdentity();
			double c = Math.cos(angle);
			double s = Math.sin(angle);
			rot.setElement(i,i, c);		rot.setElement(i,j,-s); 
			rot.setElement(j,i, s);		rot.setElement(j,j, c);

			// Apply the rotation
			//*this = rot *this * rot.transposed();
			Matrix3d rt = new Matrix3d();
			rt.transpose(rot);
			Matrix3d temp = new Matrix3d();
			temp.mul(rot,q);
			q.mul(temp,rt);

			// Update the off matrix norm
			offMatrixNorm2 -= q.getElement(i,j) * q.getElement(i,j);

			d.mul(rot);
		}

		dOut.set(d);
		qOut.set(q);
	}

	public static Matrix4d orthographicMatrix4d(double left, double right, double bottom, double top, double near, double far) {
		double [] list = new double[16];
		org.joml.Matrix4d ortho = new org.joml.Matrix4d();
		ortho.setOrtho(left, right, bottom, top, near, far).get(list);
		return new Matrix4d(list);
	}

	public static Matrix4d perspectiveMatrix4d(double fovY, double aspect, double near, double far) {
		double [] list = new double[16];
		org.joml.Matrix4d perspective = new org.joml.Matrix4d();
		perspective.setPerspective(Math.toRadians(fovY), aspect, near, far).get(list);
		return new Matrix4d(list);
	}
}