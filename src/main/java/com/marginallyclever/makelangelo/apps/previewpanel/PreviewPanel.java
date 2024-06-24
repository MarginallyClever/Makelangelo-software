package com.marginallyclever.makelangelo.apps.previewpanel;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Camera;
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
import javax.vecmath.Vector2d;
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
	// Use debug pipeline?
	private static final boolean DEBUG_GL_ON = false;
	private static final boolean TRACE_GL_ON = false;

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

	private final Paper myPaper;
	private final Plotter myPlotter;
	private PlotterRenderer myPlotterRenderer;
	private final TurtleRenderFacade myTurtleRenderer = new TurtleRenderFacade();
	private final DoubleRangeSlider rangeSlider = new DoubleRangeSlider();

	// OpenGL stuff
	private GLU glu;
	private FPSAnimator animator;

	public PreviewPanel() {
		this(new Paper(),new Plotter());
	}

	public PreviewPanel(Paper paper, Plotter plotter) {
		super(new BorderLayout());
		myPaper = paper;
		myPlotter = plotter;

		add(toolBar, BorderLayout.NORTH);
		add(glCanvas, BorderLayout.CENTER);
		add(rangeSlider, BorderLayout.SOUTH);

		addListener(myPaper);
		addListener(myPlotter);
		addListener((gl2)->{
			if(myPlotterRenderer!=null) {
				myPlotterRenderer.render(gl2, myPlotter);
			}
		});
		addListener(myTurtleRenderer);

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

		glCanvas.addGLEventListener(this);

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
			public void mouseMoved(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				mouseOldX = x;
				mouseOldY = y;
				mouseX = x;
				mouseY = y;
				setTipXY();
			}
		});

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
/*
		JButton buttonZoomToFit = new JButton(Translator.get("MenuView.zoomFit"));
		//buttonZoomToFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, SHORTCUT_CTRL));
		buttonZoomToFit.addActionListener((e) -> camera.zoomToFit(app.getPaper().getPaperWidth(),app.getPaper().getPaperHeight()));
		toolBar.add(buttonZoomToFit);
*/
		JCheckBoxMenuItem checkboxShowPenUpMoves = new JCheckBoxMenuItem(Translator.get("GFXPreferences.showPenUp"), GFXPreferences.getShowPenUp());
		//checkboxShowPenUpMoves.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, SHORTCUT_CTRL));//"ctrl M"
		checkboxShowPenUpMoves.addActionListener((e) -> {
			boolean b = GFXPreferences.getShowPenUp();
			GFXPreferences.setShowPenUp(!b);
		});
		GFXPreferences.addListener((e)->{
			checkboxShowPenUpMoves.setSelected ((boolean)e.getNewValue());
		});
		checkboxShowPenUpMoves.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-plane-16.png"))));
		toolBar.add(checkboxShowPenUpMoves);

		TurtleRenderFactory[] values = TurtleRenderFactory.values();
		String [] names = Arrays.stream(values).map(TurtleRenderFactory::getName).toArray(String[]::new);
		JComboBox<String> comboBox = new JComboBox<>(names);
		comboBox.setSelectedIndex(0);
		comboBox.addActionListener((e)->{
			JComboBox<?> cb = (JComboBox<?>)e.getSource();
			String name = (String)cb.getSelectedItem();
			onTurtleRenderChange(name);
		});
		toolBar.add(comboBox);
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
	
	/**
	 * Set up the correct projection so the image appears in the right location and aspect ratio.
	 */
	@Override
	public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
		logger.debug("reshape {}x{}",width,height);
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		camera.setWidth(width);
		camera.setHeight(height);

		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		// orthographic projection
		float w2 = width/2.0f;
		float h2 = height/2.0f;
		glu.gluOrtho2D(-w2, w2, -h2, h2);
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
		maybeAddDebugPipeline(gl);
		maybeAddTracePipeline(gl);
		// must come after adding any extra pipelines.
		glu = GLU.createGLU(gl);
	}

	@Override
	public void dispose(GLAutoDrawable glautodrawable) {
		logger.debug("dispose");
		GL gl = glautodrawable.getGL();
		TextureFactory.unloadAll(gl);
	}

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

		var list = previewListeners.getListeners(PreviewListener.class);
		ArrayUtils.reverse(list);
		for( PreviewListener p : list ) {
			gl2.glPushMatrix();
			p.render(gl2);
			gl2.glPopMatrix();
		}
	}

	private void maybeAddTracePipeline(GL gl) {
		if (!TRACE_GL_ON) return;

		try {
			// Trace ..
			gl = gl.getContext().setGL(
					GLPipelineFactory.create("com.jogamp.opengl.Trace", null, gl, new Object[] { System.err }));
		} catch (Exception e) {
			logger.error("Failed to init OpenGL trace", e);
		}
	}

	private void maybeAddDebugPipeline(GL gl) {
		if (!DEBUG_GL_ON) return;

		try {
			// Debug ..
			gl = gl.getContext().setGL(GLPipelineFactory.create("com.jogamp.opengl.Debug", null, gl, null));
		} catch (Exception e) {
			logger.error("Failed to init OpenGL debug pipeline", e);
		}
	}

	/**
	 * Set up the correct modelview so the robot appears where it should.
	 *
	 * @param gl2 OpenGL context
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
	 * @param gl2 OpenGL context
	 */
	private void paintBackground(GL2 gl2) {
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
				&& glCanvas.shouldPreserveColorBufferIfTranslucent()) {
			gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		} else {
			gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
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

	public void updatePlotterRenderer() {
		try {
			myPlotterRenderer = PlotterRendererFactory.valueOf(myPlotter.getSettings().getString(PlotterSettings.STYLE)).getPlotterRenderer();
		} catch (Exception e) {
			logger.error("Failed to find plotter style {}", myPlotter.getSettings().getString(PlotterSettings.STYLE));
			myPlotterRenderer = PlotterRendererFactory.MAKELANGELO_5.getPlotterRenderer();
		}
	}
}