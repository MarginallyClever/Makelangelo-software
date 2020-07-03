package com.marginallyclever.makelangelo;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.prefs.Preferences;

import javax.swing.event.MouseInputListener;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLPipelineFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.util.PreferencesHelper;

// Custom drawing panel written as an inner class to access the instance variables.
public class PreviewPanel extends GLJPanel implements MouseListener, MouseInputListener, MouseWheelListener, GLEventListener {
	private static final float CAMERA_ZFAR = 1000.0f;
	private static final float CAMERA_ZNEAR = 10.0f;

	static final long serialVersionUID = 2;

	// Use debug pipeline?
	private static final boolean DEBUG_GL_ON = false;
	private static final boolean TRACE_GL_ON = false;

	// motion control
	// private boolean mouseIn=false;
	private int buttonPressed = MouseEvent.NOBUTTON;
	private int mouseOldX, mouseOldY;

	// scale + position
	private double cameraOffsetX = 0.0d;
	private double cameraOffsetY = 0.0d;
	private double cameraZoom = 1.0d;
	private int windowWidth = 0;
	private int windowHeight = 0;

	private GLU glu;
	
	protected MakelangeloRobot robot;

	private Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
	

	public PreviewPanel(GLCapabilities caps) {
		super(caps);
		addMouseMotionListener(this);
		addMouseListener(this);
		addGLEventListener(this);
		addMouseWheelListener(this);
	}

	void setRobot(MakelangeloRobot r) {
		robot = r;
	}

	/**
	 * set up the correct projection so the image appears in the right location
	 * and aspect ratio.
	 */
	@Override
	public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		// gl2.setSwapInterval(1);

		windowWidth = width;
		windowHeight = height;
		// window_aspect_ratio = window_width / window_height;

		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		// gl2.glOrtho(-windowWidth / 2.0d, windowWidth / 2.0d, -windowHeight /
		// 2.0d, windowHeight / 2.0d, 0.01d, 100.0d);

