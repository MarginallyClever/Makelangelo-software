package com.marginallyclever.makelangelo;
/**
 * @(#)Makelangelo.java drawbot application with GUI
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */

import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.log.LogPanel;
import com.marginallyclever.makelangelo.firmwareUploader.FirmwareUploaderPanel;
import com.marginallyclever.makelangelo.machines.Machines;
import com.marginallyclever.makelangelo.makeArt.InfillTurtleAction;
import com.marginallyclever.makelangelo.makeArt.ReorderTurtle;
import com.marginallyclever.makelangelo.makeArt.ResizeTurtleToPaper;
import com.marginallyclever.makelangelo.makeArt.SimplifyTurtle;
import com.marginallyclever.makelangelo.makeArt.io.LoadFilePanel;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGeneratorFactory;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGeneratorPanel;
import com.marginallyclever.makelangelo.makelangeloSettingsPanel.GFXPreferences;
import com.marginallyclever.makelangelo.makelangeloSettingsPanel.LanguagePreferences;
import com.marginallyclever.makelangelo.makelangeloSettingsPanel.MakelangeloSettingPanel;
import com.marginallyclever.makelangelo.makelangeloSettingsPanel.MetricsPreferences;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.paper.PaperSettings;
import com.marginallyclever.makelangelo.plotter.PiCaptureAction;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterEvent;
import com.marginallyclever.makelangelo.plotter.marlinSimulation.MarlinSimulation;
import com.marginallyclever.makelangelo.plotter.plotterControls.PlotterControls;
import com.marginallyclever.makelangelo.plotter.plotterControls.SaveGCode;
import com.marginallyclever.makelangelo.plotter.plotterRenderer.PlotterRenderer;
import com.marginallyclever.makelangelo.preview.Camera;
import com.marginallyclever.makelangelo.preview.PreviewPanel;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleRenderFacade;
import com.marginallyclever.util.PreferencesHelper;
import com.marginallyclever.util.PropertiesFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * The Makelangelo app is a tool for programming CNC robots, typically plotters.  It converts lines (made of segments made of points)
 * into instructions in GCODE format, as described in https://github.com/MarginallyClever/Makelangelo-firmware/wiki/gcode-description.
 * 
 * In order to do this the app also provides convenient methods to load vectors (like DXF or SVG), create vectors (ImageGenerators), or 
 * interpret bitmaps (like BMP,JPEG,PNG,GIF,TGA) into vectors (ImageConverters).
 * 
 * The app must also know some details about the machine, the surface onto which drawings will be made, and the drawing tool making
 * the mark on the paper.  This knowledge helps the app to create better gcode.  
 * 
 * @author Dan Royer
 * @since 0.0.1
 */
public final class Makelangelo {
	private static final String KEY_WINDOW_X = "windowX";
	private static final String KEY_WINDOW_Y = "windowY";
	private static final String KEY_WINDOW_WIDTH = "windowWidth";
	private static final String KEY_WINDOW_HEIGHT = "windowHeight";
	private static final String KEY_MACHINE_STYLE = "machineStyle";

	private static Logger logger;

	/**
	 * Defined in src/resources/makelangelo.properties and uses Maven's resource filtering to update the 
	 * VERSION based upon pom.xml.  In this way we only define the VERSION once and prevent violating DRY.
	 */
	public String VERSION;

	private MakelangeloSettingPanel myPreferencesPanel;
	
	private Camera camera;
	private Plotter myPlotter;
	private Paper myPaper = new Paper();
	private Turtle myTurtle = new Turtle();
	private static boolean isMacOS = false;

	private TurtleRenderFacade myTurtleRenderer = new TurtleRenderFacade();
	private PlotterRenderer myPlotterRenderer;
	
	// GUI elements
	private JFrame mainFrame;
	private JMenuBar mainMenuBar;
	private PreviewPanel previewPanel;
	private SaveDialog saveDialog;
	
	private RecentFiles recentFiles;

	// drag files into the app with {@link DropTarget}
	@SuppressWarnings("unused")
	private DropTarget dropTarget;

