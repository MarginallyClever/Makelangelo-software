package com.marginallyclever.makelangelo.preview;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * OpenGL hardware accelerated WYSIWYG view.
 * @author Dan Royer
 *
 */
public class PreviewPanel extends GLJPanel implements GLEventListener {

	private static final Logger logger = LoggerFactory.getLogger(PreviewPanel.class);
	
	static final long serialVersionUID = 2;

	// Use debug pipeline?
	private static final boolean DEBUG_GL_ON = false;
	private static final boolean TRACE_GL_ON = false;

	private List<PreviewListener> previewListeners = new ArrayList<>();
	
	private Camera camera;
	
	// background color, rgb values 0...255
	public ColorRGB backgroundColor = new ColorRGB(255-67,255-67,255-67);
	
	// motion control
	// private boolean mouseIn=false;
	private int buttonPressed = MouseEvent.NOBUTTON;
	private int mouseOldX, mouseOldY;

	// OpenGL stuff
	private GLU glu;
	private FPSAnimator animator;


	public PreviewPanel() {
		super();
		
		try {
			logger.debug("  get GL capabilities...");
			GLProfile glProfile = GLProfile.getDefault();
			GLCapabilities caps = new GLCapabilities(glProfile);
			// caps.setSampleBuffers(true);
			// caps.setHardwareAccelerated(true);
			// caps.setNumSamples(4);
			setRequestedGLCapabilities(caps);
		} catch(GLException e) {
			logger.error("I failed the very first call to OpenGL.  Are your native libraries missing?", e);
			System.exit(1);
		}

		addGLEventListener(this);
		
		// scroll the mouse wheel to zoom
		addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int notches = e.getWheelRotation();
				if (notches < 0) {
					camera.zoomIn();
				} else if (notches > 0) {
					camera.zoomOut();
				}
				repaint();
			}
		});
		
		// remember button states for when we need them.
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				buttonPressed = e.getButton();
				mouseOldX = e.getX();
				mouseOldY = e.getY();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				buttonPressed = MouseEvent.NOBUTTON;
			}
		});
		
		
		// left click + drag to move the camera around.
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (buttonPressed == MouseEvent.BUTTON1) {
					int x = e.getX();
					int y = e.getY();
					int dx = x - mouseOldX;
					int dy = y - mouseOldY;
					mouseOldX = x;
					mouseOldY = y;
					camera.moveRelative(-dx, -dy);
					repaint();
				}
			}
		});
		
		// start animation system
		logger.debug("  starting animator...");
		animator = new FPSAnimator(1);
		animator.add(this);
		animator.start();
	}

	public void addListener(PreviewListener arg0) {
		previewListeners.add(arg0);
	}
	
	public void removeListener(PreviewListener arg0) {
		previewListeners.remove(arg0);
	}
	
	/**
	 * Set up the correct projection so the image appears in the right location and aspect ratio.
	 */
	@Override
	public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		// gl2.setSwapInterval(1);
		
		camera.setWidth(width);
		camera.setHeight(height);
		
		// window_aspect_ratio = window_width / window_height;

		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		glu.gluPerspective( 90,
				(float) width / (float) height,
				Camera.CAMERA_ZNEAR,
				Camera.CAMERA_ZFAR);
	}

	/**
	 * Startup OpenGL.  Turn on debug pipeline(s) if needed.
	 */
	@Override
	public void init(GLAutoDrawable glautodrawable) {
		GL gl = glautodrawable.getGL();

		if (DEBUG_GL_ON) {
			try {
				// Debug ..
				gl = gl.getContext().setGL(GLPipelineFactory.create("com.jogamp.opengl.Debug", null, gl, null));
			} catch (Exception e) {
				logger.error("Failed to init OpenGL debug pipeline", e);
			}
		}

		if (TRACE_GL_ON) {
			try {
				// Trace ..
				gl = gl.getContext().setGL(
						GLPipelineFactory.create("com.jogamp.opengl.Trace", null, gl, new Object[] { System.err }));
			} catch (Exception e) {
				logger.error("Failed to init OpenGL trace", e);
			}
		}
		
		glu = GLU.createGLU(gl);
	}

	@Override
	public void dispose(GLAutoDrawable glautodrawable) {}

	/**
	 * Refresh the image in the view.  This is where drawing begins.
	 */
	@Override
	public void display(GLAutoDrawable glautodrawable) {
		// long now_time = System.currentTimeMillis();
		// float dt = (now_time - last_time)*0.001f;
		// last_time = now_time;
		// logger.debug(dt);

		// draw the world
		GL2 gl2 = glautodrawable.getGL().getGL2();

		// set some render quality options
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		if(prefs != null && prefs.getBoolean("antialias", true)) {
			gl2.glEnable(GL2.GL_LINE_SMOOTH);
			gl2.glEnable(GL2.GL_POLYGON_SMOOTH);
			gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
		} else {
			gl2.glDisable(GL2.GL_LINE_SMOOTH);
			gl2.glDisable(GL2.GL_POLYGON_SMOOTH);
			gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_FASTEST);
		}
		
		// turn on blending
		gl2.glEnable(GL2.GL_BLEND);
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);  
		
		paintBackground(gl2);
		paintCamera(gl2);
		
		gl2.glLineWidth((float)camera.getZoom());

		for( PreviewListener p : previewListeners ) {
			gl2.glPushMatrix();
			p.render(gl2);
			gl2.glPopMatrix();
		}
	}
	
	/**
	 * Set up the correct modelview so the robot appears where it should.
	 *
	 * @param gl2
	 */
	private void paintCamera(GL2 gl2) {
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		gl2.glTranslated(-camera.getX(), camera.getY(),-camera.getZoom());
	}

	/**
	 * Clear the panel
	 *
	 * @param gl2
	 */
	private void paintBackground(GL2 gl2) {
		int b=70;
		backgroundColor = new ColorRGB(255-b,255-b,255-b);
		// Clear The Screen And The Depth Buffer
		gl2.glClearColor(
				(float)backgroundColor.getRed()/255.0f,
				(float)backgroundColor.getGreen()/255.0f,
				(float)backgroundColor.getBlue()/255.0f,
				0.0f);

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

	public void stop() {
		animator.stop();
	}

	public void setCamera(Camera camera2) {
		camera = camera2;
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
