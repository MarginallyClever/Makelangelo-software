package com.marginallyclever.makelangelo.preview;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
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

	/**
	 * button state tracking
	 */
	private int buttonPressed = MouseEvent.NOBUTTON;

	/**
	 * previous mouse position
	 */
	private int mouseOldX, mouseOldY;

	/**
	 * mouseLastZoomDirection is used to prevent reverse zooming on track pads, bug #559.
	 */
	private int mouseLastZoomDirection = 0;

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

		final JPanel me = this;

		// scroll the mouse wheel to zoom
		addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int notches = e.getWheelRotation();
				if (notches == 0) return;

				Point2D p = new Point2D(e.getPoint().x,e.getPoint().y);
				Rectangle r = me.getBounds();
				p.x -= r.getCenterX();
				p.y -= r.getCenterY();

				if (notches < 0) {
					if (mouseLastZoomDirection == -1) camera.zoom(-1,p);
					mouseLastZoomDirection = -1;
				} else {
					if (mouseLastZoomDirection == 1) camera.zoom(1,p);
					mouseLastZoomDirection = 1;
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
			@Override
			public void mouseMoved(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				mouseOldX = x;
				mouseOldY = y;
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
		
		camera.setWidth(width);
		camera.setHeight(height);

		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		// orthographic projection
		glu.gluOrtho2D(-width/2, width/2, -height/2, height/2);
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

		for( PreviewListener p : previewListeners ) {
			gl2.glPushMatrix();
			p.render(gl2);
			gl2.glPopMatrix();
		}
	}

	// if you need to display a marker in the scene at the cursor position for debugging, use this.
	private void paintCursor(GL2 gl2) {
		gl2.glPushMatrix();

		Rectangle r = this.getBounds();
		Point2D sp = new Point2D(mouseOldX,mouseOldY);
		sp.x -= r.getCenterX();
		sp.y -= r.getCenterY();

		Point2D wp = camera.screenToWorldSpace(sp);
		gl2.glColor3d(255,0,255);
		gl2.glTranslated(wp.x,-wp.y,0);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex2d(-10,0);
		gl2.glVertex2d( 10,0);
		gl2.glVertex2d(0,-10);
		gl2.glVertex2d(0, 10);
		gl2.glEnd();

		gl2.glPopMatrix();
	}

	/**
	 * Set up the correct modelview so the robot appears where it should.
	 *
	 * @param gl2
	 */
	private void paintCamera(GL2 gl2) {
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		gl2.glScaled(camera.getZoom(),camera.getZoom(),1);
		gl2.glTranslated(-camera.getX(), camera.getY(),0);
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

	public void setCamera(Camera camera) {
		this.camera = camera;
	}
}