package com.marginallyclever.makelangelo;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.makeart.io.LoadFilePanel;
import com.marginallyclever.makelangelo.makeart.io.OpenFileChooser;
import com.marginallyclever.makelangelo.makeart.io.SaveGCode;
import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.applicationsettings.LanguagePreferences;
import com.marginallyclever.makelangelo.applicationsettings.MetricsPreferences;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRenderer;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRendererFactory;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.preview.Camera;
import com.marginallyclever.makelangelo.preview.PreviewPanel;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.MarlinSimulationVisualizer;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFacade;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFactory;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderer;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.Locale;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * <p>The Makelangelo app is a tool for programming CNC robots, typically plotters.  It converts lines (made of
 * segments made of points) into instructions in GCODE format, as described in <a
 * href="https://github.com/MarginallyClever/Makelangelo-firmware/wiki/gcode-description">the wiki</a>.</p>
 * <p>In order to do this the app also provides convenient methods to load vectors (like DXF or SVG), create vectors
 * ({@link com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator}s), or
 * interpret bitmaps (like BMP,JPEG,PNG,GIF,TGA,PIO) into vectors
 * ({@link com.marginallyclever.makelangelo.makeart.imageconverter.ImageConverter}s).</p>
 * <p>The app must also know some details about the machine, the surface onto which drawings will be made, and the
 * drawing tool making the mark on the paper.  This knowledge helps the app to create better gcode.</p>
 *
 * @author Dan Royer (dan@marginallyclever.com)
 * @since 1.00 2012/2/28
 */
public final class Makelangelo {
	private static final String PREFERENCE_SAVE_PATH = "savePath";
	private static Logger logger;
	private final Camera camera;
	private final PlotterSettingsManager plotterSettingsManager = new PlotterSettingsManager();
	private final Plotter myPlotter = new Plotter();
	private final Paper myPaper = new Paper();
	private Turtle myTurtle = new Turtle();
	private final TurtleRenderFacade myTurtleRenderer = new TurtleRenderFacade();
	private PlotterRenderer myPlotterRenderer;
	
	// GUI elements
	private MainFrame mainFrame;
	private final MainMenu mainMenuBar = new MainMenu(this);
	private PreviewPanel previewPanel;
	private final MakeleangeloRangeSlider rangeSlider = new MakeleangeloRangeSlider();


	public Makelangelo() {
		super();

		logger.info("Locale={}", Locale.getDefault().toString());
		logger.info("Headless={}", (GraphicsEnvironment.isHeadless()?"Y":"N"));

		myPlotter.setSettings(plotterSettingsManager.getLastSelectedProfile());
		myPaper.loadConfig();

		if(previewPanel != null) {
			previewPanel.addListener(myPlotter);
			addPlotterRendererToPreviewPanel();
		}

		rangeSlider.addChangeListener(e->{
			myTurtleRenderer.setFirst(rangeSlider.getBottom());
			myTurtleRenderer.setLast(rangeSlider.getTop());
		});

		onPlotterSettingsUpdate(myPlotter.getSettings());
		
		logger.debug("Starting virtual camera...");
		camera = new Camera();
	}

	private void updatePlotterRenderer() {
		try {
			myPlotterRenderer = PlotterRendererFactory.valueOf(myPlotter.getSettings().getString(PlotterSettings.STYLE)).getPlotterRenderer();
		} catch (Exception e) {
			logger.error("Failed to find plotter style {}", myPlotter.getSettings().getString(PlotterSettings.STYLE));
			myPlotterRenderer = PlotterRendererFactory.MAKELANGELO_5.getPlotterRenderer();
		}
	}

	public void onPlotterSettingsUpdate(PlotterSettings settings) {
		myPlotter.setSettings(settings);

		TurtleRenderer turtleRenderer = TurtleRenderFactory.MARLIN_SIM.getTurtleRenderer();
		if(turtleRenderer instanceof MarlinSimulationVisualizer msv) {
			msv.setSettings(settings);
			msv.reset();
		}
		myTurtleRenderer.setUpColor(settings.getColor(PlotterSettings.PEN_UP_COLOR));
		myTurtleRenderer.setPenDiameter(settings.getDouble(PlotterSettings.DIAMETER));
		// myTurtleRenderer.setDownColor() would be meaningless, the down color is stored in each Turtle.

		updatePlotterRenderer();

		if(previewPanel != null) previewPanel.repaint();
	}

