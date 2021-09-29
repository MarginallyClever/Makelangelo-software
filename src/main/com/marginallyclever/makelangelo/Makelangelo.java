package com.marginallyclever.makelangelo;
/**
 * @(#)Makelangelo.java drawbot application with GUI
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */

// io functions

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.log.LogPanel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.firmwareUploader.FirmwareUploaderPanel;
import com.marginallyclever.makelangelo.makeArt.ReorderTurtle;
import com.marginallyclever.makelangelo.makeArt.ResizeTurtleToPaper;
import com.marginallyclever.makelangelo.makeArt.SimplifyTurtle;
import com.marginallyclever.makelangelo.makeArt.io.LoadResource;
import com.marginallyclever.makelangelo.makeArt.io.SaveResource;
import com.marginallyclever.makelangelo.makeArt.io.image.LoadImage;
import com.marginallyclever.makelangelo.makeArt.io.image.LoadImagePanel;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_Text;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGeneratorPanel;
import com.marginallyclever.makelangelo.preferences.GFXPreferences;
import com.marginallyclever.makelangelo.preferences.MakelangeloAppPreferences;
import com.marginallyclever.makelangelo.preferences.MetricsPreferences;
import com.marginallyclever.makelangelo.preview.Camera;
import com.marginallyclever.makelangelo.preview.PreviewPanel;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotEvent;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotPanel;
import com.marginallyclever.makelangeloRobot.PiCaptureAction;
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
	/**
	 * Defined in src/resources/makelangelo.properties and uses Maven's resource filtering to update the 
	 * VERSION based upon pom.xml.  In this way we only define the VERSION once and prevent violating DRY.
	 */
	public String VERSION;
	
	private final static String FORUM_URL = "https://discord.gg/Q5TZFmB";

	private MakelangeloAppPreferences appPreferences;
	private Camera camera;
	private MakelangeloRobot robot;

	private String lastFileIn = "";
	private FileFilter lastFilterIn = null;
	private String lastFileOut = "";
	private FileFilter lastFilterOut = null;
	
	// GUI elements
	private JFrame mainFrame = null;
	private JFrame logFrame = null;
	private LogPanel logPanel = null;
	private PreviewPanel previewPanel;
	private MakelangeloRobotPanel robotPanel;

	JMenuItem buttonNewFile;
	JMenuItem buttonOpenFile;
	JMenuItem buttonReopenFile;
	
	private PiCaptureAction piCameraCaptureAction;
	
	// for drag + drop operations
	@SuppressWarnings("unused")
	private DropTarget dropTarget;
	
	public static void main(String[] argv) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(argv);

		// set look and feel
        try {
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        	Log.error("Look and feel could not be set: "+e.getLocalizedMessage());
        }
        
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(()->{
			Makelangelo makelangeloProgram = new Makelangelo();
			makelangeloProgram.run();
		});
	}

	public Makelangelo() {
		Translator.start();
		
		logPanel = new LogPanel();
		
		Log.message("Locale="+Locale.getDefault().toString());
		Log.message("Headless="+(GraphicsEnvironment.isHeadless()?"Y":"N"));
		
		Log.message("Starting preferences...");
		VERSION = PropertiesFileHelper.getMakelangeloVersionPropertyValue();
		appPreferences = new MakelangeloAppPreferences();

		Log.message("Starting robot...");
		// create a robot and listen to it for important news
		robot = new MakelangeloRobot();
		robot.addListener((e)->{
			if(e.type==MakelangeloRobotEvent.CONNECTION_READY) whenIdentityConfirmed(e.subject);
			if(e.type==MakelangeloRobotEvent.BAD_FIRMWARE) whenBadFirmwareDetected((String)e.extra);
			if(e.type==MakelangeloRobotEvent.BAD_HARDWARE) whenBadHardwareDetected((String)e.extra);
			if(e.type==MakelangeloRobotEvent.DISCONNECT) whenDisconnected();
			if(e.type==MakelangeloRobotEvent.TOOL_CHANGE) requestUserChangeTool((int)e.extra);
		});
		robot.getSettings().addListener((e)->{
			if(previewPanel != null) previewPanel.repaint();
		});
		logPanel.addListener((command)->{
			robot.send(command);
		});

		Log.message("Starting virtual camera...");
		camera = new Camera();
		
		Log.message("Starting art pipeline...");
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
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());
		menuBar.add(createGenerateMenu());
		menuBar.add(createArtPipelineMenu());
		menuBar.add(createViewMenu());
		menuBar.add(createHelpMenu());
		menuBar.updateUI();
		return menuBar;
	}

	private JMenu createArtPipelineMenu() {
		JMenu menu = new JMenu(Translator.get("Art Pipeline"));

		try {
			piCameraCaptureAction = new PiCaptureAction(this, Translator.get("MenuCaptureImage"));	
			if (piCameraCaptureAction != null) {
		        menu.add(piCameraCaptureAction);
				menu.addSeparator();
		    } 
		} catch (FailedToRunRaspistillException e) {
			Log.message("Raspistill unavailable.");
		}

		JMenuItem fit = new JMenuItem(Translator.get("ConvertImagePaperFit"));
		menu.add(fit);
		fit.addActionListener((e)->{
			robot.setTurtle(ResizeTurtleToPaper.run(robot.getTurtle(),robot.getSettings(),false));
		});

		JMenuItem fill = new JMenuItem(Translator.get("ConvertImagePaperFill"));
		menu.add(fill);
		fill.addActionListener((e)->{
			robot.setTurtle(ResizeTurtleToPaper.run(robot.getTurtle(),robot.getSettings(),true));
		});

		menu.addSeparator();
		
		JMenuItem flipH = new JMenuItem(Translator.get("FlipH"));
		menu.add(flipH);
		flipH.addActionListener((e) -> robot.getTurtle().scale(1, -1));

		JMenuItem flipV = new JMenuItem(Translator.get("FlipV"));
		menu.add(flipV);
		flipV.addActionListener((e) -> robot.getTurtle().scale(-1, 1));

		menu.addSeparator();
		
		menu.add(new SimplifyTurtle(robot));
		menu.add(new ReorderTurtle(robot));

		return menu;
	}

	private JMenu createGenerateMenu() {
		JMenu menu = new JMenu(Translator.get("MenuGenerate"));
		
		ServiceLoader<TurtleGenerator> imageGenerators = ServiceLoader.load(TurtleGenerator.class);
		for( TurtleGenerator ici : imageGenerators ) {
			JMenuItem mi = new JMenuItem(ici.getName());
			mi.addActionListener((e) -> runGeneratorDialog(ici));
			menu.add(mi);
		}
		
		return menu;
	}
	
	private void runGeneratorDialog(TurtleGenerator ici) {
		robot.setDecorator(ici);
		ici.setRobot(robot);

		ici.addListener((t) -> {
			signNameIfDesired(t);
			robot.setTurtle(t);
		});
		
		ici.generate();
		
		JDialog dialog = new JDialog(getMainFrame(),ici.getName());
		TurtleGeneratorPanel panel = ici.getPanel();
		dialog.add(panel.getPanel());
		dialog.setLocationRelativeTo(mainFrame);
		dialog.setMinimumSize(new Dimension(300,300));
		dialog.pack();
		dialog.setVisible(true);

		robot.getSettings().setRotationRef(0);
		robot.setDecorator(null);
		
		Log.message(Translator.get("Finished"));
		SoundSystem.playConversionFinishedSound();
	}

	private void signNameIfDesired(Turtle t) {
		if(!robot.getSettings().shouldSignName()) return;
		
		Generator_Text ymh = new Generator_Text();
		ymh.setRobot(robot);
		ymh.signName();
		t.history.addAll(ymh.turtle.history);
	}
	
	private void newFile() {
		robot.setTurtle(new Turtle());
	}

	private JMenu createFileMenu() {
		JMenu menu = new JMenu(Translator.get("MenuMakelangelo"));

		buttonNewFile = new JMenuItem(Translator.get("MenuNewFile"));
		buttonNewFile.addActionListener((e) -> newFile());
		menu.add(buttonNewFile);

		buttonOpenFile = new JMenuItem(Translator.get("MenuOpenFile"));
		buttonOpenFile.addActionListener((e) -> openFile());
		menu.add(buttonOpenFile);
		
		buttonReopenFile = new JMenuItem(Translator.get("MenuReopenFile"));
		buttonReopenFile.addActionListener((e) -> reopenLastFile());
		buttonReopenFile.setEnabled(false);
		menu.add(buttonReopenFile);		
		
		JMenuItem buttonSaveFile = new JMenuItem(Translator.get("MenuSaveGCODEAs"));
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
			camera.zoomToFit(
					robot.getSettings().getPaperWidth(),
					robot.getSettings().getPaperHeight());
		});
		menu.add(buttonZoomToFit);

		JCheckBoxMenuItem checkboxShowPenUpMoves = new JCheckBoxMenuItem(Translator.get("MenuGraphicsPenUp"),GFXPreferences.getShowPenUp());
		checkboxShowPenUpMoves.addActionListener((e) -> {
			boolean b = !GFXPreferences.getShowPenUp();
			checkboxShowPenUpMoves.setSelected(b);
			GFXPreferences.setShowPenUp(b);
		});
		menu.add(checkboxShowPenUpMoves);
		
		JMenuItem buttonViewLog = new JMenuItem(Translator.get("ShowLog"));
		buttonViewLog.addActionListener((e) -> {
			if(logFrame == null) {
				logFrame = new JFrame(Translator.get("Log"));
				logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				logFrame.setPreferredSize(new Dimension(600,400));
				logFrame.add(logPanel);
				logFrame.pack();
				logFrame.addWindowListener(new WindowListener() {
					@Override
					public void windowOpened(WindowEvent e) {}
					@Override
					public void windowIconified(WindowEvent e) {}
					@Override
					public void windowDeiconified(WindowEvent e) {}
					@Override
					public void windowDeactivated(WindowEvent e) {}
					@Override
					public void windowClosing(WindowEvent e) {}
					@Override
					public void windowClosed(WindowEvent e) {
						logFrame=null;
					}
					@Override
					public void windowActivated(WindowEvent e) {}
				});
			}
			logFrame.setVisible(true);
		});
		menu.add(buttonViewLog);

		return menu;
	}

	private JMenu createHelpMenu() {
		JMenu menu = new JMenu(Translator.get("Help"));

		JMenuItem buttonForums = new JMenuItem(Translator.get("MenuForums"));
		buttonForums.addActionListener((e) -> {
			try {
				java.awt.Desktop.getDesktop().browse(URI.create(FORUM_URL));
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
				if (comp > 0) {
					results = Translator.get("UpdateNotice");
					// TODO downloadUthatpdate(), flashNewFirmwareToRobot();
				} else if (comp < 0)
					results = "This version is from the future?!";
				else
					results = Translator.get("UpToDate");

				JOptionPane.showMessageDialog(mainFrame, results);
			}
			in.close();
		} catch (Exception e) {
			if (announceIfFailure) {
				JOptionPane.showMessageDialog(null, Translator.get("UpdateCheckFailed"));
			}
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
		previewPanel.addListener(robot);

		Log.message("  assign panel to robot...");
		robotPanel = new MakelangeloRobotPanel(contentPane,robot);

		// major layout
		Log.message("  vertical split...");
		Splitter splitLeftRight = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
		splitLeftRight.add(previewPanel);
		splitLeftRight.add(new JScrollPane(robotPanel));

		contentPane.add(splitLeftRight, BorderLayout.CENTER);

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
		mainFrame.setJMenuBar(createMenuBar());
		
		mainFrame.setContentPane(createContentPane());

		camera.zoomToFit(
				robot.getSettings().getPaperWidth(),
				robot.getSettings().getPaperHeight());
		
		Log.message("  make visible...");
		mainFrame.setVisible(true);
		
		setWindowSizeAndPosition();

		setupDropTarget();
	}

	private void setupDropTarget() {
		Log.message("  adding drag & drop support...");
		dropTarget = new DropTarget(mainFrame,new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde) {
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
			}

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
			        					openFileOnDemand(((File)o).getAbsolutePath());
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

		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

		// window size
		int width = preferences.getInt("windowWidth", -1);
		int height = preferences.getInt("windowHeight", -1);
		if(width==-1 || height==-1) {
    		Log.message("...default size");
			width = Math.min(screenSize.width,1200);
			height = Math.min(screenSize.height,1020);
		}

		if (width > screenSize.width || height > screenSize.height) {
			width = screenSize.width;
			height = screenSize.height;
		}

		mainFrame.setSize(width, height);

    	int windowX = preferences.getInt("windowX", -1);
    	int windowY = preferences.getInt("windowY", -1);
        if(windowX==-1 || windowY==-1) {
    		Log.message("...default position");
        	// centered
        	windowX = (screenSize.width - width)/2;
        	windowY = (screenSize.height - height)/2;
        }
		mainFrame.setLocation(windowX, windowY);
	}

	/**
	 * Display a dialog asking the user to change the pen
	 * @param toolNumber a 24 bit RGB color of the new pen.
	 */
	private void requestUserChangeTool(int toolNumber) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10, 10, 10, 10);

		JLabel fieldValue = new JLabel("");
		fieldValue.setOpaque(true);
		fieldValue.setMinimumSize(new Dimension(80, 20));
		fieldValue.setMaximumSize(fieldValue.getMinimumSize());
		fieldValue.setPreferredSize(fieldValue.getMinimumSize());
		fieldValue.setSize(fieldValue.getMinimumSize());
		fieldValue.setBackground(new Color(toolNumber));
		fieldValue.setBorder(new LineBorder(Color.BLACK));
		panel.add(fieldValue, c);

		JLabel message = new JLabel(Translator.get("ChangeToolMessage"));
		c.gridx = 1;
		c.gridwidth = 3;
		panel.add(message, c);

		JOptionPane.showMessageDialog(mainFrame, panel, Translator.get("ChangeToolTitle"), JOptionPane.PLAIN_MESSAGE);
	}
	
	private void whenIdentityConfirmed(MakelangeloRobot r) {
		if(previewPanel != null) previewPanel.repaint();
	}

	private void whenBadFirmwareDetected(String versionFound) {
		(new DialogBadFirmwareVersion()).display(mainFrame, versionFound);
	}

	private void whenBadHardwareDetected(String versionFound) {
		JOptionPane.showMessageDialog(mainFrame, Translator.get("hardwareVersionBadMessage", new String[]{versionFound}));
	}

	private void whenDisconnected() {
		if(previewPanel != null) previewPanel.repaint();
		SoundSystem.playDisconnectSound();
	}

	private void onClose() {
		int result = JOptionPane.showConfirmDialog(mainFrame, Translator.get("ConfirmQuitQuestion"),
				Translator.get("ConfirmQuitTitle"), JOptionPane.YES_NO_OPTION);

		if (result == JOptionPane.YES_OPTION) {
			previewPanel.removeListener(robot);
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			saveWindowSizeAndPosition();
			robot.getSettings().saveConfig();

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

	// save window position and size
	private void saveWindowSizeAndPosition() {
		Dimension size = this.mainFrame.getSize();
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

		preferences.putInt("Default window width", size.width);
		preferences.putInt("Default window height", size.height);

		Point location = this.mainFrame.getLocation();
		preferences.putInt("Default window location x", location.x);
		preferences.putInt("Default window location y", location.y);
	}
	
	public JFrame getMainFrame() {
		return mainFrame;
	}

	public MakelangeloRobot getRobot() {
		return robot;
	}
	
	/**
	 * Open a file with a given LoadAndSaveFileType plugin.  
	 * The loader might spawn a new thread and return before the load is actually finished.
	 * @param filename absolute path of the file to load
	 * @param loader the plugin to use
	 * @return true if load is successful.
	 */
	public boolean openFileOnDemandWithLoader(String filename,LoadResource loader) {
		boolean success = false;
		try(final InputStream fileInputStream = new FileInputStream(filename)) {
			if(loader instanceof LoadImage) {
				LoadImage imageLoader = (LoadImage)loader;
				imageLoader.load(fileInputStream, robot, mainFrame);
				runImageConversionProcess(imageLoader);
			} else {
				Turtle t = loader.load(fileInputStream, robot ,mainFrame);
				robot.setTurtle(t);
			}
			success=true;
		} catch(Exception e) {
			Log.error("Load error: "+e.getLocalizedMessage()); 
			JOptionPane.showMessageDialog(mainFrame, e.getLocalizedMessage(), Translator.get("Error"), JOptionPane.ERROR_MESSAGE);
		}

		return success;
	}
	
	private void runImageConversionProcess(LoadImage imageLoader) {
		LoadImagePanel panel = new LoadImagePanel(imageLoader);
		panel.run(mainFrame);
	}
	
	/**
	 * User has asked that a file be opened.
	 * @param filename the file to be opened.
	 * @return true if file was loaded successfully.  false if it failed.
	 */
	public boolean openFileOnDemand(String filename) {
		Log.message(Translator.get("OpeningFile") + filename + "...");

		ServiceLoader<LoadResource> imageLoaders = ServiceLoader.load(LoadResource.class);
		Iterator<LoadResource> i = imageLoaders.iterator();
		while(i.hasNext()) {
			LoadResource loader = i.next();
			if(!loader.canLoad(filename)) continue;
			
			return openFileOnDemandWithLoader(filename,loader);
		}
		
		Log.error(Translator.get("UnknownFileType"));
		return false;
	}

	public void reopenLastFile() {
		openFileOnDemand(lastFileIn);
	}

	public void openFile() {
		Log.message("Opening file...");
		try {
			JFileChooser fc = new JFileChooser();
			// list available loaders
			ServiceLoader<LoadResource> imageLoaders = ServiceLoader.load(LoadResource.class);
			for( LoadResource lft : imageLoaders ) {
				FileFilter filter = lft.getFileNameFilter();
				fc.addChoosableFileFilter(filter);
			}
			// no wild card filter, please.
			fc.setAcceptAllFileFilterUsed(false);
			// remember the last filter used, if any
			if(lastFilterIn!=null) fc.setFileFilter(lastFilterIn);
			// remember the last path used, if any
			fc.setCurrentDirectory((lastFileIn==null?null : new File(lastFileIn)));
			
			// run the dialog
			if (fc.showOpenDialog(getMainFrame()) == JFileChooser.APPROVE_OPTION) {
				String selectedFile = fc.getSelectedFile().getAbsolutePath();
				Log.message("File selected by user: "+selectedFile);
				FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter)fc.getFileFilter();
	
				// figure out which of the loaders was requested.
				for( LoadResource loader : imageLoaders ) {
					if( !isMatchingFileFilter(selectedFilter, (FileNameExtensionFilter)loader.getFileNameFilter()) ) continue;
					Log.message("Found potential matching loader.");
					boolean success = openFileOnDemandWithLoader(selectedFile,loader);
					if(success) {
						Log.message("Load success!");
						lastFilterIn = selectedFilter;
						lastFileIn = selectedFile;

						SoundSystem.playConversionFinishedSound();
						updateButtonAccess();
						break;
					}
				}
			}
		}
		catch(Exception e) {
			Log.error(e.getLocalizedMessage());
		}
	}

	private void updateButtonAccess() {
		boolean isRunning = robot.isRunning();
		
		buttonNewFile.setEnabled(!isRunning);
		buttonOpenFile.setEnabled(!isRunning);
		buttonReopenFile.setEnabled(!isRunning && !lastFileIn.isEmpty());
	}
	
	private boolean isMatchingFileFilter(FileNameExtensionFilter a,FileNameExtensionFilter b) {
		if(!a.getDescription().equals(b.getDescription())) return false;
		String [] aa = a.getExtensions();
		String [] bb = b.getExtensions();
		if(aa.length!=bb.length) return false;
		for(int i=0;i<aa.length;++i) {
			if(!aa[i].equals(bb[i])) return false;
		}
		return true;
	}
	
	public void saveFile() {
		// list all the known savable file types.
		File lastDir = (lastFileOut==null?null : new File(lastFileOut));
		JFileChooser fc = new JFileChooser(lastDir);
		ServiceLoader<SaveResource> imageSavers = ServiceLoader.load(SaveResource.class);
		for( SaveResource lft : imageSavers ) {
			FileFilter filter = lft.getFileNameFilter();
			fc.addChoosableFileFilter(filter);
		}
		
		// do not allow wild card (*.*) file extensions
		fc.setAcceptAllFileFilterUsed(false);
		// remember the last path & filter used.
		if(lastFilterOut!=null) fc.setFileFilter(lastFilterOut);
		
		// run the dialog
		if (fc.showSaveDialog(getMainFrame()) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter)fc.getFileFilter();
			
			// figure out which of the savers was requested.
			for( SaveResource lft : imageSavers ) {
				FileNameExtensionFilter filter = (FileNameExtensionFilter)lft.getFileNameFilter();
				//if(!filter.accept(new File(selectedFile))) {
				if( !isMatchingFileFilter(selectedFilter,filter) ) {
					continue;
				}
					
				// make sure a valid extension is added to the file.
				String selectedFileLC = selectedFile.toLowerCase();
				String[] exts = ((FileNameExtensionFilter)filter).getExtensions();
				boolean foundExtension=false;
				for(String ext : exts) {
					if (selectedFileLC.endsWith('.'+ext.toLowerCase())) {
						foundExtension=true;
						break;
					}
				}
				if(!foundExtension) {
					selectedFile+='.'+exts[0];
				}

				// try to save now.
				boolean success = false;
				try (final OutputStream fileOutputStream = new FileOutputStream(selectedFile)) {
					success=lft.save(fileOutputStream,robot,mainFrame);
				} catch(IOException e) {
					JOptionPane.showMessageDialog(getMainFrame(), "Save failed: "+e.getMessage());
					//e.printStackTrace();
				}
				if(success==true) {
					lastFileOut = selectedFile;
					lastFilterOut = selectedFilter;
					updateButtonAccess();
					break;
				}					
			}
			// No file filter was found.  Wait, what?!
		}
	}
	
	public String getLastFileIn() {
		return lastFileIn;
	}
}