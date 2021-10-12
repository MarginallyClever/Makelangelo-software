package com.marginallyclever.makelangelo;
/**
 * @(#)Makelangelo.java drawbot application with GUI
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */

// io functions

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.log.LogPanel;
import com.marginallyclever.makelangelo.firmwareUploader.FirmwareUploaderPanel;
import com.marginallyclever.makelangelo.makeArt.ReorderTurtle;
import com.marginallyclever.makelangelo.makeArt.ResizeTurtleToPaper;
import com.marginallyclever.makelangelo.makeArt.SimplifyTurtle;
import com.marginallyclever.makelangelo.makeArt.io.LoadFilePanel;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGeneratorFactory;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGeneratorPanel;
import com.marginallyclever.makelangelo.preferences.GFXPreferences;
import com.marginallyclever.makelangelo.preferences.MakelangeloAppPreferences;
import com.marginallyclever.makelangelo.preferences.MetricsPreferences;
import com.marginallyclever.makelangelo.preview.Camera;
import com.marginallyclever.makelangelo.preview.PreviewPanel;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleRenderFacade;
import com.marginallyclever.makelangelo.plotter.PiCaptureAction;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterEvent;
import com.marginallyclever.makelangelo.plotter.plotterControls.PlotterControls;
import com.marginallyclever.util.PreferencesHelper;
import com.marginallyclever.util.PropertiesFileHelper;

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
	private static final String KEY_WINDOW_Y = "windowX";
	private static final String KEY_WINDOW_WIDTH = "windowWidth";
	private static final String KEY_WINDOW_HEIGHT = "windowHeight";

	/**
	 * Defined in src/resources/makelangelo.properties and uses Maven's resource filtering to update the 
	 * VERSION based upon pom.xml.  In this way we only define the VERSION once and prevent violating DRY.
	 */
	public String VERSION;

	private MakelangeloAppPreferences appPreferences;
	
	private Camera camera;
	private Plotter myPlotter;
	private Paper myPaper = new Paper();
	private Turtle myTurtle = new Turtle();

	private TurtleRenderFacade myTurtleRenderer = new TurtleRenderFacade();
	
	// GUI elements
	private JFrame mainFrame;
	private PreviewPanel previewPanel;
	private static JFrame logFrame;
	private SaveDialog saveDialog;
	
	private RecentFiles recentFiles;

	// drag files into the app with {@link DropTarget}
	@SuppressWarnings("unused")
	private DropTarget dropTarget;
	
	public static void main(String[] args) {
		setSystemLookAndFeel();
        
		logFrame = LogPanel.createFrame();
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(()->{
			Makelangelo makelangeloProgram = new Makelangelo();
			makelangeloProgram.run();
		});
	}

	private static void setSystemLookAndFeel() {
        try {
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
	}

	public Makelangelo() {
		Log.message("Locale="+Locale.getDefault().toString());
		Log.message("Headless="+(GraphicsEnvironment.isHeadless()?"Y":"N"));
		
		Log.message("Starting preferences...");
		VERSION = PropertiesFileHelper.getMakelangeloVersionPropertyValue();
		appPreferences = new MakelangeloAppPreferences();

		Log.message("Starting robot...");
		myPlotter = new Plotter();
		myPlotter.addListener((e)->{
			if(e.type==PlotterEvent.TOOL_CHANGE) requestUserChangeTool((int)e.extra);
		});
		myPlotter.getSettings().addListener((e)->{
			if(previewPanel != null) previewPanel.repaint();
		});

		saveDialog = new SaveDialog();
		
		Log.message("Starting virtual camera...");
		camera = new Camera();
	}
	
	public void run() {
		createAppWindow();		
		checkSharingPermission();

		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
		if (preferences.getBoolean("Check for updates", false)) checkForUpdate(true);
	}

	// check if we need to ask about sharing
	private void checkSharingPermission() {
		Log.message("Checking sharing permissions...");
		
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
	 * If the menu bar exists, empty it. If it doesn't exist, create it.
	 * @return the refreshed menu bar
	 */
	private JMenuBar getMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());
		menuBar.add(createGenerateMenu());
		menuBar.add(createToolsMenu());
		menuBar.add(createViewMenu());
		menuBar.add(createRobotMenu());
		menuBar.add(createHelpMenu());
		menuBar.updateUI();
		
		return menuBar;
	}

	private JMenu createRobotMenu() {
		JMenuItem bOpenControls = new JMenuItem(Translator.get("OpenControls"));
		bOpenControls.addActionListener((e)-> openPlotterControls());

		JMenu menu = new JMenu(Translator.get("Robot"));
		menu.add(bOpenControls);
		
		return menu;
	}

	private void openPlotterControls() {
		PlotterControls plotterControls = new PlotterControls(myPlotter,myTurtle);
		JDialog dialog = new JDialog(mainFrame,PlotterControls.class.getSimpleName());
		dialog.add(plotterControls);
		dialog.setLocationRelativeTo(mainFrame);
		dialog.setMinimumSize(new Dimension(300,300));
		dialog.pack();
		// make sure pc closes the connection when the dialog is closed.
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				plotterControls.closeConnection();
			}
		});
		dialog.setVisible(true);
		
	}

	private JMenu createToolsMenu() {
		JMenu menu = new JMenu(Translator.get("Art Pipeline"));

		try {
			PiCaptureAction pc = new PiCaptureAction();
			
			if(pc != null) {
				JButton bCapture = new JButton(Translator.get("MenuCaptureImage"));
				bCapture.addActionListener((e)->{
					pc.run(mainFrame,myPaper);
				});
		        menu.add(bCapture);
				menu.addSeparator();
		    } 
		} catch (FailedToRunRaspistillException e) {
			Log.message("Raspistill unavailable.");
		}

		JMenuItem fit = new JMenuItem(Translator.get("ConvertImagePaperFit"));
		menu.add(fit);
		fit.addActionListener((e)->{
			setTurtle(ResizeTurtleToPaper.run(myTurtle,myPaper,false));
		});

		JMenuItem fill = new JMenuItem(Translator.get("ConvertImagePaperFill"));
		menu.add(fill);
		fill.addActionListener((e)->{
			setTurtle(ResizeTurtleToPaper.run(myTurtle,myPaper,true));
		});

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

		return menu;
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
		ici.addListener((t) -> setTurtle(t));
		ici.generate();
		
		JDialog dialog = new JDialog(mainFrame,ici.getName());
		TurtleGeneratorPanel panel = ici.getPanel();
		dialog.add(panel.getPanel());
		dialog.setLocationRelativeTo(mainFrame);
		dialog.setMinimumSize(new Dimension(300,300));
		dialog.pack();
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				myPaper.setRotationRef(0);
				Log.message(Translator.get("Finished"));
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
		recentFiles.addSubmenuListener((e)->{
			openLoadFile(((JMenuItem)e.getSource()).getText());	
		});
		menu.add(recentFiles);		
		
		JMenuItem buttonSaveFile = new JMenuItem(Translator.get("MenuSaveFile"));
		buttonSaveFile.addActionListener((e) -> saveFile());
		menu.add(buttonSaveFile);

		menu.addSeparator();
				
		JMenuItem buttonAdjustPreferences = new JMenuItem(Translator.get("MenuPreferences"));
		buttonAdjustPreferences.addActionListener((e)-> appPreferences.run(mainFrame));
		menu.add(buttonAdjustPreferences);

		JMenuItem buttonFirmwareUpdate = new JMenuItem(Translator.get("FirmwareUpdate"));
		buttonFirmwareUpdate.addActionListener((e) -> runFirmwareUpdate());
		menu.add(buttonFirmwareUpdate);
		
		JMenuItem buttonCheckForUpdate = new JMenuItem(Translator.get("MenuUpdate"));
		buttonCheckForUpdate.addActionListener((e) -> checkForUpdate(false));
		menu.add(buttonCheckForUpdate);

		menu.addSeparator();

		JMenuItem buttonExit = new JMenuItem(Translator.get("MenuQuit"));
		buttonExit.addActionListener((e) -> onClose());
		menu.add(buttonExit);

		return menu;
	}

	public void openLoadFile(String filename) {
		Log.message("Loading file "+filename+"...");
		try {
			LoadFilePanel loader = new LoadFilePanel(myPaper,filename);
			loader.addActionListener((e)-> setTurtle((Turtle)(e).getSource()) );
			previewPanel.addListener(loader);
			if(filename!=null && !filename.trim().isEmpty() ) {
				loader.load(filename);
			}
			
			JDialog dialog = new JDialog(mainFrame,LoadFilePanel.class.getSimpleName());
			dialog.add(loader);
			dialog.setLocationRelativeTo(mainFrame);
			dialog.setMinimumSize(new Dimension(500,500));
			dialog.pack();
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					previewPanel.removeListener(loader);
					recentFiles.addFilename(loader.getLastFileIn());
				}
			});
			dialog.setVisible(true);
			
		} catch(Exception e) {
			Log.error("Load error: "+e.getMessage()); 
			JOptionPane.showMessageDialog(mainFrame, e.getLocalizedMessage(), Translator.get("Error"), JOptionPane.ERROR_MESSAGE);
			recentFiles.removeFilename(filename);
		}
	}

	private void runFirmwareUpdate() {
		JDialog dialog = new JDialog(mainFrame,"Firmware Update");
		
		dialog.add(new FirmwareUploaderPanel());
		dialog.pack();
		dialog.setLocationRelativeTo(mainFrame);
		dialog.setVisible(true);
	}

	private JMenu createViewMenu() {
		JMenu menu = new JMenu(Translator.get("MenuPreview"));
		
		JMenuItem buttonZoomOut = new JMenuItem(Translator.get("ZoomOut"), KeyEvent.VK_MINUS);
		buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		buttonZoomOut.addActionListener((e) -> camera.zoomOut());
		menu.add(buttonZoomOut);

		JMenuItem buttonZoomIn = new JMenuItem(Translator.get("ZoomIn"), KeyEvent.VK_EQUALS);
		buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));
		buttonZoomIn.addActionListener((e) -> camera.zoomIn());
		menu.add(buttonZoomIn);
		
		JMenuItem buttonZoomToFit = new JMenuItem(Translator.get("ZoomFit"), KeyEvent.VK_0);
		buttonZoomToFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
		buttonZoomToFit.addActionListener((e) -> {
			camera.zoomToFit(myPaper.getPaperWidth(),myPaper.getPaperHeight());
		});
		menu.add(buttonZoomToFit);

		JCheckBoxMenuItem checkboxShowPenUpMoves = new JCheckBoxMenuItem(Translator.get("MenuGraphicsPenUp"),GFXPreferences.getShowPenUp());
		checkboxShowPenUpMoves.addActionListener((e) -> {
			boolean b = !GFXPreferences.getShowPenUp();
			checkboxShowPenUpMoves.setSelected(b);
			GFXPreferences.setShowPenUp(b);
		});
		menu.add(checkboxShowPenUpMoves);

		return menu;
	}

	private JMenu createHelpMenu() {
		JMenu menu = new JMenu(Translator.get("Help"));
		
		JMenuItem buttonViewLog = new JMenuItem(Translator.get("ShowLog"));
		buttonViewLog.addActionListener((e) -> logFrame.setVisible(true) );
		menu.add(buttonViewLog);

		JMenuItem buttonForums = new JMenuItem(Translator.get("MenuForums"));
		buttonForums.addActionListener((e) -> {
			try {
				java.awt.Desktop.getDesktop().browse(URI.create("https://discord.gg/Q5TZFmB"));
			} catch (IOException e1) {
				Log.error("Forum URL error: "+e1.getLocalizedMessage());
				e1.printStackTrace();
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
	
	/**
	 * Parse https://github.com/MarginallyClever/Makelangelo/releases/latest
	 * redirect notice to find the latest release tag.
	 */
	private void checkForUpdate(boolean announceIfFailure) {
		Log.message("checking for updates...");
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

				Log.message("latest release: " + line2 + "; this version: " + VERSION);
				// Log.message(inputLine.compareTo(VERSION));

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
			Log.error("Update check failed: "+e.getMessage());
			e.printStackTrace();
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
		Log.message("create content pane...");

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);

		Log.message("  create PreviewPanel...");
		previewPanel = new PreviewPanel();
		previewPanel.setCamera(camera);
		previewPanel.addListener(myPaper);
		previewPanel.addListener(myPlotter);
		previewPanel.addListener(myTurtleRenderer);

		// major layout
		contentPane.add(previewPanel, BorderLayout.CENTER);

		return contentPane;
	}

	//  For thread safety this method should be invoked from the event-dispatching thread.
	private void createAppWindow() {
		Log.message("Creating GUI...");

		mainFrame = new JFrame(Translator.get("TitlePrefix")+" "+this.VERSION);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
	
			@Override
			public void windowDeactivated(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowClosed(WindowEvent e) {}
		});
		
		Log.message("  adding menu bar...");
		mainFrame.setJMenuBar(getMenuBar());
		
		mainFrame.setContentPane(createContentPane());

		camera.zoomToFit(myPaper.getPaperWidth(),myPaper.getPaperHeight());
		
		Log.message("  make visible...");
		mainFrame.setVisible(true);
		
		setWindowSizeAndPosition();

		setupDropTarget();
	}
	
	private void setupDropTarget() {
		Log.message("  adding drag & drop support...");
		dropTarget = new DropTarget(mainFrame,new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
			    try {
			        Transferable tr = dtde.getTransferable();
			        DataFlavor[] flavors = tr.getTransferDataFlavors();
			        for (int i = 0; i < flavors.length; i++) {
			        	System.out.println("Possible flavor: " + flavors[i].getMimeType());
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
			        System.out.println("Drop failed: " + dtde);
			        dtde.rejectDrop();
			    } catch (Exception e) {
			        e.printStackTrace();
			        dtde.rejectDrop();
			    }
			}
		});
	}

	private void setWindowSizeAndPosition() {
		Log.message("adjust window size...");

		// Get default screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// window size
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		int width = preferences.getInt(KEY_WINDOW_WIDTH, -1);
		int height = preferences.getInt(KEY_WINDOW_HEIGHT, -1);
    	int windowX = preferences.getInt(KEY_WINDOW_X, -1);
    	int windowY = preferences.getInt(KEY_WINDOW_Y, -1);

		if(width==-1 || height==-1) {
    		Log.message("...default size");
			width = Math.min(screenSize.width,1200);
			height = Math.min(screenSize.height,1020);
		}
        if(windowX==-1 || windowY==-1) {
    		Log.message("...default position");
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
	}

	/**
	 * Display a dialog asking the user to change the pen
	 * @param toolNumber a 24 bit RGB color of the new pen.
	 */
	private void requestUserChangeTool(int toolNumber) {
		ChangeToolPanel panel = new ChangeToolPanel(toolNumber);
		panel.run(mainFrame);
	}

	private void onClose() {
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
			new Thread(new Runnable() {
				public void run() {
					previewPanel.stop();
					mainFrame.dispose();
				}
			}).start();
		}
	}
			
	private void saveFile() {
		Log.message("Saving vector file...");
		try {
			saveDialog.run(myTurtle, mainFrame);
		} catch(Exception e) {
			Log.error("Save error: "+e.getLocalizedMessage()); 
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
}