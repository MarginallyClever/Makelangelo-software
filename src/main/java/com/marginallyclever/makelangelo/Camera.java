package com.marginallyclever.makelangelo;

import com.marginallyclever.util.PreferencesHelper;

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
	private double px = 0.0;
	private double py = 0.0;
	private double zoom = 1.0;

	// window size (for aspect ratio?)
	private int width, height;

	public Camera() {}

	/**
	 * Reposition the camera
	 * @param dx change horizontally
	 * @param dy change vertically
	 */
	public void moveRelative(double dx, double dy) {
		double scale = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS).getInt("dragSpeed", 1);
		// TODO moving camera with these scale factors is pretty close but could be better.
		px += dx * scale * zoom/height;
		py += dy * scale * zoom/height;
	}

	private void limitCameraZoom() {
		if(zoom < CAMERA_ZOOM_MIN) zoom = CAMERA_ZOOM_MIN;
		if(zoom > CAMERA_ZOOM_MAX) zoom = CAMERA_ZOOM_MAX;
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
		// TODO this is not quite right.
		output.x = px + input.x * zoom/width;
		output.y = py + input.y * zoom/width;
		return output;
	}

	/**
	 * Scale the picture of the robot at the indicated point
	 * @param amount amount to zoom
	 * @param cursor center of zoom, relative to camera.
	 */
	public void zoom(int amount, Point2d cursor) {
		Point2d before = screenToWorldSpace(cursor);
		//zoom -= (double)amount * ZOOM_STEP_SIZE;
		double zoomScale = (double)amount * ZOOM_STEP_SIZE;
		zoom = zoom * (1.0 + zoomScale);

		limitCameraZoom();
		Point2d after = screenToWorldSpace(cursor);

		px -= after.x - before.x;
		py -= after.y - before.y;
	}

	// scale the picture of the robot to fake a zoom.
	public void zoomToFit(double w,double h) {
		px = 0;
		py = 0;
		zoom =  Math.max(w,h) * Math.cos(Math.toRadians(60))*2;

		limitCameraZoom();
	}

	public double getX() {
		return px;
	}

	public double getY() {
		return py;
	}

	public double getZoom() {
		return zoom;
	}
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
