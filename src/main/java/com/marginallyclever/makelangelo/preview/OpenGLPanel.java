package com.marginallyclever.makelangelo.preview;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * OpenGL hardware accelerated WYSIWYG view.
 * @author Dan Royer
 *
 */
public class OpenGLPanel extends JPanel implements GLEventListener, MouseWheelListener, MouseListener, MouseMotionListener {
	private static final Logger logger = LoggerFactory.getLogger(OpenGLPanel.class);
	
	// Use debug pipeline?
	private static final boolean DEBUG_GL_ON = false;
	private static final boolean TRACE_GL_ON = false;
	private GLJPanel glCanvas;
	private int canvasWidth,canvasHeight;

	private final List<PreviewListener> previewListeners = new ArrayList<>();
	
	private Camera camera;

	public Color backgroundColor = new Color(255-67,255-67,255-67);

	/**
	 * button state tracking
	 */
	private int buttonPressed = MouseEvent.NOBUTTON;

	/**
	 * previous mouse position
	 */
	private int mouseOldX, mouseOldY;
	private int mouseX,mouseY;

	/**
	 * mouseLastZoomDirection is used to prevent reverse zooming on track pads, bug #559.
	 */
	private int mouseLastZoomDirection = 0;

	// OpenGL stuff
	private GLU glu;
	private FPSAnimator animator;

	public OpenGLPanel() {
		super(new BorderLayout());
		
		try {
			logger.info("availability="+ GLProfile.glAvailabilityToString());
			GLCapabilities capabilities = getCapabilities();
			logger.info("create canvas");
			glCanvas = new GLJPanel(capabilities);
		} catch(GLException e) {
			logger.error("I failed the very first call to OpenGL.  Are your native libraries missing?", e);
			System.exit(1);
		}

		add(glCanvas, BorderLayout.CENTER);

		// start animation system
		logger.debug("  starting animator...");
		animator = new FPSAnimator(1);
		animator.add(glCanvas);
		animator.start();
	}

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

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		mouseX = x;
		mouseY = y;
		setTipXY();

		if (buttonPressed == MouseEvent.BUTTON1) {
			int dx = x - mouseOldX;
			int dy = y - mouseOldY;
			mouseOldX = x;
			mouseOldY = y;
			camera.moveRelative(-dx, -dy);
			repaint();
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		if (notches == 0) return;

		Point2d p = new Point2d(e.getPoint().x,e.getPoint().y);
		Rectangle r = this.getBounds();
		p.x -= r.getCenterX();
		p.y -= r.getCenterY();

		if (notches > 0) {
			if (mouseLastZoomDirection == -1) camera.zoom(-1,p);
			mouseLastZoomDirection = -1;
		} else {
			if (mouseLastZoomDirection == 1) camera.zoom(1,p);
			mouseLastZoomDirection = 1;
		}
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		mouseOldX = x;
		mouseOldY = y;
		mouseX = x;
		mouseY = y;
		setTipXY();
	}

	private GLCapabilities getCapabilities() {
		GLProfile profile = GLProfile.getMaxProgrammable(true);
		GLCapabilities capabilities = new GLCapabilities(profile);
		capabilities.setHardwareAccelerated(true);
		capabilities.setBackgroundOpaque(true);
		capabilities.setDoubleBuffered(true);
		capabilities.setStencilBits(8);
		capabilities.setDepthBits(32);  // 32 bit depth buffer is floating point
		StringBuilder sb = new StringBuilder();
		capabilities.toString(sb);
		logger.info("capabilities="+sb);
		return capabilities;
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
		System.out.println("reshape "+width+"x"+height);
		canvasWidth = width;
		canvasHeight = height;
	}

	public Vector2d getMousePositionInWorld() {
		double w2 = camera.getWidth()/2.0;
		double h2 = camera.getHeight()/2.0;
		double z = camera.getZoom();
		Vector2d cursorInSpace = new Vector2d(mouseX-w2, mouseY-h2);
		cursorInSpace.scale(1.0/z);
		return new Vector2d(camera.getX()+cursorInSpace.x,
							-(camera.getY()+cursorInSpace.y));
	}

	private void setTipXY() {
		Vector2d p = getMousePositionInWorld();
		this.setToolTipText((int)p.x + ", " + (int)p.y);
	}

	/**
	 * Startup OpenGL.  Turn on debug pipeline(s) if needed.
	 */
	@Override
	public void init(GLAutoDrawable glautodrawable) {
		logger.debug("init");
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

		// turn on vsync
		gl.setSwapInterval(1);

		// make things pretty
		gl.glEnable(GL3.GL_LINE_SMOOTH);
		gl.glEnable(GL3.GL_POLYGON_SMOOTH);
		gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
		gl.glEnable(GL3.GL_MULTISAMPLE);
	}

	@Override
	public void dispose(GLAutoDrawable glautodrawable) {
		logger.info("dispose");
		TextureFactory.dispose(glautodrawable.getGL());
	}

	/**
	 * Refresh the image in the view.  This is where drawing begins.
	 */
	@Override
	public void display(GLAutoDrawable glautodrawable) {
		// draw the world
		GL2 gl2 = glautodrawable.getGL().getGL2();

		camera.setWidth(canvasWidth);
		camera.setHeight(canvasHeight);

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


	/**
	 * Set up the correct modelview so the robot appears where it should.
	 *
	 * @param gl2 OpenGL context
	 */
	private void paintCamera(GL2 gl2) {
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		// orthographic projection
		float w2 = canvasWidth/2.0f;
		float h2 = canvasHeight/2.0f;
		glu.gluOrtho2D(-w2, w2, -h2, h2);

		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		gl2.glScaled(camera.getZoom(),camera.getZoom(),1);
		gl2.glTranslated(-camera.getX(), camera.getY(),0);
	}

	/**
	 * Clear the panel
	 *
	 * @param gl2 OpenGL context
	 */
	private void paintBackground(GL2 gl2) {
		// Clear The Screen And The Depth Buffer
		gl2.glClearColor(
				(float)backgroundColor.getRed()/255.0f,
				(float)backgroundColor.getGreen()/255.0f,
				(float)backgroundColor.getBlue()/255.0f,
				0.0f);

		gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
	}

	public void stop() {
		animator.stop();
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		glCanvas.addGLEventListener(this);
		glCanvas.addMouseListener(this);
		glCanvas.addMouseMotionListener(this);
		glCanvas.addMouseWheelListener(this);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		glCanvas.removeGLEventListener(this);
		glCanvas.removeMouseListener(this);
		glCanvas.removeMouseMotionListener(this);
		glCanvas.removeMouseWheelListener(this);
	}

	public Component getCanvas() {
		return glCanvas;
	}
}