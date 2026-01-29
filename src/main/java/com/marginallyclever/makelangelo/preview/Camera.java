package com.marginallyclever.makelangelo.preview;


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
	private double offsetX = 0.0;
	private double offsetY = 0.0;
	private double zoom = 1.0;

	// window size (for aspect ratio?)
	private double width, height;

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

	/**
	 * Center and zoom such that w + h fit in the view.  zoom 1 is the furthest away, larger zooms are closer.
	 * @param w width to fit
	 * @param h height to fit
	 */
	public void zoomToFit(double w,double h) {
		offsetX = 0;
		offsetY = 0;
		// use width and height of camera viewport to determine zoom level.
		// compute the maximum zoom that still fits both dimensions
		double scaleX = width / w;
		double scaleY = height / h;
		zoom = Math.min(scaleX, scaleY);

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

	public void reshape(double width,double height) {
		this.width = width;
		this.height = height;
	}

	public double getHeight() {
		return height;
	}
}