	public Makelangelo() {
		logger.debug("Locale={}", Locale.getDefault().toString());
		logger.debug("Headless={}", (GraphicsEnvironment.isHeadless()?"Y":"N"));
		VERSION = PropertiesFileHelper.getMakelangeloVersionPropertyValue();
		myPreferencesPanel = new MakelangeloSettingPanel();

		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		String machineStyle = preferences.get(KEY_MACHINE_STYLE, Machines.MAKELANGELO_5.getName());
		logger.debug("machine style: {}", machineStyle);

		try {
			myPlotterRenderer = Machines.valueOf(machineStyle).getPlotterRenderer();
		} catch (Exception e) {
			myPlotterRenderer = Machines.MAKELANGELO_5.getPlotterRenderer();
		}

		startRobot();

		saveDialog = new SaveDialog();
		
		logger.debug("Starting virtual camera...");
		camera = new Camera();
	}
	
	private void startRobot() {
		logger.debug("Starting robot...");
		myPlotter = new Plotter();
		myPlotter.addPlotterEventListener(this::onPlotterEvent);
		myPlotter.getSettings().addPlotterSettingsListener((e)->{
			if(previewPanel != null) previewPanel.repaint();
		});
		if(previewPanel != null) {
			previewPanel.addListener(myPlotter);
			addPlotterRendererToPreviewPanel();
		}
	}

	private void addPlotterRendererToPreviewPanel() {
		previewPanel.addListener((gl2)->{
			if(myPlotterRenderer!=null) {
				myPlotterRenderer.render(gl2, myPlotter);
			}
		});
	}

