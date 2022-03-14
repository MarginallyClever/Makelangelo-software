package com.marginallyClever.makelangelo.preview;

import com.marginallyClever.util.PreferencesHelper;

/**
 * All information about the position and zoom level of the virtual eye looking through the PreviewPanel at the robot/art
 * @author Dan Royer
 *
 */
public class Camera {
	public static final float CAMERA_ZFAR = 1000.0f;
	public static final float CAMERA_ZNEAR = 10.0f;
	public static final double ZOOM_STEP_SIZE = 3.5d / 4.0d;

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
	public void moveRelative(int dx, int dy) {
		int scale = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS).getInt("dragSpeed", 1);

		offsetX += (float) dx * zoom * scale / width;
		offsetY += (float) dy * zoom * scale / height;
	}

	private void limitCameraZoom() {
		if(zoom<CAMERA_ZNEAR) zoom=CAMERA_ZNEAR;
		if(zoom>CAMERA_ZFAR) zoom=CAMERA_ZFAR;
	}

	// scale the picture of the robot to fake a zoom.
	public void zoomIn() {
		zoom *= ZOOM_STEP_SIZE;
		limitCameraZoom();
	}

	// scale the picture of the robot to fake a zoom.
	public void zoomOut() {
		zoom /= ZOOM_STEP_SIZE;
		limitCameraZoom();
	}

	// scale the picture of the robot to fake a zoom.
	public void zoomToFit(double w,double h) {
		zoom = (Math.max(w, h))/2.0;
		limitCameraZoom();
		
		offsetX = 0;
		offsetY = 0;
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
}
