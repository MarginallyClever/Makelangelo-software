package com.marginallyclever.makelangelo;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.log.LogPanel;
import com.marginallyclever.makelangelo.firmwareuploader.FirmwareUploaderPanel;
import com.marginallyclever.makelangelo.makeart.TurtleModifierAction;
import com.marginallyclever.makelangelo.makeart.io.LoadFilePanel;
import com.marginallyclever.makelangelo.makeart.io.OpenFileChooser;
import com.marginallyclever.makelangelo.makeart.io.SaveGCode;
import com.marginallyclever.makelangelo.makeart.tools.*;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGeneratorFactory;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGeneratorPanel;
import com.marginallyclever.makelangelo.makelangelosettingspanel.GFXPreferences;
import com.marginallyclever.makelangelo.makelangelosettingspanel.LanguagePreferences;
import com.marginallyclever.makelangelo.makelangelosettingspanel.MakelangeloSettingPanel;
import com.marginallyclever.makelangelo.makelangelosettingspanel.MetricsPreferences;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.paper.PaperSettings;
import com.marginallyclever.makelangelo.plotter.PiCaptureAction;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.marlinsimulation.MarlinSimulation;
import com.marginallyclever.makelangelo.plotter.marlinsimulation.MarlinSimulationVisualizer;
import com.marginallyclever.makelangelo.plotter.plottercontrols.PlotterControls;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.Machines;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRenderer;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettings;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettingsManagerPanel;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettingsUserGcodePanel;
import com.marginallyclever.makelangelo.preview.Camera;
import com.marginallyclever.makelangelo.preview.PreviewPanel;
import com.marginallyclever.makelangelo.rangeslider.RangeSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFacade;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFactory;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderer;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * The Makelangelo app is a tool for programming CNC robots, typically plotters.  It converts lines (made of segments made of points)
 * into instructions in GCODE format, as described in <a href="https://github.com/MarginallyClever/Makelangelo-firmware/wiki/gcode-description">the wiki</a>.
 *
 * In order to do this the app also provides convenient methods to load vectors (like DXF or SVG), create vectors (ImageGenerators), or 
 * interpret bitmaps (like BMP,JPEG,PNG,GIF,TGA,PIO) into vectors (ImageConverters).
 *
 * The app must also know some details about the machine, the surface onto which drawings will be made, and the drawing tool making
 * the mark on the paper.  This knowledge helps the app to create better gcode.  
 *
 * @author Dan Royer (dan@marginallyClever.com)
 * @since 1.00 2012/2/28
 */
public final class Makelangelo {
	private static final String KEY_WINDOW_X = "windowX";
	private static final String KEY_WINDOW_Y = "windowY";
	private static final String KEY_WINDOW_WIDTH = "windowWidth";
	private static final String KEY_WINDOW_HEIGHT = "windowHeight";
	private static final String PREFERENCE_SAVE_PATH = "savePath";
	private static int SHORTCUT_CTRL = InputEvent.CTRL_DOWN_MASK;
	private static int SHORTCUT_ALT = InputEvent.ALT_DOWN_MASK;

	private static Logger logger;

	private final MakelangeloSettingPanel myPreferencesPanel = new MakelangeloSettingPanel();
	
	private final Camera camera;

	private final PlotterSettingsManager plotterSettingsManager = new PlotterSettingsManager();
	private final Plotter myPlotter = new Plotter();
	private final Paper myPaper = new Paper();
	private Turtle myTurtle = new Turtle();
	private static boolean isMacOS = false;

	private final TurtleRenderFacade myTurtleRenderer = new TurtleRenderFacade();
	
	private PlotterRenderer myPlotterRenderer;
	
	// GUI elements
	private JFrame mainFrame;
	private JMenuBar mainMenuBar;
	private PreviewPanel previewPanel;
	private final SaveDialog saveDialog = new SaveDialog();
	
	private RecentFiles recentFiles;
	private OpenFileChooser openFileChooser;

	private RangeSlider rangeSlider;
	private final JLabel labelRangeMin = new JLabel();
	private final JLabel labelRangeMax = new JLabel();

	
	// drag files into the app with {@link DropTarget}
	private DropTarget dropTarget;

	public Makelangelo() {
		logger.debug("Locale={}", Locale.getDefault().toString());
		logger.debug("Headless={}", (GraphicsEnvironment.isHeadless()?"Y":"N"));

		startRobot();
		
		logger.debug("Starting virtual camera...");
		camera = new Camera();
	}
	
