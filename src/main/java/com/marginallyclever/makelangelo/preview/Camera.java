package com.marginallyclever.makelangelo.preview;


import com.marginallyclever.util.PreferencesHelper;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;

/**
 * All information about the position and zoom level of the virtual eye looking through the PreviewPanel at the robot/art
 * @author Dan Royer
 *
 */
public class Camera {
	public static final float CAMERA_ZOOM_MAX = 1000.0f;
	public static final float CAMERA_ZOOM_MIN = 0.25f;
	public static final double ZOOM_STEP_SIZE = 0.15;

	// scale + position
	private double offsetX = 0.0;
	private double offsetY = 0.0;
	private double zoom = 1.0;

	// window size (for aspect ratio?)
	private double width, height;

	private double fovY = 60;
	private double nearZ = 1;
	private double farZ = 1000;

	public Camera() {}

	/**
	 * Reposition the camera
	 * @param dx change horizontally
	 * @param dy change vertically
	 */
	public void moveRelative(double dx, double dy) {
		double scale = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS).getInt("dragSpeed", 1);
		offsetX += dx * scale / zoom;
		offsetY += dy * scale / zoom;
	}

	private void limitCameraZoom() {
		if(zoom< CAMERA_ZOOM_MIN) zoom= CAMERA_ZOOM_MIN;
		if(zoom> CAMERA_ZOOM_MAX) zoom= CAMERA_ZOOM_MAX;
	}

	// scale the picture of the robot to fake a zoom.
	public void zoom(int amount) {
		zoom(amount, new Point2d());
	}

	/**
	 * Returns the input converted to world-space coordinates.
	 * @param input a point relative to the center of the camera view.
	 * @return the input converted to world-space coordinates.
	 */
	public Point2d screenToWorldSpace(Point2d input) {
		Point2d output = new Point2d();
		output.x = input.x/zoom + offsetX;
		output.y = input.y/zoom + offsetY;
		return output;
	}

	/**
	 * Scale the picture of the robot at the indicated point
	 * @param amount amount to zoom
	 * @param cursor center of zoom, relative to camera.
	 */
	public void zoom(int amount, Point2d cursor) {
		Point2d before = screenToWorldSpace(cursor);
		zoom -= (double)amount * ZOOM_STEP_SIZE;
		limitCameraZoom();
		Point2d after = screenToWorldSpace(cursor);

		offsetX -= after.x - before.x;
		offsetY -= after.y - before.y;
	}

	// scale the picture of the robot to fake a zoom.
	public void zoomToFit(double w,double h) {
		offsetX = 0;
		offsetY = 0;
		zoom = Math.max(w/h, h/w);

		limitCameraZoom();
	}

	public double getX() {
		return offsetX;
	}

	public double getY() {
		return offsetY;
	}

	public double getZoom() {
		return zoom;
	}
	
	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	/**
	 * return the finite perspective matrix for the camera.
	 * @param width width of the viewport
	 * @param height height of the viewport
	 * @return the perspective projection matrix
	 */
	public Matrix4d getProjectionMatrix(int width,int height) {
		double aspect = (double)width / (double)height;
		var far = getFarZ();
		var	near = getNearZ();
		var m = new Matrix4d();
		double f = 1.0 / Math.tan(Math.toRadians(fovY) / 2.0);
		m.m00 = f / aspect;
		m.m11 = f;
		m.m22 = -(far + near) / (far - near);
		m.m32 = -1.0;
		m.m23 = -(2.0 * far * near) / (far - near);
		m.m33 = 0.0;
		return m;
	}

	public Matrix4d getOrthographicMatrix(int width, int height) {
		Matrix4d matrix = new Matrix4d();
		double h = height / 2.0;
		double w = width / 2.0;
		double left = -w;
		double right = w;
		double bottom = -h;
		double top = h;
		double near = nearZ; // Near plane
		double far = farZ; // Far plane
		matrix.m00 = 2.0 / (right - left);
		matrix.m11 = 2.0 / (top - bottom);
		matrix.m22 = -2.0 / (far - near);
		matrix.m33 = 1.0;
		matrix.m30 = -(right + left) / (right - left);
		matrix.m31 = -(top + bottom) / (top - bottom);
		matrix.m32 = -(far + near) / (far - near);
		return matrix;
	}

	public Matrix4d getViewMatrix() {
		Matrix4d matrix = new Matrix4d();
		matrix.setIdentity();
		matrix.m00 = zoom;
		matrix.m11 = zoom;
		matrix.m03 = -offsetX * zoom;
		matrix.m13 = offsetY * zoom;
		matrix.m23 = -1;
		return matrix;
	}

	public double getFovY() {
		return fovY;
	}

	public void setFovY(double fovY) {
		this.fovY = fovY;
	}

	public double getNearZ() {
		return nearZ;
	}

	public void setNearZ(double nearZ) {
		this.nearZ = nearZ;
	}

	public double getFarZ() {
		return farZ;
	}

	public void setFarZ(double farZ) {
		this.farZ = farZ;
	}
}
