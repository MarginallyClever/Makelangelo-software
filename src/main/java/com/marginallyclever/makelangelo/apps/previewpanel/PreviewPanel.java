package com.marginallyclever.makelangelo.apps.previewpanel;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.makelangelo.Camera;
import com.marginallyclever.makelangelo.MeshFactory;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;
import com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer.PlotterRenderer;
import com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer.PlotterRendererFactory;
import com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer.TurtleRenderFacade;
import com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer.TurtleRenderFactory;
import com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer.TurtleRenderer;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.rangeslider.DoubleRangeSlider;
import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * OpenGL hardware accelerated WYSIWYG view.
 * @author Dan Royer
 *
 */
public class PreviewPanel extends JPanel implements GLEventListener {
	private static final Logger logger = LoggerFactory.getLogger(PreviewPanel.class);

	private final EventListenerList previewListeners = new EventListenerList();
	private final JToolBar toolBar = new JToolBar();
	private final GLJPanel glCanvas = new GLJPanel();
	private final Camera camera = new Camera();

	public final Color backgroundColor = new Color(255-67,255-67,255-67);

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

	private final Plotter myPlotter;
	private final Paper myPaper;
	private PlotterRenderer myPlotterRenderer;
	private final TurtleRenderFacade myTurtleRenderer = new TurtleRenderFacade();
	private final DoubleRangeSlider rangeSlider = new DoubleRangeSlider();

	// OpenGL stuff
	private FPSAnimator animator;
	private ShaderProgram shaderDefault;
	private ShaderProgram shaderLine;

	public PreviewPanel() {
		this(new Paper(),new Plotter());
	}

	public PreviewPanel(Paper paper, Plotter plotter) {
		super(new BorderLayout());
		myPlotter = plotter;
		myPaper = paper;

		add(toolBar, BorderLayout.NORTH);
		add(glCanvas, BorderLayout.CENTER);
		add(rangeSlider, BorderLayout.SOUTH);

		addListener(paper);
		addListener(myTurtleRenderer);
		addListener(context -> {
			if(myPlotterRenderer!=null) {
				myPlotterRenderer.render(context, myPlotter);
			}
		});

		setGLCapabilities();
		glCanvas.addGLEventListener(this);
		addMouseListeners();

		rangeSlider.addChangeListener(e->{
			myTurtleRenderer.setFirst(rangeSlider.getBottom());
			myTurtleRenderer.setLast(rangeSlider.getTop());
		});

		buildToolBar();

		// start animation system
		logger.debug("  starting animator...");
		animator = new FPSAnimator(1);
		animator.add(glCanvas);
		animator.start();
	}

