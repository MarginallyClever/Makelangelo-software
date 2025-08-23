package com.marginallyclever.makelangelo.preview;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * OpenGL hardware accelerated WYSIWYG view.
 */
public class OpenGLPanel extends JPanel implements GLEventListener, MouseWheelListener, MouseListener, MouseMotionListener {
	private static final Logger logger = LoggerFactory.getLogger(OpenGLPanel.class);
	
	// Use debug pipeline?
	private static boolean DEBUG_GL_ON = true;
	private static boolean TRACE_GL_ON = true;
	private GLJPanel glCanvas;
	private int canvasWidth,canvasHeight;

	private final List<PreviewListener> previewListeners = new ArrayList<>();
	
	private Camera camera;

	public Color backgroundColor = new Color(255-67,255-67,255-67);

	private ShaderProgram shaderProgram;

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
	private final FPSAnimator animator = new FPSAnimator(1);

	public OpenGLPanel() {
		super(new BorderLayout());
		
		try {
			logger.info("availability="+ GLProfile.glAvailabilityToString());
			GLCapabilities capabilities = getCapabilities();
			logger.info("create canvas");
			glCanvas = new GLJPanel(capabilities);
		} catch(GLException e) {
			logger.error("I failed the very first call to OpenGL.  Are your native libraries missing?", e);
			JOptionPane.showMessageDialog(null,
					Translator.get("OpenGLPanel.errorNoGL3"),
					Translator.get("ErrorTitle"),
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		add(glCanvas, BorderLayout.CENTER);

		// start animation system
		logger.debug("  starting animator...");
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

	/**
	 * Get the OpenGL capabilities for this panel.
	 * @return the OpenGL capabilities
	 * @throws GLException if the OpenGL profile is below minimum requirements
	 */
	private GLCapabilities getCapabilities() throws GLException {
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
		if(!profile.isGL3()) throw new GLException("OpenGL 3.0 or higher is required.");
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
	public void init(GLAutoDrawable glAutoDrawable) {
		logger.debug("init");
		glAutoDrawable.getGL().getGL3();

		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		if(prefs != null) {
			DEBUG_GL_ON = prefs.getBoolean("debug GL", false);
			TRACE_GL_ON = prefs.getBoolean("trace GL", false);
		}

		activatePipelines(glAutoDrawable);

		var gl = glAutoDrawable.getGL().getGL3();
		glu = GLU.createGLU(gl);

		// turn on vsync
		gl.setSwapInterval(1);

		// make things pretty
		gl.glEnable(GL3.GL_LINE_SMOOTH);
		gl.glHint(GL3.GL_LINE_SMOOTH_HINT, GL3.GL_NICEST);

		gl.glEnable(GL3.GL_POLYGON_SMOOTH);
		gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);

		gl.glEnable(GL3.GL_MULTISAMPLE);

		try {
			shaderProgram = new ShaderProgram(gl,
					ResourceHelper.readResource(this.getClass(), "default.vert"),
					ResourceHelper.readResource(this.getClass(), "default.frag"));
		} catch(Exception e) {
			logger.error("Failed to load shader", e);
		}
	}

	private void activatePipelines(GLAutoDrawable glautodrawable) {
		GL3 gl = glautodrawable.getGL().getGL3();
		if (DEBUG_GL_ON) {
			logger.info("Activating debug pipeline");
			gl = new DebugGL3(gl);
		}
		if (TRACE_GL_ON) {
			logger.info("Activating trace pipeline");
			gl = new TraceGL3(gl, new PrintStream(System.out));
		}
		glautodrawable.setGL(gl);
	}

	@Override
	public void dispose(GLAutoDrawable glautodrawable) {
		logger.info("dispose");
		var gl = glautodrawable.getGL().getGL3();
		TextureFactory.dispose(gl);
		shaderProgram.dispose(gl);
		for( PreviewListener p : previewListeners ) {
			p.dispose();
		}
	}

	/**
	 * Refresh the image in the view.  This is where drawing begins.
	 */
	@Override
	public void display(GLAutoDrawable glAutoDrawable) {
		// draw the world
		var gl = glAutoDrawable.getGL().getGL3();

		camera.setWidth(canvasWidth);
		camera.setHeight(canvasHeight);

		// set some render quality options
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		if(prefs != null && prefs.getBoolean("antialias", true)) {
			gl.glEnable(GL3.GL_LINE_SMOOTH);
			gl.glHint(GL3.GL_LINE_SMOOTH_HINT, GL3.GL_NICEST);
			gl.glEnable(GL3.GL_POLYGON_SMOOTH);
			gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
		} else {
			gl.glDisable(GL3.GL_LINE_SMOOTH);
			gl.glHint(GL3.GL_LINE_SMOOTH_HINT, GL3.GL_FASTEST);
			gl.glDisable(GL3.GL_POLYGON_SMOOTH);
			gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_FASTEST);
		}
		
		// turn on blending
		gl.glEnable(GL3.GL_BLEND);
		gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
		
		paintBackground(gl);

		gl.glDisable(GL3.GL_DEPTH_TEST);
		paintCamera(gl);

		for( PreviewListener p : previewListeners ) {
			p.render(shaderProgram,gl);
		}

		//renderTestTriangle(gl);

		gl.glEnable(GL3.GL_DEPTH_TEST);
	}

	private void renderTestTriangle(GL3 gl) {
		float d = 150f;
		Mesh triangleMesh = new Mesh();
		triangleMesh.setRenderStyle(GL3.GL_TRIANGLES);
		triangleMesh.addColor(1.0f, 0.0f, 0.0f, 1.0f);  triangleMesh.addVertex(0.0f, d, 0.0f);
		triangleMesh.addColor(0.0f, 1.0f, 0.0f, 1.0f);  triangleMesh.addVertex(-d, -d, 0.0f);
		triangleMesh.addColor(0.0f, 0.0f, 1.0f, 1.0f);  triangleMesh.addVertex(d, -d, 0.0f);
		triangleMesh.render(gl);
	}

	/**
	 * Set up the correct modelview so the robot appears where it should.
	 * @param gl OpenGL context
	 */
	private void paintCamera(GL3 gl) {
		shaderProgram.use(gl);
		var projectionMatrix = camera.getOrthographicMatrix(canvasWidth,canvasHeight);
		shaderProgram.setMatrix4d(gl,"projectionMatrix", projectionMatrix);

		var m = camera.getViewMatrix();
		shaderProgram.setVector3d(gl,"cameraPos", new Vector3d(m.m03,m.m13,m.m23));
		shaderProgram.setMatrix4d(gl,"viewMatrix", m);

		var identity = new Matrix4d();
		identity.setIdentity();
		shaderProgram.setMatrix4d(gl,"modelMatrix", identity);

		shaderProgram.setColor(gl, "lightColor", Color.WHITE);
		shaderProgram.setColor(gl, "diffuseColor", Color.WHITE);
		shaderProgram.setColor(gl, "specularColor", Color.WHITE);
		shaderProgram.setColor(gl,"ambientColor",Color.BLACK);
		shaderProgram.set1i(gl,"useTexture",0);
		shaderProgram.set1i(gl,"useLighting",0);
		shaderProgram.set1i(gl,"useVertexColor",1);
	}

	/**
	 * Clear the panel
	 *
	 * @param gl OpenGL context
	 */
	private void paintBackground(GL3 gl) {
		// Clear The Screen And The Depth Buffer
		gl.glClearColor(
				(float)backgroundColor.getRed()/255.0f,
				(float)backgroundColor.getGreen()/255.0f,
				(float)backgroundColor.getBlue()/255.0f,
				0.0f);

		gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
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