	private void onPlotterEvent(PlotterEvent e) {
		if(e.type==PlotterEvent.TOOL_CHANGE) {
			requestUserChangeTool((int)e.extra);
		}
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
	        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        } catch (Exception e) {}
		}
		setSystemLookAndFeelForMacos();
	}
	
	private static void setSystemLookAndFeelForMacos() {
		String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		if ((os.contains("mac")) || (os.contains("darwin"))) {
			isMacOS = true;
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		}
	}

	// check if we need to ask about sharing
	@SuppressWarnings("unused")
	private void checkSharingPermission() {
		logger.debug("Checking sharing permissions...");
		
		final String SHARING_CHECK_STRING = "Last version sharing checked";
		
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		String v = preferences.get(SHARING_CHECK_STRING,"0");
		int comparison = VERSION.compareTo(v);
		if(comparison!=0) {
			preferences.put(SHARING_CHECK_STRING,VERSION);
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
		mainMenuBar.add(createPaperSettingsMenu());
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

	private JMenu createPaperSettingsMenu() {
		JMenu menu = new JMenu(Translator.get("MenuPaper"));
		
		JMenuItem bOpenControls = new JMenuItem(Translator.get("OpenPaperSettings"));
		bOpenControls.addActionListener((e)-> openPaperSettings());
		menu.add(bOpenControls);
		
		return menu;
	}

	private void openPaperSettings() {
		PaperSettings paperSettings = new PaperSettings(myPaper);
		JDialog dialog = new JDialog(mainFrame,PaperSettings.class.getSimpleName());
		dialog.add(paperSettings);
		dialog.setLocationRelativeTo(mainFrame);
		dialog.setMinimumSize(new Dimension(300,300));
		dialog.pack();

		enableMenuBar(false);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				enableMenuBar(true);
				paperSettings.save();
			}
		});

		dialog.setVisible(true);
	}

	private JMenu createRobotMenu() {
		JMenu menu = new JMenu(Translator.get("Robot"));

		menu.add(createStyleMenu());
		
		JMenuItem bEstimate = new JMenuItem(Translator.get("RobotMenu.GetTimeEstimate"));
		bEstimate.addActionListener((e)-> estimateTime());
		menu.add(bEstimate);

		JMenuItem bSaveToSD = new JMenuItem(Translator.get("RobotMenu.SaveGCode"));
		bSaveToSD.addActionListener((e)-> saveGCode());
		menu.add(bSaveToSD);

		JMenuItem bOpenControls = new JMenuItem(Translator.get("RobotMenu.OpenControls"));
		bOpenControls.addActionListener((e)-> openPlotterControls());
		menu.add(bOpenControls);

		return menu;
	}

	private JMenuItem createStyleMenu() {
		JMenu menu = new JMenu(Translator.get("RobotMenu.Style"));
		
		ButtonGroup group = new ButtonGroup();

		Arrays.stream(Machines.values())
				.forEach(it -> {
					PlotterRenderer pr = it.getPlotterRenderer();
					String name = it.getName();
					JRadioButtonMenuItem button = new JRadioButtonMenuItem(name);
					if (myPlotterRenderer == pr) button.setSelected(true);
					button.addActionListener((e)-> onMachineChange(name));
					menu.add(button);
					group.add(button);
				});

		return menu;
	}

	private void onMachineChange(String name) {
		logger.debug("Switching to Machine '{}'", name);
		Machines machineStyle = Machines.findByName(name);
		myPlotterRenderer = machineStyle.getPlotterRenderer();
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		preferences.put(KEY_MACHINE_STYLE, machineStyle.name());
	}

	private void saveGCode() {
		logger.debug("Saving to gcode...");
		SaveGCode save = new SaveGCode();
		try {
			save.run(myTurtle, myPlotter, mainFrame);
		} catch(Exception e) {
			logger.error("Error while exporting the gcode", e);
			JOptionPane.showMessageDialog(mainFrame, e.getLocalizedMessage(), Translator.get("Error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private void estimateTime() {
		MarlinSimulation ms = new MarlinSimulation(myPlotter.getSettings());
		int estimatedSeconds = (int)Math.ceil(ms.getTimeEstimate(myTurtle));
		String timeAsString = StringHelper.getElapsedTime(estimatedSeconds);
		String message = Translator.get("EstimatedTimeIs",new String[]{timeAsString});
		JOptionPane.showMessageDialog(mainFrame, message, Translator.get("GetTimeEstimate"), JOptionPane.INFORMATION_MESSAGE);
	}

	private void openPlotterControls() {
		JDialog dialog = new JDialog(mainFrame, Translator.get("PlotterControls.Title"));
		dialog.setPreferredSize(new Dimension(850, 220));
		PlotterControls plotterControls = new PlotterControls(myPlotter,myTurtle, dialog);
		dialog.add(plotterControls);
		dialog.setLocationRelativeTo(mainFrame);
		dialog.pack();

		enableMenuBar(false);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				enableMenuBar(true);
				// make sure to close the connection when the dialog is closed.
				plotterControls.closeConnection();
			}
		});

		dialog.setResizable(false);
		dialog.setVisible(true);
	}

	private JMenu createToolsMenu() {
		JMenu menu = new JMenu(Translator.get("Art Pipeline"));

		try {
			PiCaptureAction pc = new PiCaptureAction();

			JButton bCapture = new JButton(Translator.get("MenuCaptureImage"));
			bCapture.addActionListener((e)-> pc.run(mainFrame,myPaper));
			menu.add(bCapture);
			menu.addSeparator();
		} catch (FailedToRunRaspistillException e) {
			logger.debug("Raspistill unavailable.");
		}

		JMenuItem fit = new JMenuItem(Translator.get("ConvertImagePaperFit"));
		menu.add(fit);
		fit.addActionListener((e)-> setTurtle(ResizeTurtleToPaper.run(myTurtle,myPaper,false)));

		JMenuItem fill = new JMenuItem(Translator.get("ConvertImagePaperFill"));
		menu.add(fill);
		fill.addActionListener((e)-> setTurtle(ResizeTurtleToPaper.run(myTurtle,myPaper,true)));

		JMenuItem scale = new JMenuItem(Translator.get("Scale"));
		menu.add(scale);
		scale.addActionListener((e) -> runScalePanel());

		menu.addSeparator();
		
		JMenuItem flipH = new JMenuItem(Translator.get("FlipH"));
		menu.add(flipH);
		flipH.addActionListener((e) -> myTurtle.scale(1, -1));

		JMenuItem flipV = new JMenuItem(Translator.get("FlipV"));
		menu.add(flipV);
		flipV.addActionListener((e) -> myTurtle.scale(-1, 1));

		menu.addSeparator();
		
		menu.add(new SimplifyTurtle(this));
		menu.add(new ReorderTurtle(this));
		menu.add(new InfillTurtleAction(this));

		return menu;
	}

	private void runScalePanel() {
		ScaleTurtlePanel.runAsDialog(mainFrame, myTurtle);
	}

	private JMenu createGenerateMenu() {
		JMenu menu = new JMenu(Translator.get("MenuGenerate"));
		
		for( TurtleGenerator ici : TurtleGeneratorFactory.available ) {
			JMenuItem mi = new JMenuItem(ici.getName());
			mi.addActionListener((e) -> runGeneratorDialog(ici));
			menu.add(mi);
		}
		
		return menu;
	}
	
	private void runGeneratorDialog(TurtleGenerator ici) {
		ici.setPaper(myPaper);
		ici.addListener(this::setTurtle);
		ici.generate();
		
		JDialog dialog = new JDialog(mainFrame,ici.getName());
		TurtleGeneratorPanel panel = ici.getPanel();
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
				logger.debug(Translator.get("Finished"));
			}
		});

		dialog.setVisible(true);
	}
	
	private void newFile() {
		setTurtle(new Turtle());
	}

	private JMenu createFileMenu() {
		JMenu menu = new JMenu(Translator.get("MenuMakelangelo"));

		JMenuItem buttonNewFile = new JMenuItem(Translator.get("MenuNewFile"));
		buttonNewFile.addActionListener((e) -> newFile());
		menu.add(buttonNewFile);

		JMenuItem buttonOpenFile = new JMenuItem(Translator.get("MenuOpenFile"));
		buttonOpenFile.addActionListener((e) -> openLoadFile(""));
		menu.add(buttonOpenFile);
		
		recentFiles = new RecentFiles(Translator.get("MenuReopenFile"));
		recentFiles.addSubmenuListener((e)-> openLoadFile(((JMenuItem)e.getSource()).getText()));
		menu.add(recentFiles);		
		
		JMenuItem buttonSaveFile = new JMenuItem(Translator.get("MenuSaveFile"));
		buttonSaveFile.addActionListener((e) -> saveFile());
		menu.add(buttonSaveFile);

		menu.addSeparator();
				
		JMenuItem buttonAdjustPreferences = new JMenuItem(Translator.get("MenuPreferences"));
		buttonAdjustPreferences.addActionListener((e)-> myPreferencesPanel.run(mainFrame));
		menu.add(buttonAdjustPreferences);

		JMenuItem buttonFirmwareUpdate = new JMenuItem(Translator.get("FirmwareUpdate"));
		buttonFirmwareUpdate.addActionListener((e) -> runFirmwareUpdate());
		menu.add(buttonFirmwareUpdate);
		
		JMenuItem buttonCheckForUpdate = new JMenuItem(Translator.get("MenuUpdate"));
		buttonCheckForUpdate.addActionListener((e) -> checkForUpdate(false));
		menu.add(buttonCheckForUpdate);

		if (!isMacOS) {
			menu.addSeparator();

			JMenuItem buttonExit = new JMenuItem(Translator.get("MenuQuit"));
			buttonExit.addActionListener((e) -> {
				WindowEvent windowClosing = new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING);
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(windowClosing);
			});
			menu.add(buttonExit);
		}
		return menu;
	}

	public void openLoadFile(String filename) {
		logger.debug("Loading file {}...", filename);
		try {
			LoadFilePanel loader = new LoadFilePanel(myPaper,filename);
			loader.addActionListener((e)-> setTurtle((Turtle)(e).getSource()));
			previewPanel.addListener(loader);
			if(filename!=null && !filename.trim().isEmpty() ) {
				loader.load(filename);
			}
			
			JDialog dialog = new JDialog(mainFrame,LoadFilePanel.class.getSimpleName());
			dialog.add(loader);
			dialog.setLocationRelativeTo(mainFrame);
			dialog.setMinimumSize(new Dimension(500,500));
			dialog.pack();

			enableMenuBar(false);
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					enableMenuBar(true);
					previewPanel.removeListener(loader);
					recentFiles.addFilename(loader.getLastFileIn());
				}
			});

			dialog.setVisible(true);
		} catch(Exception e) {
			logger.error("Error while loading the file {}", filename, e);
			JOptionPane.showMessageDialog(mainFrame, e.getLocalizedMessage(), Translator.get("Error"), JOptionPane.ERROR_MESSAGE);
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
		
		JMenuItem buttonZoomOut = new JMenuItem(Translator.get("MenuView.zoomOut"), KeyEvent.VK_MINUS);
		buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		buttonZoomOut.addActionListener((e) -> camera.zoomOut());
		menu.add(buttonZoomOut);

		JMenuItem buttonZoomIn = new JMenuItem(Translator.get("MenuView.zoomIn"), KeyEvent.VK_EQUALS);
		buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));
		buttonZoomIn.addActionListener((e) -> camera.zoomIn());
		menu.add(buttonZoomIn);
		
		JMenuItem buttonZoomToFit = new JMenuItem(Translator.get("MenuView.zoomFit"), KeyEvent.VK_0);
		buttonZoomToFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
		buttonZoomToFit.addActionListener((e) -> camera.zoomToFit(myPaper.getPaperWidth(),myPaper.getPaperHeight()));
		menu.add(buttonZoomToFit);

		JCheckBoxMenuItem checkboxShowPenUpMoves = new JCheckBoxMenuItem(Translator.get("GFXPreferences.showPenUp"),GFXPreferences.getShowPenUp());
		checkboxShowPenUpMoves.addActionListener((e) -> {
			boolean b = GFXPreferences.getShowPenUp();
			GFXPreferences.setShowPenUp(!b);
		});
		GFXPreferences.addListener((e)->{
			checkboxShowPenUpMoves.setSelected ((boolean)e.getNewValue());
		});
		menu.add(checkboxShowPenUpMoves);

		return menu;
	}

	private JMenu createHelpMenu() {
		JMenu menu = new JMenu(Translator.get("Help"));
		
		JMenuItem buttonViewLog = new JMenuItem(Translator.get("ShowLog"));
		buttonViewLog.addActionListener((e) -> runLogPanel());
		menu.add(buttonViewLog);

		JMenuItem buttonForums = new JMenuItem(Translator.get("MenuForums"));
		buttonForums.addActionListener((e) -> {
			try {
				java.awt.Desktop.getDesktop().browse(URI.create("https://discord.gg/Q5TZFmB"));
			} catch (IOException ioe) {
				logger.error("Can't open the browser to discord", ioe);
			}
		});
		menu.add(buttonForums);
		
		JMenuItem buttonAbout = new JMenuItem(Translator.get("MenuAbout"));
		buttonAbout.addActionListener((e) -> {
			DialogAbout a = new DialogAbout();
			a.display(mainFrame,VERSION);
		});
		menu.add(buttonAbout);

		return menu;
	}

	private void runLogPanel() {
		LogPanel.runAsDialog(mainFrame);
	}

	/**
	 * Parse https://github.com/MarginallyClever/Makelangelo/releases/latest
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

				logger.debug("latest release: {}; this version: {}", line2, VERSION);
				// logger.debug(inputLine.compareTo(VERSION));

				int comp = line2.compareTo(VERSION);
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
	 * See
	 * http://www.dreamincode.net/forums/topic/190944-creating-an-updater-in-
	 * java/
	 *//*
	 * private void downloadUpdate() { String[] run =
	 * {"java","-jar","updater/update.jar"}; try {
	 * Runtime.getRuntime().exec(run); } catch (Exception ex) {
	 * ex.printStackTrace(); } System.exit(0); }
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

		// major layout
		contentPane.add(previewPanel, BorderLayout.CENTER);

		return contentPane;
	}

	//  For thread safety this method should be invoked from the event-dispatching thread.
	private void createAppWindow() {
		logger.debug("Creating GUI...");

		mainFrame = new JFrame(Translator.get("TitlePrefix")+" "+this.VERSION);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClosing();
			}
		});
		
		buildMenuBar();
		
		mainFrame.setContentPane(createContentPane());

		camera.zoomToFit(Paper.DEFAULT_WIDTH, Paper.DEFAULT_HEIGHT);
		
		logger.debug("  make visible...");
		mainFrame.setVisible(true);
		
		setWindowSizeAndPosition();

		setupDropTarget();
	}
	
	private void setupDropTarget() {
		logger.debug("adding drag & drop support...");
		dropTarget = new DropTarget(mainFrame,new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
			    try {
			        Transferable tr = dtde.getTransferable();
			        DataFlavor[] flavors = tr.getTransferDataFlavors();
			        for (int i = 0; i < flavors.length; i++) {
			        	logger.debug("Possible flavor: {}", flavors[i].getMimeType());
			        	if (flavors[i].isFlavorJavaFileListType()) {
			        		dtde.acceptDrop(DnDConstants.ACTION_COPY);
			        		Object o = tr.getTransferData(flavors[i]);
			        		if(o instanceof List<?>) {
			        			List<?> list = (List<?>)o;
			        			if( list.size()>0 ) {
			        				o = list.get(0);
			        				if( o instanceof File ) {
			        					openLoadFile(((File)o).getAbsolutePath());
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
		logger.debug("adjust window size...");

		// Get default screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// window size
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		int width = preferences.getInt(KEY_WINDOW_WIDTH, -1);
		int height = preferences.getInt(KEY_WINDOW_HEIGHT, -1);
    	int windowX = preferences.getInt(KEY_WINDOW_X, -1);
    	int windowY = preferences.getInt(KEY_WINDOW_Y, -1);

		if(width==-1 || height==-1) {
    		logger.debug("...default size");
			width = Math.min(screenSize.width,1200);
			height = Math.min(screenSize.height,1020);
		}
        if(windowX==-1 || windowY==-1) {
    		logger.debug("...default position");
        	// centered
        	windowX = (screenSize.width - width)/2;
        	windowY = (screenSize.height - height)/2;
        }
        
		mainFrame.setSize(width, height);
		mainFrame.setLocation(windowX, windowY);
	}

	// save window position and size
	private void saveWindowSizeAndPosition() {
		Dimension size = this.mainFrame.getSize();
		Point location = this.mainFrame.getLocation();

		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

		preferences.putInt(KEY_WINDOW_WIDTH, size.width);
		preferences.putInt(KEY_WINDOW_HEIGHT, size.height);
		preferences.putInt(KEY_WINDOW_X, location.x);
		preferences.putInt(KEY_WINDOW_Y, location.y);
		try {
			preferences.sync();
		} catch (BackingStoreException e) {
			logger.error("Failed to store size of the window", e);
		}
	}

	/**
	 * Display a dialog asking the user to change the pen
	 * @param toolNumber a 24 bit RGB color of the new pen.
	 */
	private void requestUserChangeTool(int toolNumber) {
		SwingUtilities.invokeLater(()->{
			ChangeToolPanel panel = new ChangeToolPanel(toolNumber);
			panel.run(mainFrame);
		});
	}

	private void onClosing() {
		int result = JOptionPane.showConfirmDialog(mainFrame, Translator.get("ConfirmQuitQuestion"),
				Translator.get("ConfirmQuitTitle"), JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION) {
			previewPanel.removeListener(myPlotter);
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			saveWindowSizeAndPosition();
			myPlotter.getSettings().saveConfig();

			// Log.end() should be the very last call.  mainFrame.dispose() kills the thread, so this is as close as I can get.
			Log.end();

			// Run this on another thread than the AWT event queue to
			// make sure the call to Animator.stop() completes before
			// exiting
			new Thread(()->{
				previewPanel.stop();
				mainFrame.dispose();
			}).start();
		}
	}
	
	private void saveFile() {
		logger.debug("Saving vector file...");
		try {
			saveDialog.run(myTurtle, mainFrame);
		} catch(Exception e) {
			logger.error("Error while saving the vector file", e);
			JOptionPane.showMessageDialog(mainFrame, e.getLocalizedMessage(), Translator.get("Error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setTurtle(Turtle turtle) {
		myTurtle = turtle;
		myTurtleRenderer.setTurtle(turtle);
	}

	public Turtle getTurtle() {
		return myTurtle;
	}

	public static void main(String[] args) {
		Log.start();
		// lazy init to be able to purge old files
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