		glu.gluPerspective(
				90,
				(float) windowWidth / (float) windowHeight,
				CAMERA_ZNEAR,
				CAMERA_ZFAR);
	}

	/**
	 * turn on debug pipeline(s) if needed.
	 */
	@Override
	public void init(GLAutoDrawable glautodrawable) {
		GL gl = glautodrawable.getGL();

		if (DEBUG_GL_ON) {
			try {
				// Debug ..
				gl = gl.getContext().setGL(GLPipelineFactory.create("com.jogamp.opengl.Debug", null, gl, null));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (TRACE_GL_ON) {
			try {
				// Trace ..
				gl = gl.getContext().setGL(
						GLPipelineFactory.create("com.jogamp.opengl.Trace", null, gl, new Object[] { System.err }));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		glu = GLU.createGLU(gl);
	}

	@Override
	public void dispose(GLAutoDrawable glautodrawable) {
	}

	/**
	 * refresh the image in the view
	 */
	@Override
	public void display(GLAutoDrawable glautodrawable) {
		// long now_time = System.currentTimeMillis();
		// float dt = (now_time - last_time)*0.001f;
		// last_time = now_time;
		// Log.message(dt);

		// draw the world
		GL2 gl2 = glautodrawable.getGL().getGL2();
		render(gl2);
	}

	/**
	 * scale the picture of the robot to fake a zoom.
	 */
	public void zoomToFitPaper() {
		if (robot != null) {
			double widthOfPaper = robot.getSettings().getPaperWidth();
			double heightOfPaper = robot.getSettings().getPaperHeight();
			double drawPanelWidthZoom = widthOfPaper;
			double drawPanelHeightZoom = heightOfPaper;

			if (windowWidth < windowHeight) {
				cameraZoom = (drawPanelWidthZoom > drawPanelHeightZoom ? drawPanelWidthZoom : drawPanelHeightZoom);
			} else {
				cameraZoom = (drawPanelWidthZoom < drawPanelHeightZoom ? drawPanelWidthZoom : drawPanelHeightZoom);
			}
		}
		cameraOffsetX = 0;
		cameraOffsetY = 0;
		
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		buttonPressed = e.getButton();
		mouseOldX = e.getX();
		mouseOldY = e.getY();
	}

	public void mouseReleased(MouseEvent e) {
		buttonPressed = MouseEvent.NOBUTTON;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int dx = x - mouseOldX;
		int dy = y - mouseOldY;
		if (buttonPressed == MouseEvent.BUTTON1) moveCamera(-dx, -dy);
		//if (buttonPressed == MouseEvent.BUTTON3) zoomCamera(dy);
		mouseOldX = x;
		mouseOldY = y;
	}

	public void mouseMoved(MouseEvent e) {
	}

	/**
	 * Reposition the camera
	 * @param dx change horizontally
	 * @param dy change vertically
	 */
	private void moveCamera(int dx, int dy) {
		cameraOffsetX += (float) dx * cameraZoom / windowWidth;
		cameraOffsetY += (float) dy * cameraZoom / windowHeight;
		repaint();
	}

	/**
	 * Scale the picture of the robot to fake a zoom.
	 *
	 * @param dy zoom amount
	 */
	@SuppressWarnings("unused")
	private void zoomCamera(int dy) {
		double zoomAmount = (double) dy * 0.25;
		cameraZoom += zoomAmount;
		if (cameraZoom < 0.1)
			cameraZoom = 0.1;
		
		repaint();
	}

	/**
	 * scale the picture of the robot to fake a zoom.
	 */
	public void zoomIn() {
		cameraZoom *= 3.5d / 4.0d;
		if(cameraZoom<CAMERA_ZNEAR) cameraZoom=CAMERA_ZNEAR;
		
		repaint();
	}

	/**
	 * scale the picture of the robot to fake a zoom.
	 */
	public void zoomOut() {
		cameraZoom *= 4.0d / 3.5d;
		if(cameraZoom>CAMERA_ZFAR) cameraZoom=CAMERA_ZFAR;
		
		repaint();
	}

	public double getZoom() {
		return cameraZoom;
	}
	
	/**
	 * set up the correct modelview so the robot appears where it should.
	 *
	 * @param gl2
	 */
	private void paintCamera(GL2 gl2) {
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		gl2.glTranslated(-cameraOffsetX, cameraOffsetY,-cameraZoom);
	}

	/**
	 * clear the panel
	 *
	 * @param gl2
	 */
	private void paintBackground(GL2 gl2) {
		// Clear The Screen And The Depth Buffer
		gl2.glClearColor(212.0f / 255.0f, 233.0f / 255.0f, 255.0f / 255.0f, 0.0f);

		// Special handling for the case where the GLJPanel is translucent
		// and wants to be composited with other Java 2D content
		if (GLProfile.isAWTAvailable()
				&& !isOpaque()
				&& shouldPreserveColorBufferIfTranslucent()) {
			gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		} else {
			gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		}
	}

	public void render(GL2 gl2) {
		if(prefs.getBoolean("antialias", true)) {
			gl2.glEnable(GL2.GL_LINE_SMOOTH);
			gl2.glEnable(GL2.GL_POLYGON_SMOOTH);
			gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
		} else {
			gl2.glDisable(GL2.GL_LINE_SMOOTH);
			gl2.glDisable(GL2.GL_POLYGON_SMOOTH);
			gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_FASTEST);
		}
		gl2.glEnable(GL2.GL_BLEND);
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);  
		
		paintBackground(gl2);
		paintCamera(gl2);

		gl2.glPushMatrix();

		if (robot != null) {
			gl2.glLineWidth((float)cameraZoom);
			robot.render(gl2);
		}

		gl2.glPopMatrix();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		if (notches < 0) {
			zoomIn();
		} else {
			zoomOut();
		}
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
 * Makelangelo is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Makelangelo. If not, see <http://www.gnu.org/licenses/>.
 */