	private void startRobot() {
		logger.debug("Starting robot...");

		myPlotter.setSettings(plotterSettingsManager.getLastSelectedProfile());

		if(previewPanel != null) {
			previewPanel.addListener(myPlotter);
			addPlotterRendererToPreviewPanel();
		}

		onPlotterSettingsUpdate(myPlotter.getSettings());
	}

	private void updatePlotterRenderer() {
		try {
			myPlotterRenderer = Machines.valueOf(myPlotter.getSettings().getStyle()).getPlotterRenderer();
		} catch (Exception e) {
			logger.error("Failed to find plotter style {}", myPlotter.getSettings().getStyle());
			myPlotterRenderer = Machines.MAKELANGELO_5.getPlotterRenderer();
		}
	}

	private void onPlotterSettingsUpdate(PlotterSettings e) {
		myPlotter.setSettings(e);
		if(previewPanel != null) previewPanel.repaint();
		TurtleRenderer f = TurtleRenderFactory.MARLIN_SIM.getTurtleRenderer();
		if(f instanceof MarlinSimulationVisualizer) {
			MarlinSimulationVisualizer msv = (MarlinSimulationVisualizer)f;
			msv.setSettings(e);
		}
		myTurtleRenderer.setUpColor(e.getPenUpColor());
		myTurtleRenderer.setPenDiameter(e.getPenDiameter());
		// myTurtleRenderer.setDownColor() would be meaningless, the down color is stored in each Turtle.
		updatePlotterRenderer();
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
		setSystemLookAndFeelForMacos();
	}
	
	private static void setSystemLookAndFeelForMacos() {
		String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		if ((os.contains("mac")) || (os.contains("darwin"))) {
			isMacOS = true;
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			SHORTCUT_CTRL = InputEvent.META_DOWN_MASK;
			SHORTCUT_ALT = InputEvent.META_DOWN_MASK;
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
			int dialogResult = JOptionPane.showConfirmDialog(mainFrame, Translator.get("collectAnonymousMetricsOnUpdate"),"Sharing Is Caring",JOptionPane.YES_NO_OPTION);
			MetricsPreferences.setAllowedToShare(dialogResult == JOptionPane.YES_OPTION);
		}
	}

	/**
	 * Build the main menu
	 */
	private void buildMenuBar() {
		logger.debug("  adding menu bar...");

		mainMenuBar = new JMenuBar();
		mainMenuBar.add(createFileMenu());
		mainMenuBar.add(createSettingsMenu());
		mainMenuBar.add(createGenerateMenu());
		mainMenuBar.add(createToolsMenu());
		mainMenuBar.add(createViewMenu());
		mainMenuBar.add(createRobotMenu());
		mainMenuBar.add(createHelpMenu());
		mainMenuBar.updateUI();
		
		mainFrame.setJMenuBar(mainMenuBar);
	}

	/**
	 * Change the enable state of the menu items inside the {@code mainMenuBar}.
	 * Remember that enabling the menuBar does not affect the children.
	 * @param b the new state
	 */
	private void enableMenuBar(boolean b) {
		int c = mainMenuBar.getMenuCount();
		while(--c>=0) {
			mainMenuBar.getMenu(c).setEnabled(b);
		}
	}