	private void addPlotterRendererToPreviewPanel() {
		previewPanel.addListener((gl2)->{
			if(myPlotterRenderer!=null) {
				myPlotterRenderer.render(gl2, myPlotter);
			}
		});
	}

	public void run() {
		createAppWindow();		
		//checkSharingPermission();

		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
		if (preferences.getBoolean("Check for updates", false)) checkForUpdate(true);
	}

	private static void setSystemLookAndFeel() {
		if(!CommandLineOptions.hasOption("-nolf")) {
			try {
				FlatLaf.registerCustomDefaultsSource( "com.marginallyclever.makelangelo" );
				UIManager.setLookAndFeel( new FlatLightLaf() );
			} catch( Exception e ) {
				logger.warn("failed to set flat look and feel. falling back to default native lnf", e);
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception ex) {
					logger.warn("failed to set native look and feel.", ex);
				}
			}
		}
	}

	// check if we need to ask about sharing
	@SuppressWarnings("unused")
	private void checkSharingPermission() {
		logger.debug("Checking sharing permissions...");
		
		final String SHARING_CHECK_STRING = "Last version sharing checked";
		
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		String v = preferences.get(SHARING_CHECK_STRING,"0");
		int comparison = MakelangeloVersion.VERSION.compareTo(v);
		if(comparison!=0) {
			preferences.put(SHARING_CHECK_STRING, MakelangeloVersion.VERSION);
			int dialogResult = JOptionPane.showConfirmDialog(mainFrame, Translator.get("MetricsPreferences.collectAnonymousMetricsOnUpdate"),"Sharing Is Caring",JOptionPane.YES_NO_OPTION);
			MetricsPreferences.setAllowedToShare(dialogResult == JOptionPane.YES_OPTION);
		}
	}

	/**
	 * Change the enable state of the menu items inside the {@code mainMenuBar}.
	 * Remember that enabling the menuBar does not affect the children.
	 * @param b the new state
	 */
	public void enableMenuBar(boolean b) {
		int c = mainMenuBar.getMenuCount();
		while(--c>=0) {
			mainMenuBar.getMenu(c).setEnabled(b);
		}
	}

	public void openFile(String filename) {
		try {
			LoadFilePanel loader = new LoadFilePanel(myPaper,filename);
			loader.addActionListener((e)-> setTurtle((Turtle)(e).getSource()));

			if(filename == null || filename.trim().isEmpty()) throw new InvalidParameterException("filename cannot be empty");

			if (loader.onNewFilenameChosen(filename)) {
				previewPanel.addListener(loader);
				JDialog dialog = new JDialog(mainFrame, Translator.get("LoadFilePanel.title"));
				dialog.add(loader);
				dialog.setMinimumSize(new Dimension(500,500));
				dialog.pack();
				dialog.setLocationRelativeTo(mainFrame);
				loader.setParent(dialog);

				enableMenuBar(false);
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						loader.loadingFinished();
						enableMenuBar(true);
						previewPanel.removeListener(loader);
						mainMenuBar.getRecentFiles().addFilename(filename);
					}
				});

				dialog.setVisible(true);
			} else {
				mainMenuBar.getRecentFiles().addFilename(filename);
			}

			setMainTitle(new File(filename).getName());
		} catch(Exception e) {
			logger.error("Error while loading the file {}", filename, e);
			JOptionPane.showMessageDialog(mainFrame, Translator.get("LoadError") + e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
			mainMenuBar.getRecentFiles().removeFilename(filename);
		}
	}

	/**
	 * Load a vector and add it to the existing {@link Turtle}.
	 */
	public void importFile() {
		JFileChooser jFileChooser = new JFileChooser();

		// add vector formats
		for (FileNameExtensionFilter ff : TurtleFactory.getLoadExtensions()) {
			jFileChooser.addChoosableFileFilter(ff);
		}

		// no wild card filter, please.
		jFileChooser.setAcceptAllFileFilterUsed(false);

		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
		String lastPath = preferences.get(OpenFileChooser.KEY_PREFERENCE_LOAD_PATH, FileAccess.getWorkingDirectory());
		jFileChooser.setCurrentDirectory(new File(lastPath));

		if (jFileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
			String filename = jFileChooser.getSelectedFile().getAbsolutePath();
			preferences.put(OpenFileChooser.KEY_PREFERENCE_LOAD_PATH, jFileChooser.getCurrentDirectory().toString());
			logger.debug("File selected by user: {}", filename);
			try {
				Turtle t = TurtleFactory.load(filename);
				myTurtle.add(t);
				setTurtle(myTurtle);
			} catch(Exception e) {
				logger.error("Failed to load {}", filename, e);
				JOptionPane.showMessageDialog(mainFrame, e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void onDialogAbout() {
		DialogAbout a = new DialogAbout();
		a.display(mainFrame,MakelangeloVersion.VERSION, MakelangeloVersion.DETAILED_VERSION);
	}

	/**
	 * Parse <a href="https://github.com/MarginallyClever/Makelangelo/releases/latest">https://github.com/MarginallyClever/Makelangelo/releases/latest</a>
	 * redirect notice to find the latest release tag.
	 */
	public void checkForUpdate(boolean announceIfFailure) {
		logger.debug("checking for updates...");
		try {
			URI link = new URI("https://github.com/MarginallyClever/Makelangelo-Software/releases/latest");
			HttpURLConnection conn = (HttpURLConnection)link.toURL().openConnection();
			conn.setInstanceFollowRedirects(false); // you still need to handle redirect manually.
			conn.setConnectTimeout(5000);
			conn.connect();
			int responseCode = conn.getResponseCode();
			String responseMessage = conn.getHeaderField("Location");
			conn.disconnect();

			// parse the last part of the redirect URL, which contains the
			// release tag (which is the VERSION)
			String line2 = responseMessage.substring(responseMessage.lastIndexOf("/") + 1);

			logger.debug("latest release: {}; this version: {}@{}", line2, MakelangeloVersion.VERSION, MakelangeloVersion.DETAILED_VERSION);

			int comp = line2.compareTo(MakelangeloVersion.VERSION);
			String results;
			if (comp > 0) results = Translator.get("UpdateNotice");
			else if (comp < 0) results = "This version is from the future?!";
			else results = Translator.get("UpToDate");

			JOptionPane.showMessageDialog(mainFrame, results);
		} catch (Exception e) {
			if (announceIfFailure) {
				JOptionPane.showMessageDialog(null, Translator.get("UpdateCheckFailed") + e.getLocalizedMessage());
			}
			logger.error("Update check failed", e);
		}
	}

	/**
	 * See <a href="http://www.dreamincode.net/forums/topic/190944-creating-an-updater-in-java/">creating an updater in java</a>
	 *//*
	 * private void downloadUpdate() {
	 *   String[] run = {"java","-jar","updater/update.jar"};
	 *   try {
	 *     Runtime.getRuntime().exec(run);
	 *   } catch (Exception ex) {
	 *     ex.printStackTrace();
	 *   }
	 *   System.exit(0);
	 * }
	 */

	private Container createContentPane() {
		logger.debug("create content pane...");

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);

		logger.debug("  create PreviewPanel...");
		previewPanel = new PreviewPanel();
		previewPanel.setCamera(camera);
		previewPanel.addListener(myPaper);
		previewPanel.addListener(myPlotter);
		previewPanel.addListener(myTurtleRenderer);
		addPlotterRendererToPreviewPanel();

		contentPane.add(previewPanel, BorderLayout.CENTER);
		contentPane.add(rangeSlider, BorderLayout.SOUTH);

		return contentPane;
	}

	//  For thread safety this method should be invoked from the event-dispatching thread.
	private void createAppWindow() {
		logger.debug("Creating GUI...");

		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		mainFrame = new MainFrame("",preferences);
		setMainTitle("");
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setupDropTarget();

		mainFrame.addWindowListener(new WindowAdapter() {
			// when someone tries to close the app, confirm it.
			@Override
			public void windowClosing(WindowEvent e) {
				confirmClose();
				super.windowClosing(e);
			}
		});

		try {
			mainFrame.setIconImage(ImageIO.read(Objects.requireNonNull(Makelangelo.class.getResource("/logo-icon.png"))));
		} catch (IOException e) {
			logger.warn("Can't load icon", e);
		}

		mainFrame.setJMenuBar(mainMenuBar);
		mainFrame.setContentPane(createContentPane());
		logger.debug("  make visible...");
		mainFrame.setVisible(true);
		mainFrame.setWindowSizeAndPosition();

		camera.zoomToFit( Paper.DEFAULT_WIDTH, Paper.DEFAULT_HEIGHT);

		loadPaths();

		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
				desktop.setQuitHandler((evt, res) -> {
					if (confirmClose()) {
						res.performQuit();
					} else {
						res.cancelQuit();
					}
				});
			}
			if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
				desktop.setAboutHandler((e) -> onDialogAbout());
			}
		}
	}
	
	private void setupDropTarget() {
		logger.debug("adding drag & drop support...");
		new DropTarget(mainFrame, new MakelangeloDropTarget(this));
	}

	private boolean confirmClose() {
		int result = JOptionPane.showConfirmDialog(mainFrame, Translator.get("ConfirmQuitQuestion"),
				Translator.get("ConfirmQuitTitle"), JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION) {
			previewPanel.removeListener(myPlotter);
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			myPlotter.getSettings().save();
			plotterSettingsManager.setLastSelectedProfile(myPlotter.getSettings().getUID());
			savePaths();

			// Run this on another thread than the AWT event queue to
			// make sure the call to Animator.stop() completes before
			// exiting
			new Thread(()->{
				previewPanel.stop();
				mainFrame.dispose();
			}).start();
			return true;
		}
		return false;
	}
	
	/**
	 * Use Preferences to store the last "save" dialog path.
	 */
	private void savePaths() {
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
		preferences.put(PREFERENCE_SAVE_PATH, SaveDialog.getLastPath() );
	}

	/**
	 * Use Preferences to recall the last "save" dialog path.
	 */
	private void loadPaths() {
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
		SaveDialog.setLastPath( preferences.get(PREFERENCE_SAVE_PATH, FileAccess.getWorkingDirectory() ) );
	}

	public void setMainTitle(String title) {
		String finalTitle = MakelangeloVersion.getFullOrLiteVersionStringRelativeToSysEnvDevValue();
		if (!title.trim().isEmpty()) {
			finalTitle = title + " - " + finalTitle;
		}
		mainFrame.setTitle(finalTitle);
	}

	public void setTurtle(Turtle turtle) {
		myTurtle = turtle;
		myTurtleRenderer.setTurtle(turtle);
		int top = turtle.history.size();
		rangeSlider.setLimits(0,top);
	}

	public Turtle getTurtle() {
		return myTurtle;
	}

	public static void main(String[] args) {
		Log.start();
		logger = LoggerFactory.getLogger(Makelangelo.class);

		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		if(Translator.isThisTheFirstTimeLoadingLanguageFiles()) {
			LanguagePreferences.chooseLanguage();
		}
		
		setSystemLookAndFeel();

		javax.swing.SwingUtilities.invokeLater(()->{
			Makelangelo makelangeloProgram = new Makelangelo();
			makelangeloProgram.run();
		});
	}

	public void saveGCode() {
		logger.debug("Saving to gcode...");

		SaveGCode save = new SaveGCode();
		try {
			int head = rangeSlider.getValue();
			int tail = rangeSlider.getUpperValue();
			save.run(getTurtle(), getPlotter(), mainFrame, head, tail);
		} catch(Exception e) {
			logger.error("Error while exporting the gcode", e);
			JOptionPane.showMessageDialog(mainFrame, Translator.get("SaveError") + e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public Paper getPaper() {
		return myPaper;
	}

	public Plotter getPlotter() {
		return myPlotter;
	}

	public TurtleRenderer getTurtleRenderer() {
		return myTurtleRenderer.getRenderer();
	}

	public Camera getCamera() {
		return camera;
	}

	public void setTurtleRenderer(TurtleRenderer renderer) {
		myTurtleRenderer.setRenderer(renderer);
	}

	public PlotterSettingsManager getPlotterSettingsManager() {
		return plotterSettingsManager;
	}
}