	private void addMouseListeners() {
		final JPanel me = this;
		// scroll the mouse wheel to zoom
		addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int notches = e.getWheelRotation();
				if (notches == 0) return;

				Point2d p = new Point2d(e.getPoint().x,e.getPoint().y);
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
				int x = e.getX();
				int y = e.getY();
				mouseX = x;
				mouseY = y;
				setToolTipXY();

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
			public void mouseMoved(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				mouseOldX = x;
				mouseOldY = y;
				mouseX = x;
				mouseY = y;
				setToolTipXY();
			}
		});
	}

	private void setGLCapabilities() {
		try {
			logger.debug("  get GL capabilities...");
			GLProfile glProfile = GLProfile.getDefault();
			GLCapabilities caps = new GLCapabilities(glProfile);
			// caps.setSampleBuffers(true);
			// caps.setHardwareAccelerated(true);
			// caps.setNumSamples(4);
			glCanvas.setRequestedGLCapabilities(caps);
		} catch(GLException e) {
			logger.error("I failed the very first call to OpenGL.  Are your native libraries missing?", e);
			System.exit(1);
		}
	}

	private void buildToolBar() {
		JButton buttonZoomOut = new JButton(Translator.get("MenuView.zoomOut"));
		//buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, SHORTCUT_CTRL));
		buttonZoomOut.addActionListener((e) -> camera.zoom(1));
		buttonZoomOut.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-zoom-out-16.png"))));
		toolBar.add(buttonZoomOut);

		JButton buttonZoomIn = new JButton(Translator.get("MenuView.zoomIn"));
		//buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, SHORTCUT_CTRL));
		buttonZoomIn.addActionListener((e) -> camera.zoom(-1));
		buttonZoomIn.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-zoom-in-16.png"))));
		toolBar.add(buttonZoomIn);

		JButton buttonZoomToFit = new JButton(Translator.get("MenuView.zoomFit"));
		//buttonZoomToFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, SHORTCUT_CTRL));
		buttonZoomToFit.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-zoom-to-extents-16.png"))));
		buttonZoomToFit.addActionListener((e) -> camera.zoomToFit(myPaper.getPaperWidth(),myPaper.getPaperHeight()));
		toolBar.add(buttonZoomToFit);

		JCheckBox checkboxShowPenUpMoves = new JCheckBox(Translator.get("GFXPreferences.showPenUp"), GFXPreferences.getShowPenUp());
		//checkboxShowPenUpMoves.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, SHORTCUT_CTRL));//"ctrl M"
		checkboxShowPenUpMoves.addActionListener((e) -> {
			boolean b = GFXPreferences.getShowPenUp();
			GFXPreferences.setShowPenUp(!b);
			TurtleRenderFactory.resetAll();
		});
		GFXPreferences.addListener((e)->{
			checkboxShowPenUpMoves.setSelected((boolean)e.getNewValue());
		});
		checkboxShowPenUpMoves.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-plane-16.png"))));
		toolBar.add(checkboxShowPenUpMoves);

		JLabel label = new JLabel(Translator.get("RobotMenu.RenderStyle"));
		toolBar.add(label);
		TurtleRenderFactory[] values = TurtleRenderFactory.values();
		String [] names = Arrays.stream(values).map(TurtleRenderFactory::getName).toArray(String[]::new);
		JComboBox<String> comboBox = new JComboBox<>(names);
		comboBox.setSelectedIndex(0);
		comboBox.addActionListener((e)->{
			JComboBox<?> cb = (JComboBox<?>)e.getSource();
			String name = (String)cb.getSelectedItem();
			onTurtleRenderChange(name);
		});
		// Set the maximum size of the JComboBox to its preferred size
		Dimension preferredSize = comboBox.getPreferredSize();
		comboBox.setMaximumSize(preferredSize);
		toolBar.add(comboBox);
		label.setLabelFor(comboBox);
	}

	private void onTurtleRenderChange(String name) {
		logger.debug("Switching to render style '{}'", name);
		TurtleRenderer renderer = TurtleRenderFactory.findByName(name).getTurtleRenderer();
		setTurtleRenderer(renderer);
	}

	public void addListener(PreviewListener arg0) {
		previewListeners.add(PreviewListener.class,arg0);
	}
	
	public void removeListener(PreviewListener arg0) {
		previewListeners.remove(PreviewListener.class,arg0);
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

	private void setToolTipXY() {
		Vector2d p = getMousePositionInWorld();
		this.setToolTipText((int)p.x + ", " + (int)p.y);
	}

	/**
	 * Startup OpenGL.  Turn on debug pipeline(s) if needed.
	 */
	@Override
	public void init(GLAutoDrawable glAutoDrawable) {
		logger.debug("init");
		GL3 gl3 = glAutoDrawable.getGL().getGL3();
		gl3.setSwapInterval(1);

		try {
			shaderDefault = new ShaderProgram(gl3,
					ResourceHelper.readResource(this.getClass(), "default.vert"),
					ResourceHelper.readResource(this.getClass(), "default.frag"));
		} catch(Exception e) {
			logger.error("Failed to load default shader", e);
		}

		try {
			shaderLine = new ShaderProgram(gl3,
					ResourceHelper.readResource(this.getClass(), "line.vert"),
					ResourceHelper.readResource(this.getClass(), "line.frag"));
			OpenGLHelper.checkGLError(gl3,logger);
		} catch(Exception e) {
			OpenGLHelper.checkGLError(gl3,logger);
			logger.error("Failed to load line shader", e);
		}

		myTurtleRenderer.setLineShader(shaderLine);
	}

	@Override
	public void dispose(GLAutoDrawable glAutoDrawable) {
		logger.debug("dispose");
		GL3 gl = glAutoDrawable.getGL().getGL3();
		TextureFactory.unloadAll(gl);
		MeshFactory.unloadAll(gl);
		shaderDefault.delete(gl);
	}

	/**
	 * Set up the correct projection so the image appears in the right location and aspect ratio.
	 */
	@Override
	public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
		logger.debug("reshape {}x{}",width,height);
		camera.setWidth(width);
		camera.setHeight(height);
	}

	/**
	 * Refresh the image in the view.  This is where drawing begins.
	 */
	@Override
	public void display(GLAutoDrawable glautodrawable) {
		// draw the world
		GL3 gl3 = glautodrawable.getGL().getGL3();

		// set some render quality options
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		if(prefs != null && prefs.getBoolean("antialias", true)) {
			gl3.glEnable(GL3.GL_LINE_SMOOTH);
			gl3.glEnable(GL3.GL_POLYGON_SMOOTH);
			gl3.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
			gl3.glEnable(GL3.GL_MULTISAMPLE);
		} else {
			gl3.glDisable(GL3.GL_LINE_SMOOTH);
			gl3.glDisable(GL3.GL_POLYGON_SMOOTH);
			gl3.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_FASTEST);
			gl3.glDisable(GL3.GL_MULTISAMPLE);
		}
		
		// turn on blending
		gl3.glEnable(GL3.GL_BLEND);
		gl3.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);

		gl3.glDisable(GL3.GL_CULL_FACE);
		gl3.glDisable(GL3.GL_DEPTH_TEST);

		paintBackground(gl3);

		shaderDefault.use(gl3);
		//shader.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space
		shaderDefault.setColor(gl3,"lightColor", Color.WHITE);
		shaderDefault.setColor(gl3,"diffuseColor",Color.WHITE);
		shaderDefault.setColor(gl3,"specularColor",Color.WHITE);
		shaderDefault.setColor(gl3,"ambientColor",Color.BLACK);
		shaderDefault.set1i(gl3,"useVertexColor",1);
		shaderDefault.set1i(gl3,"useLighting",0);
		shaderDefault.set1i(gl3,"diffuseTexture",0);
		shaderDefault.set1i(gl3,"useTexture",0);

		var list = previewListeners.getListeners(PreviewListener.class);
		ArrayUtils.reverse(list);
		for( PreviewListener p : list ) {
			if(p instanceof TurtleRenderFacade) {
				shaderLine.use(gl3);
				shaderLine.set1f(gl3,"viewportWidth",camera.getWidth());
				shaderLine.set1f(gl3,"viewportHeight",camera.getHeight());
				float height = camera.getHeight(); // Math.max(camera.getHeight(),camera.getWidth());
				float zoom = height / (float)(camera.getZoom());
				//zoom = Math.max(5.0f,zoom);
				shaderLine.set1f(gl3,"zoom",zoom);
				paintCamera(gl3,shaderLine);
			} else {
				shaderDefault.use(gl3);
				paintCamera(gl3, shaderDefault);
			}
			p.render(new RenderContext(gl3, shaderDefault));
		}
	}

	/**
	 * Set up the correct model view so the robot appears where it should.
	 *
	 * @param gl3 OpenGL context
	 */
	private void paintCamera(GL3 gl3,ShaderProgram program) {
		program.setMatrix4d(gl3,"modelMatrix", MatrixHelper.createIdentityMatrix4());

		Matrix4d inverseCamera = MatrixHelper.createIdentityMatrix4();
		inverseCamera.setTranslation(new Vector3d(camera.getX(),-camera.getY(),camera.getZoom()));
		inverseCamera.invert();
		inverseCamera.transpose();
		program.setMatrix4d(gl3,"viewMatrix",inverseCamera);

		program.setMatrix4d(gl3,"projectionMatrix",getPerspectiveFrustum(camera.getWidth(),camera.getHeight()));
		//program.setMatrix4d(gl3,"projectionMatrix",getOrthographicMatrix(camera.getWidth(),camera.getHeight()));

		// only needed with useLighting=1
		//Vector3d cameraWorldPos = new Vector3d(-camera.getX(),camera.getY(),-5);
		//shader.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
	}

	/**
	 * Render the scene in orthographic projection.
	 */
	public Matrix4d getOrthographicMatrix(int width,int height) {
		double h = 5.0;  // why 5?
		double w = h * (double)width / (double)height;
		return MatrixHelper.orthographicMatrix4d(-w,w,-h,h,1,100);
	}


	public Matrix4d getPerspectiveFrustum(int width,int height) {
		double aspect = (double)width / (double)height;
		return MatrixHelper.perspectiveMatrix4d(60,aspect,1,1600);
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

		// Special handling for the case where the GLJPanel is translucent
		// and wants to be composited with other Java 2D content
		if (GLProfile.isAWTAvailable()
				&& !isOpaque()
				&& glCanvas.shouldPreserveColorBufferIfTranslucent()) {
			gl.glClear(GL3.GL_DEPTH_BUFFER_BIT);
		} else {
			gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
		}
	}

	public void stop() {
		animator.stop();
	}

	public void setTurtle(Turtle turtle) {
		myTurtleRenderer.setTurtle(turtle);
		rangeSlider.setLimits(0,turtle.history.size());
	}

	public int getRangeBottom() {
		return rangeSlider.getValue();
	}

	public int getRangeTop() {
		return rangeSlider.getUpperValue();
	}

	public TurtleRenderer getTurtleRenderer() {
		return myTurtleRenderer.getRenderer();
	}

	public void setTurtleRenderer(TurtleRenderer renderer) {
		myTurtleRenderer.setRenderer(renderer);
	}

	public void updatePlotterSettings(PlotterSettings settings) {
		try {
			myPlotter.getSettings().load(myPlotter.getSettings().getUID());
			var style = myPlotter.getSettings().getString(PlotterSettings.STYLE);
			myPlotterRenderer = PlotterRendererFactory.valueOf(style).getPlotterRenderer();
		} catch (Exception e) {
			logger.error("Failed to find plotter style {}", myPlotter.getSettings().getString(PlotterSettings.STYLE));
			myPlotterRenderer = PlotterRendererFactory.MAKELANGELO_5.getPlotterRenderer();
		}
		myPlotterRenderer.updatePlotterSettings(settings);
	}
}