	private JMenu createSettingsMenu() {
		JMenu menu = new JMenu(Translator.get("MenuSettings"));
		menu.setMnemonic('S');
		
		JMenuItem bOpenPaperSettings = new JMenuItem(Translator.get("OpenPaperSettings"));
		bOpenPaperSettings.addActionListener((e)-> openPaperSettings());
		menu.add(bOpenPaperSettings);

		JMenuItem bOpenPlotterSettings = new JMenuItem(Translator.get("OpenPlotterSettings"));
		bOpenPlotterSettings.addActionListener((e)-> openPlotterSettings());
		bOpenPlotterSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, SHORTCUT_CTRL));//"ctrl P"
		menu.add(bOpenPlotterSettings);

		return menu;
	}

	private void openPlotterSettings() {
		PlotterSettingsManagerPanel plotterSettingsPanel = new PlotterSettingsManagerPanel(plotterSettingsManager);
		JDialog dialog = new JDialog(mainFrame,Translator.get("PlotterSettingsPanel.Title"));
		dialog.add(plotterSettingsPanel);
		dialog.setMinimumSize(new Dimension(350,300));
		dialog.setResizable(false);
		dialog.pack();

		enableMenuBar(false);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onPlotterSettingsUpdate(plotterSettingsManager.getLastSelectedProfile());
				enableMenuBar(true);
			}
		});

		dialog.setLocationRelativeTo(mainFrame);
		dialog.setVisible(true);
	}

	private void openPaperSettings() {
		PaperSettings settings = new PaperSettings(myPaper);
		JDialog dialog = new JDialog(mainFrame,Translator.get("PaperSettings.Title"));
		dialog.add(settings);
		dialog.setMinimumSize(new Dimension(300,300));
		dialog.pack();

		enableMenuBar(false);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				enableMenuBar(true);
				settings.save();
			}
		});

		dialog.setLocationRelativeTo(mainFrame);
		dialog.setVisible(true);
	}

	private JMenu createRobotMenu() {
		JMenu menu = new JMenu(Translator.get("Robot"));
		menu.setMnemonic('k');
		
		JMenuItem bEstimate = new JMenuItem(Translator.get("RobotMenu.GetTimeEstimate"));
		bEstimate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, SHORTCUT_CTRL));//"ctrl E"
		bEstimate.addActionListener((e)-> estimateTime());
		menu.add(bEstimate);

		JMenuItem bSaveToSD = new JMenuItem(Translator.get("RobotMenu.SaveGCode"));
		bSaveToSD.addActionListener((e)-> saveGCode());
		bSaveToSD.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, SHORTCUT_CTRL));//"ctrl G"
		menu.add(bSaveToSD);

		JMenuItem bOpenControls = new JMenuItem(Translator.get("RobotMenu.OpenControls"));
		bOpenControls.addActionListener((e)-> openPlotterControls());
		bOpenControls.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, SHORTCUT_CTRL));//"ctrl C"
		bOpenControls.setIcon(new UnicodeIcon("\uD83D\uDD79"));
		menu.add(bOpenControls);

		return menu;
	}

	private JMenuItem createRenderStyleMenu() {
		JMenu menu = new JMenu(Translator.get("RobotMenu.RenderStyle"));

		ButtonGroup group = new ButtonGroup();

		Arrays.stream(TurtleRenderFactory.values())
				.forEach(iter -> {
					TurtleRenderer renderer = iter.getTurtleRenderer();
					String name = iter.getName();
					JRadioButtonMenuItem button = new JRadioButtonMenuItem(iter.getTranslatedText());
					if (myTurtleRenderer.getRenderer() == renderer) button.setSelected(true);
					button.addActionListener((e)-> onTurtleRenderChange(name));
					menu.add(button);
					group.add(button);
				});

		return menu;
	}

	private void onTurtleRenderChange(String name) {
		logger.debug("Switching to render style '{}'", name);
		TurtleRenderer renderer = TurtleRenderFactory.findByName(name).getTurtleRenderer();
		myTurtleRenderer.setRenderer(renderer);
	}

	private void saveGCode() {
		logger.debug("Saving to gcode...");

		SaveGCode save = new SaveGCode();
		try {
			int head = rangeSlider.getValue();
			int tail = rangeSlider.getUpperValue();
			save.run(myTurtle, myPlotter, mainFrame, head, tail);
		} catch(Exception e) {
			logger.error("Error while exporting the gcode", e);
			JOptionPane.showMessageDialog(mainFrame, Translator.get("SaveError") + e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private void estimateTime() {
		MarlinSimulation ms = new MarlinSimulation(myPlotter.getSettings());
		int estimatedSeconds = (int)Math.ceil(ms.getTimeEstimate(myTurtle));
		String timeAsString = StringHelper.getElapsedTime(estimatedSeconds);
		String message = Translator.get("EstimatedTimeIs",new String[]{timeAsString});
		JOptionPane.showMessageDialog(mainFrame, message, Translator.get("RobotMenu.GetTimeEstimate"), JOptionPane.INFORMATION_MESSAGE);
	}

	private void openPlotterControls() {
		JDialog dialog = new JDialog(mainFrame, Translator.get("PlotterControls.Title"));
		dialog.setPreferredSize(new Dimension(PlotterControls.DIMENSION_PANEL_WIDTH, PlotterControls.DIMENSION_PANEL_HEIGHT));
		dialog.setMinimumSize(new Dimension(PlotterControls.DIMENSION_PANEL_WIDTH, PlotterControls.DIMENSION_PANEL_HEIGHT));
		PlotterControls plotterControls = new PlotterControls(myPlotter,myTurtle, dialog);
		dialog.add(plotterControls);
		dialog.pack();

		enableMenuBar(false);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				plotterControls.onDialogClosing();
				enableMenuBar(true);
			}
		});

		dialog.setLocationRelativeTo(mainFrame);
		dialog.setVisible(true);
	}

	private JMenu createToolsMenu() {
		JMenu menu = new JMenu(Translator.get("Art Pipeline"));
		menu.setMnemonic('T');

		try {
			PiCaptureAction pc = new PiCaptureAction();

			JButton bCapture = new JButton(Translator.get("MenuCaptureImage"));
			bCapture.addActionListener((e)-> pc.run(mainFrame,myPaper));
			bCapture.setIcon(new UnicodeIcon("📷"));
			menu.add(bCapture);
			menu.addSeparator();
		} catch (FailedToRunRaspistillException e) {
			logger.debug("Raspistill unavailable.");
		}

		TurtleModifierAction a6 = new ResizeTurtleToPaperAction(myPaper,false,Translator.get("ConvertImagePaperFit"));
		TurtleModifierAction a7 = new ResizeTurtleToPaperAction(myPaper,true,Translator.get("ConvertImagePaperFill"));
		a6.setSource(this);		a6.addModifierListener(this::setTurtle);		menu.add(a6);
		a7.setSource(this);		a7.addModifierListener(this::setTurtle);		menu.add(a7);
		
		JMenuItem scale = new JMenuItem(Translator.get("Scale"));
		menu.add(scale);
		scale.addActionListener((e) -> runScalePanel());

		JMenuItem rotate = new JMenuItem(Translator.get("Rotate"));
		rotate.setIcon(new UnicodeIcon("↻"));
		menu.add(rotate);
		rotate.addActionListener((e) -> runRotatePanel());
		menu.addSeparator();
		
		TurtleModifierAction a4 = new FlipTurtleAction(1,-1,Translator.get("FlipH"));
		a4.putValue(Action.SMALL_ICON, new UnicodeIcon("↕"));
		a4.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, SHORTCUT_CTRL));//"ctrl H"
		TurtleModifierAction a5 = new FlipTurtleAction(-1,1,Translator.get("FlipV"));
		a5.putValue(Action.SMALL_ICON, new UnicodeIcon("↔"));
		a5.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, SHORTCUT_CTRL));//"ctrl F"
		a4.setSource(this);		a4.addModifierListener(this::setTurtle);		menu.add(a4);
		a5.setSource(this);		a5.addModifierListener(this::setTurtle);		menu.add(a5);
		
		menu.addSeparator();
		
		TurtleModifierAction a1 = new SimplifyTurtleAction();
		a1.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, SHORTCUT_CTRL));//"ctrl Y"
		TurtleModifierAction a2 = new ReorderTurtleAction();
		a2.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, SHORTCUT_CTRL));//"ctrl R"
		TurtleModifierAction a3 = new InfillTurtleAction();
		a3.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, SHORTCUT_CTRL));//"ctrl I"
		a1.setSource(this);		a1.addModifierListener(this::setTurtle);		menu.add(a1);
		a2.setSource(this);		a2.addModifierListener(this::setTurtle);		menu.add(a2);
		a3.setSource(this);		a3.addModifierListener(this::setTurtle);		menu.add(a3);

		return menu;
	}

	private void runRotatePanel() {
		RotateTurtlePanel.runAsDialog(mainFrame, myTurtle);
	}
	private void runScalePanel() {
		ScaleTurtlePanel.runAsDialog(mainFrame, myTurtle);
	}

	private JMenu createGenerateMenu() {
		JMenu menu = new JMenu(Translator.get("MenuGenerate"));
		menu.setMnemonic('A');
		for( TurtleGenerator ici : TurtleGeneratorFactory.available ) {
			JMenuItem mi = new JMenuItem(ici.getName());
			mi.addActionListener((e) -> runGeneratorDialog(ici));
			menu.add(mi);
		}
		
		return menu;
	}
	
	private void runGeneratorDialog(TurtleGenerator turtleGenerator) {
		turtleGenerator.setPaper(myPaper);
		turtleGenerator.addListener(this::setTurtle);
		turtleGenerator.generate();

		if(turtleGenerator.getPanelElements().isEmpty()) {
			return;
		}

		setMainTitle(turtleGenerator.getName());
		JDialog dialog = new JDialog(mainFrame, turtleGenerator.getName());
		TurtleGeneratorPanel panel = new TurtleGeneratorPanel(turtleGenerator);
		dialog.add(panel);
		dialog.setLocationRelativeTo(mainFrame);
		dialog.setMinimumSize(new Dimension(300,300));
		dialog.pack();


		enableMenuBar(false);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				enableMenuBar(true);
				myPaper.setRotationRef(0);
				logger.debug("Generation finished");
			}
		});

		dialog.setVisible(true);
	}
	
	private void newFile() {
		setTurtle(new Turtle());
		setMainTitle("");
	}

	private JMenu createFileMenu() {
		JMenu menu = new JMenu(Translator.get("MenuMakelangelo"));
		menu.setMnemonic('f');

		JMenuItem buttonNewFile = new JMenuItem(Translator.get("MenuNewFile"));
		buttonNewFile.addActionListener((e) -> newFile());
		buttonNewFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, SHORTCUT_CTRL));//"ctrl N"
		buttonNewFile.setIcon(new UnicodeIcon("\uD83C\uDF31"));
		menu.add(buttonNewFile);

		JMenuItem buttonOpenFile = new JMenuItem(Translator.get("MenuOpenFile"));
		buttonOpenFile.addActionListener((e) -> openLoadFile());
		buttonOpenFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, SHORTCUT_CTRL));//"ctrl O"
		buttonOpenFile.setIcon(new UnicodeIcon("\uD83D\uDDC1"));
		menu.add(buttonOpenFile);
		
		recentFiles = new RecentFiles(Translator.get("MenuReopenFile"));
		recentFiles.addSubmenuListener((e)-> openFile(((JMenuItem)e.getSource()).getText()));
		menu.add(recentFiles);
		
		JMenuItem buttonSaveFile = new JMenuItem(Translator.get("MenuSaveFile"));
		buttonSaveFile.addActionListener((e) -> saveFile());
		buttonSaveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, SHORTCUT_CTRL));//"ctrl S"
		buttonSaveFile.setIcon(new UnicodeIcon("\uD83D\uDCBE"));
		menu.add(buttonSaveFile);

		menu.addSeparator();
				
		JMenuItem buttonAdjustPreferences = new JMenuItem(Translator.get("MenuPreferences"));
		buttonAdjustPreferences.addActionListener((e)-> myPreferencesPanel.run(mainFrame));
		if (isMacOS) {
			buttonAdjustPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, SHORTCUT_CTRL));//"cmd ,"
		} else {
			buttonAdjustPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, SHORTCUT_ALT));//"alt P"
		}
		buttonAdjustPreferences.setIcon(new UnicodeIcon("⚙"));
		menu.add(buttonAdjustPreferences);

		JMenuItem buttonFirmwareUpdate = new JMenuItem(Translator.get("FirmwareUpdate"));
		buttonFirmwareUpdate.addActionListener((e) -> runFirmwareUpdate());
		buttonFirmwareUpdate.setIcon(new UnicodeIcon("⬆"));
		menu.add(buttonFirmwareUpdate);

		if (!isMacOS) {
			menu.addSeparator();

			JMenuItem buttonExit = new JMenuItem(Translator.get("MenuQuit"));
			buttonExit.addActionListener((e) -> {
				WindowEvent windowClosing = new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING);
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(windowClosing);
			});
			buttonExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, SHORTCUT_CTRL));//"ctrl Q"
			buttonExit.setMnemonic('q');
			menu.add(buttonExit);
		}
		return menu;
	}

	public void openLoadFile() {
		logger.debug("Open file...");
		openFileChooser.chooseFile();
	}

	public void openFile(String filename) {
		try {
			LoadFilePanel loader = new LoadFilePanel(myPaper,filename);
			loader.addActionListener((e)-> setTurtle((Turtle)(e).getSource()));

			if(filename == null || filename.trim().isEmpty()) throw new InvalidParameterException("filename cannot be empty");

			if (loader.load(filename)) {
				previewPanel.addListener(loader);
				JDialog dialog = new JDialog(mainFrame, Translator.get("LoadFilePanel.title"));
				dialog.add(loader);
				dialog.setLocationRelativeTo(mainFrame);
				dialog.setMinimumSize(new Dimension(500,500));
				dialog.pack();
				loader.setParent(dialog);

				enableMenuBar(false);
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						loader.loadingFinished();
						enableMenuBar(true);
						previewPanel.removeListener(loader);
						recentFiles.addFilename(filename);
					}
				});

				dialog.setVisible(true);
			} else {
				recentFiles.addFilename(filename);
			}

			setMainTitle(new File(filename).getName());

		} catch(Exception e) {
			logger.error("Error while loading the file {}", filename, e);
			JOptionPane.showMessageDialog(mainFrame, Translator.get("LoadError") + e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
			recentFiles.removeFilename(filename);
		}
	}

	private void runFirmwareUpdate() {
		JDialog dialog = new JDialog(mainFrame,"Firmware Update");
		dialog.add(new FirmwareUploaderPanel());
		dialog.pack();
		dialog.setLocationRelativeTo(mainFrame);

		enableMenuBar(false);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				enableMenuBar(true);
			}
		});

		dialog.setVisible(true);
	}

	private JMenu createViewMenu() {
		JMenu menu = new JMenu(Translator.get("MenuView"));
		menu.setMnemonic('V');
		
		JMenuItem buttonZoomOut = new JMenuItem(Translator.get("MenuView.zoomOut"), KeyEvent.VK_MINUS);
		buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, SHORTCUT_CTRL));
		buttonZoomOut.addActionListener((e) -> camera.zoom(1));
		buttonZoomOut.setIcon(new UnicodeIcon("\uD83D\uDD0D-"));
		menu.add(buttonZoomOut);

		JMenuItem buttonZoomIn = new JMenuItem(Translator.get("MenuView.zoomIn"), KeyEvent.VK_EQUALS);
		buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, SHORTCUT_CTRL));
		buttonZoomIn.addActionListener((e) -> camera.zoom(-1));
		buttonZoomIn.setIcon(new UnicodeIcon("\uD83D\uDD0D+"));
		menu.add(buttonZoomIn);
		
		JMenuItem buttonZoomToFit = new JMenuItem(Translator.get("MenuView.zoomFit"), KeyEvent.VK_0);
		buttonZoomToFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, SHORTCUT_CTRL));
		buttonZoomToFit.addActionListener((e) -> camera.zoomToFit(myPaper.getPaperWidth(),myPaper.getPaperHeight()));
		menu.add(buttonZoomToFit);

		JCheckBoxMenuItem checkboxShowPenUpMoves = new JCheckBoxMenuItem(Translator.get("GFXPreferences.showPenUp"),GFXPreferences.getShowPenUp());
		checkboxShowPenUpMoves.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, SHORTCUT_CTRL));//"ctrl M" 
		checkboxShowPenUpMoves.addActionListener((e) -> {
			boolean b = GFXPreferences.getShowPenUp();
			GFXPreferences.setShowPenUp(!b);
		});
		GFXPreferences.addListener((e)->{
			checkboxShowPenUpMoves.setSelected ((boolean)e.getNewValue());
		});
		checkboxShowPenUpMoves.setIcon(new UnicodeIcon("\uD83D\uDC41"));
		menu.add(checkboxShowPenUpMoves);

		menu.add(createRenderStyleMenu());

		return menu;
	}

	private JMenu createHelpMenu() {
		JMenu menu = new JMenu(Translator.get("Help"));
		menu.setMnemonic('H');//
		
		JMenuItem buttonViewLog = new JMenuItem(Translator.get("ShowLog"));
		buttonViewLog.addActionListener((e) -> runLogPanel());		 
		buttonViewLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, SHORTCUT_CTRL));//"ctrl L"
		menu.add(buttonViewLog);
		
		JMenuItem buttonForums = createMenuItemBrowse(Translator.get("MenuForums"), "https://discord.gg/Q5TZFmB");
		menu.add(buttonForums);

		JMenuItem buttonDonation = createMenuItemBrowse(Translator.get("MenuItemPayPalDonation"), "https://www.marginallyclever.com/products/makelangelo-software/");
		menu.add(buttonDonation);
		
		JMenuItem buttonCheckForUpdate = new JMenuItem(Translator.get("MenuUpdate"));
		buttonCheckForUpdate.addActionListener((e) -> checkForUpdate(false));
		menu.add(buttonCheckForUpdate);

		menu.addSeparator();
		
		if (!isMacOS) {
			JMenuItem buttonAbout = new JMenuItem(Translator.get("MenuAbout"));
			buttonAbout.addActionListener((e) -> onDialogButton());
			menu.add(buttonAbout);
		}

		
		return menu;
	}

	public JMenuItem createMenuItemBrowse(String menuLabelAlreadyTranslated, String urlAsString) {
		JMenuItem jmi = new JMenuItem(menuLabelAlreadyTranslated);
		jmi.setToolTipText(urlAsString);
		jmi.addActionListener((e) -> {
			try {
				if (Desktop.isDesktopSupported()) {
					if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
						java.awt.Desktop.getDesktop().browse(URI.create(urlAsString));
					} else {
						logger.error("Desktop.Action.BROWSE not supported. Can't open the browser to " + urlAsString);
					}
				} else {
					logger.error("Desktop not supported. Can't open the browser to " + urlAsString);
				}
				
			} catch (IOException ioe) {
				logger.error("Can't open the browser to "+urlAsString, ioe);
			}
		});
		return jmi;
	}

	private void onDialogButton() {
		DialogAbout a = new DialogAbout();
		a.display(mainFrame,MakelangeloVersion.VERSION, MakelangeloVersion.DETAILED_VERSION);
	}

	private void runLogPanel() {
		LogPanel.runAsDialog(mainFrame);
	}

	/**
	 * Parse <a href="https://github.com/MarginallyClever/Makelangelo/releases/latest">https://github.com/MarginallyClever/Makelangelo/releases/latest</a>
	 * redirect notice to find the latest release tag.
	 */
	private void checkForUpdate(boolean announceIfFailure) {
		logger.debug("checking for updates...");
		try {
			URL github = new URL("https://github.com/MarginallyClever/Makelangelo-Software/releases/latest");
			HttpURLConnection conn = (HttpURLConnection) github.openConnection();
			conn.setInstanceFollowRedirects(false); // you still need to handle
													// redirect manully.
			HttpURLConnection.setFollowRedirects(false);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String inputLine = in.readLine();
			if(inputLine == null) throw new Exception("Could not read from update server.");

			// parse the URL in the text-only redirect
			String matchStart = "<a href=\"";
			String matchEnd = "\">";
			int start = inputLine.indexOf(matchStart);
			int end = inputLine.indexOf(matchEnd);
			if (start != -1 && end != -1) {
				String line2 = inputLine.substring(start + matchStart.length(), end);
				// parse the last part of the redirect URL, which contains the
				// release tag (which is the VERSION)
				line2 = line2.substring(line2.lastIndexOf("/") + 1);

				logger.debug("latest release: {}; this version: {}@{}", line2, MakelangeloVersion.VERSION, MakelangeloVersion.DETAILED_VERSION);
				// logger.debug(inputLine.compareTo(MakelangeloVersion.VERSION));

				int comp = line2.compareTo(MakelangeloVersion.VERSION);
				String results;
				if (comp > 0) results = Translator.get("UpdateNotice");
				else if (comp < 0) results = "This version is from the future?!";
				else results = Translator.get("UpToDate");

				JOptionPane.showMessageDialog(mainFrame, results);
			}
			in.close();
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
		
		createRangeSlider(contentPane);
		
		contentPane.add(previewPanel, BorderLayout.CENTER);

		return contentPane;
	}

	/**
	 * Build and lay out the bottom-most components of the main view: 
	 * the two-headed range slider and the numbers that show the head
	 * values.
	 * @param contentPane where to attach the new elements.
	 */
	private void createRangeSlider(JPanel contentPane) {
		logger.debug("  create range slider...");
		JPanel bottomPanel = new JPanel(new BorderLayout());
		rangeSlider = new RangeSlider();
		rangeSlider.addChangeListener(this::onChangeSlider);
		
		labelRangeMax.setHorizontalAlignment(SwingConstants.RIGHT);
		
		labelRangeMin.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		labelRangeMax.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		labelRangeMin.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		labelRangeMax.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		
		Dimension d = labelRangeMin.getPreferredSize();
		d.width=60;
		labelRangeMin.setPreferredSize(d);
		labelRangeMax.setPreferredSize(d);
		
		bottomPanel.add(labelRangeMin, BorderLayout.WEST);
		bottomPanel.add(rangeSlider, BorderLayout.CENTER);
		bottomPanel.add(labelRangeMax, BorderLayout.EAST);
		
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * When the two-headed drawing start/end slider is moved
	 * @param e {@link ChangeEvent} describing the move.
	 */
	private void onChangeSlider(ChangeEvent e) {
        RangeSlider slider = (RangeSlider)e.getSource();
        int bottom = slider.getValue();
        int top = slider.getUpperValue();
        myTurtleRenderer.setFirst(bottom);
        myTurtleRenderer.setLast(top);
        labelRangeMin.setText(Integer.toString(bottom));
        labelRangeMax.setText(Integer.toString(top));
	}

	//  For thread safety this method should be invoked from the event-dispatching thread.
	private void createAppWindow() {
		logger.debug("Creating GUI...");

		mainFrame = new JFrame();
		setMainTitle("");
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClosing();
			}
		});
		mainFrame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				saveWindowSizeAndPosition();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				saveWindowSizeAndPosition();
			}
		});

		try {
			mainFrame.setIconImage(ImageIO.read(Objects.requireNonNull(Makelangelo.class.getResource("/logo-icon.png"))));
		} catch (IOException e) {
			logger.warn("Can't load icon", e);
		}
		buildMenuBar();
		
		mainFrame.setContentPane(createContentPane());

		camera.zoomToFit( Paper.DEFAULT_WIDTH, Paper.DEFAULT_HEIGHT);
		
		logger.debug("  make visible...");
		mainFrame.setVisible(true);
		
		setWindowSizeAndPosition();

		setupDropTarget();

		openFileChooser = new OpenFileChooser(mainFrame);
		openFileChooser.setOpenListener(this::openFile);

		loadPaths();

		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
				desktop.setQuitHandler((evt, res) -> {
					if (onClosing()) {
						res.performQuit();
					} else {
						res.cancelQuit();
					}
				});
			}
			if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
				desktop.setAboutHandler((e) -> onDialogButton());
			}
		}
	}
	
	private void setupDropTarget() {
		logger.debug("adding drag & drop support...");
		dropTarget = new DropTarget(mainFrame,new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
			try {
				Transferable tr = dtde.getTransferable();
				DataFlavor[] flavors = tr.getTransferDataFlavors();
				for (DataFlavor flavor : flavors) {
					logger.debug("Possible flavor: {}", flavor.getMimeType());
					if (flavor.isFlavorJavaFileListType()) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						Object o = tr.getTransferData(flavor);
						if (o instanceof List<?>) {
							List<?> list = (List<?>) o;
							if (list.size() > 0) {
								o = list.get(0);
								if (o instanceof File) {
									openFile(((File) o).getAbsolutePath());
									dtde.dropComplete(true);
									return;
								}
							}
						}
					}
				}
				logger.debug("Drop failed: {}", dtde);
				dtde.rejectDrop();
			} catch (Exception e) {
				logger.error("Drop error", e);
				dtde.rejectDrop();
			}
			}
		});
	}

	private void setWindowSizeAndPosition() {
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

		// Get default screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// window size
		int width = preferences.getInt(KEY_WINDOW_WIDTH, Math.min(screenSize.width,1200));
		int height = preferences.getInt(KEY_WINDOW_HEIGHT, Math.min(screenSize.height,1020));
    	int windowX = preferences.getInt(KEY_WINDOW_X, (screenSize.width - width)/2);
    	int windowY = preferences.getInt(KEY_WINDOW_Y, (screenSize.height - height)/2);
		mainFrame.setBounds( windowX, windowY, width, height);

		boolean isFullscreen = preferences.getBoolean("isFullscreen",false);
		if(isFullscreen) {
			mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
	}

	// save window position and size
	private void saveWindowSizeAndPosition() {
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

		int state = mainFrame.getExtendedState();
		boolean isFullscreen = ((state & JFrame.MAXIMIZED_BOTH)!=0);
		preferences.putBoolean("isFullscreen", isFullscreen);
		if(!isFullscreen) {
			Dimension size = this.mainFrame.getSize();
			preferences.putInt(KEY_WINDOW_WIDTH, size.width);
			preferences.putInt(KEY_WINDOW_HEIGHT, size.height);

			Point location = this.mainFrame.getLocation();
			preferences.putInt(KEY_WINDOW_X, location.x);
			preferences.putInt(KEY_WINDOW_Y, location.y);
		}
	}

	private boolean onClosing() {
		int result = JOptionPane.showConfirmDialog(mainFrame, Translator.get("ConfirmQuitQuestion"),
				Translator.get("ConfirmQuitTitle"), JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION) {
			previewPanel.removeListener(myPlotter);
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			myPlotter.getSettings().saveConfig();
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

	private void saveFile() {
		logger.debug("Saving vector file...");
		try {
			saveDialog.run(myTurtle, mainFrame);
		} catch(Exception e) {
			logger.error("Error while saving the vector file", e);
			JOptionPane.showMessageDialog(mainFrame, Translator.get("SaveError") + e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private void setMainTitle(String title) {
		String finalTitle = "";
		if (! "".equals(title)) {
			finalTitle += title + " - ";
		}
		finalTitle += MakelangeloVersion.getFullOrLiteVersionStringRelativeToSysEnvDevValue();
		mainFrame.setTitle(finalTitle);
	}

	public void setTurtle(Turtle turtle) {
		myTurtle = turtle;
		myTurtleRenderer.setTurtle(turtle);
		int top = turtle.history.size();
		rangeSlider.setMinimum(0);
		rangeSlider.setValue(0);
		rangeSlider.setMaximum(top);
		rangeSlider.setUpperValue(top);
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
}
