package com.marginallyclever.makelangelo;
/**
 * @(#)Makelangelo.java drawbot application with GUI
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */

// io functions

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.artPipeline.TransformedImage;
import com.marginallyclever.artPipeline.converters.Converter_CMYK;
import com.marginallyclever.artPipeline.converters.ImageConverter;
import com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveFileType;
import com.marginallyclever.communications.ConnectionManager;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.log.LogPanel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.preferences.MakelangeloAppPreferences;
import com.marginallyclever.makelangelo.preferences.MetricsPreferences;
import com.marginallyclever.makelangelo.preview.Camera;
import com.marginallyclever.makelangelo.preview.PreviewPanel;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotListener;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotPanel;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettingsListener;
import com.marginallyclever.util.PreferencesHelper;
import com.marginallyclever.util.PropertiesFileHelper;

/**
 * The Makelangelo app is a tool for programming CNC robots, typically plotters.  It converts lines (made of segments made of points)
 * into instructions in GCODE format, as described in https://github.com/MarginallyClever/Makelangelo-firmware/wiki/gcode-description.
 * 
 * In order to do this the app also provides convenient methods to load vectors (like DXF or SVG), create vectors (TurtleGenerators), or 
 * interpret bitmaps (like BMP,JPEG,PNG,GIF,TGA) into vectors (ImageConverters).
 * 
 * The app must also know some details about the machine, the surface onto which drawings will be made, and the drawing tool making
 * the mark on the paper.  This knowledge helps the app to create better gcode.  
 * 
 * @author Dan Royer
 * @since 0.0.1
 */
public final class Makelangelo extends TransferHandler
		implements WindowListener, MakelangeloRobotListener, MakelangeloRobotSettingsListener {
	static final long serialVersionUID = 1L;

	/**
	 * Defined in src/resources/makelangelo.properties and uses Maven's resource filtering to update the VERSION based upon VERSION
	 * defined in POM.xml. In this way we only define the VERSION once and prevent violating DRY.
	 */
	public String VERSION;
	
	private final static String FORUM_URL = "https://discord.gg/Q5TZFmB";
	// only used on first run.
	private final static int DEFAULT_WINDOW_WIDTH = 1200;
	private final static int DEFAULT_WINDOW_HEIGHT = 1020;

	private MakelangeloAppPreferences appPreferences;
	
	private ConnectionManager connectionManager;
	
	private Camera camera;
	private MakelangeloRobot robot;

	private ArrayList<Turtle> myTurtles;
	
	protected String lastFileIn = "";
	protected FileFilter lastFilterIn = null;
	protected String lastFileOut = "";
	protected FileFilter lastFilterOut = null;
	
	// GUI elements
	private JFrame mainFrame = null;
	// only allow one log frame
	private JFrame logFrame = null;
	private LogPanel logPanel = null;
	
	// OpenGL window
	private PreviewPanel previewPanel;
	
	// Context sensitive menu
	private MakelangeloRobotPanel robotPanel;
	
	public static void main(String[] argv) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(argv);
		Makelangelo makelangeloProgram = new Makelangelo();
		
		if(GraphicsEnvironment.isHeadless()) {
			// TODO 
		} else {
			// Schedule a job for the event-dispatching thread:
			// creating and showing this application's GUI.
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					makelangeloProgram.runHeadFirst();
				}
			});
		}
	}

	public Makelangelo() {
		super();

		myTurtles = new ArrayList<Turtle>();
		// by default start with one turtle.
		myTurtles.add(new Turtle());
		
		Translator.start();
		
		logPanel = new LogPanel();

		Log.message("Locale="+Locale.getDefault().toString());
		Log.message("Headless="+(GraphicsEnvironment.isHeadless()?"Y":"N"));
		
		Log.message("Starting preferences...");
		//Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
		VERSION = PropertiesFileHelper.getMakelangeloVersionPropertyValue();
		appPreferences = new MakelangeloAppPreferences(this);

		Log.message("Starting robot...");
		// create a robot and listen to it for important news
		robot = new MakelangeloRobot();
		robot.addListener(this);
		robot.getSettings().addListener(this);
		logPanel.setRobot(robot);

		Log.message("Starting camera...");
		camera = new Camera();
		
		// network connections
		Log.message("Starting connection manager...");
		connectionManager = new ConnectionManager();
	}
	
	public void runHeadFirst() {
		createAppWindow();
		
		checkSharingPermission();

		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
		if (preferences.getBoolean("Check for updates", false))
			checkForUpdate(true);
	}

	// check if we need to ask about sharing
	protected void checkSharingPermission() {
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
	public JMenuBar createMenuBar() {
		Log.message("Create menu bar");

		JMenuBar menuBar = new JMenuBar();

		JMenu menu;

		// File menu
		{
			Log.message("  file...");
			menu = new JMenu(Translator.get("MenuMakelangelo"));
			menuBar.add(menu);
	
			JMenuItem buttonAdjustPreferences = new JMenuItem(Translator.get("MenuPreferences"));
			buttonAdjustPreferences.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					appPreferences.run();
				}
			});
			menu.add(buttonAdjustPreferences);
	
			JMenuItem buttonCheckForUpdate = new JMenuItem(Translator.get("MenuUpdate"));
			buttonCheckForUpdate.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					checkForUpdate(false);
				}
			});
			menu.add(buttonCheckForUpdate);
	
			menu.addSeparator();
	
			JMenuItem buttonExit = new JMenuItem(Translator.get("MenuQuit"));
			buttonExit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onClose();
				}
			});
			menu.add(buttonExit);
		}

		
		// view menu
		{
			Log.message("  view...");
			menu = new JMenu(Translator.get("MenuPreview"));
			menuBar.add(menu);
			
			JMenuItem buttonZoomOut = new JMenuItem(Translator.get("ZoomOut"), KeyEvent.VK_MINUS);
			buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			buttonZoomOut.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					camera.zoomOut();
				};
			});
			menu.add(buttonZoomOut);
	
			JMenuItem buttonZoomIn = new JMenuItem(Translator.get("ZoomIn"), KeyEvent.VK_EQUALS);
			buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			buttonZoomIn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					camera.zoomIn();
				};
			});
			menu.add(buttonZoomIn);
			
			JMenuItem buttonZoomToFit = new JMenuItem(Translator.get("ZoomFit"), KeyEvent.VK_0);
			buttonZoomToFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			buttonZoomToFit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					camera.zoomToFit(
							robot.getSettings().getPaperWidth(),
							robot.getSettings().getPaperHeight());
				};
			});
			menu.add(buttonZoomToFit);
			
			JMenuItem buttonViewLog = new JMenuItem(Translator.get("ShowLog"));
			buttonViewLog.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
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
				}
			});
			menu.add(buttonViewLog);
		}
		
		// help menu
		{
			Log.message("  help...");
			menu = new JMenu(Translator.get("Help"));
			menuBar.add(menu);
	
			JMenuItem buttonForums = new JMenuItem(Translator.get("MenuForums"));
			buttonForums.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						java.awt.Desktop.getDesktop().browse(URI.create(FORUM_URL));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			menu.add(buttonForums);
			
			JMenuItem buttonAbout = new JMenuItem(Translator.get("MenuAbout"));
			buttonAbout.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					DialogAbout a = new DialogAbout();
					a.display(mainFrame,VERSION);
				}
			});
			menu.add(buttonAbout);
		}
		
		// finish
		Log.message("  finish...");
		menuBar.updateUI();

		return menuBar;
	}

	/**
	 * Parse https://github.com/MarginallyClever/Makelangelo/releases/latest
	 * redirect notice to find the latest release tag.
	 */
	public void checkForUpdate(boolean announceIfFailure) {
		Log.message("checking for updates...");
		try {
			URL github = new URL("https://github.com/MarginallyClever/Makelangelo-Software/releases/latest");
			HttpURLConnection conn = (HttpURLConnection) github.openConnection();
			conn.setInstanceFollowRedirects(false); // you still need to handle
													// redirect manully.
			HttpURLConnection.setFollowRedirects(false);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String inputLine = in.readLine();
			if (inputLine == null) {
				throw new Exception("Could not read from update server.");
			}

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


	/**
	 *  For thread safety this method should be invoked from the event-dispatching thread.
	 */
	public void createAppWindow() {
		Log.message("Creating GUI...");

		// overall look and feel 1
		//JFrame.setDefaultLookAndFeelDecorated(true);  // ugly!

		mainFrame = new JFrame(Translator.get("TitlePrefix")+" "+this.VERSION);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(this);
		
		// overall look and feel 2
        try {
        	// weird but less ugly.
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
		
		JMenuBar bar = createMenuBar();
		Log.message("  adding menu bar...");
		mainFrame.setJMenuBar(bar);

		{
			Log.message("create content pane...");
			JPanel contentPane = new JPanel(new BorderLayout());
			contentPane.setOpaque(true);
	
			Log.message("  create PreviewPanel...");
			previewPanel = new PreviewPanel();
			previewPanel.setCamera(camera);
			previewPanel.addListener(robot);
	
			Log.message("  assign panel to robot...");
			robotPanel = robot.createControlPanel(this);
	
			// major layout
			Log.message("  vertical split...");
			Splitter splitLeftRight = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
			splitLeftRight.add(previewPanel);
			splitLeftRight.add(new JScrollPane(robotPanel));
	
			contentPane.add(splitLeftRight, BorderLayout.CENTER);
			mainFrame.setContentPane(contentPane);
		}
		
		adjustWindowSize();

		camera.zoomToFit(
				robot.getSettings().getPaperWidth(),
				robot.getSettings().getPaperHeight());
		
		Log.message("  make visible...");
		mainFrame.setVisible(true);

		Log.message("  adding drag & drop support...");
		mainFrame.setTransferHandler(this);
	}

	private void adjustWindowSize() {
		Log.message("adjust window size...");

		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

		int width = preferences.getInt("Default window width", DEFAULT_WINDOW_WIDTH);
		int height = preferences.getInt("Default window height", DEFAULT_WINDOW_HEIGHT);

		// Get default screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// Set window size
		if (width > screenSize.width || height > screenSize.height) {
			width = screenSize.width;
			height = screenSize.height;

			preferences.putInt("Default window width", width);
			preferences.putInt("Default window height", height);
		}

		mainFrame.setSize(width, height);

		// by default center the window. Later use preferences.
		int defaultLocationX = (screenSize.width - width) / 2;
		int defaultLocationY = (screenSize.height - height) / 2;
		mainFrame.setLocation(defaultLocationX, defaultLocationY);
		// int locationX = prefs.getInt("Default window location x",
		// defaultLocationX);
		// int locationY = prefs.getInt("Default window location y",
		// defaultLocationY);
		// mainFrame.setLocation(locationX,locationY);
	}

	@Override
	public void portConfirmed(MakelangeloRobot r) {
		if (previewPanel != null)
			previewPanel.repaint();
	}

	@Override
	public void firmwareVersionBad(MakelangeloRobot r, long versionFound) {
		(new DialogBadFirmwareVersion()).display(mainFrame, Long.toString(versionFound));
	}

	@Override
	public void dataAvailable(MakelangeloRobot r, String data) {
		if (data.endsWith("\n"))
			data = data.substring(0, data.length() - 1);
		Log.message(data); // #ffa500 = orange
	}

	@Override
	public void sendBufferEmpty(MakelangeloRobot r) {}

	@Override
	public void lineError(MakelangeloRobot r, int lineNumber) {}

	@Override
	public void disconnected(MakelangeloRobot r) {
		if (previewPanel != null)
			previewPanel.repaint();
		SoundSystem.playDisconnectSound();
	}

	public void settingsChangedEvent(MakelangeloRobotSettings settings) {
		if (previewPanel != null)
			previewPanel.repaint();
	}

	public NetworkConnection requestNewConnection() {
		return connectionManager.requestNewConnection(this.mainFrame);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		onClose();
	}

	private void onClose() {
		int result = JOptionPane.showConfirmDialog(mainFrame, Translator.get("ConfirmQuitQuestion"),
				Translator.get("ConfirmQuitTitle"), JOptionPane.YES_NO_OPTION);

		if (result == JOptionPane.YES_OPTION) {
			previewPanel.removeListener(robot);
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			saveWindowRealEstate();
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

	/**
	 * save window position and size
	 */
	private void saveWindowRealEstate() {
		Dimension size = this.mainFrame.getSize();
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

		preferences.putInt("Default window width", size.width);
		preferences.putInt("Default window height", size.height);

		Point location = this.mainFrame.getLocation();
		preferences.putInt("Default window location x", location.x);
		preferences.putInt("Default window location y", location.y);
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

	public JFrame getMainFrame() {
		return mainFrame;
	}

	public MakelangeloRobot getRobot() {
		return robot;
	}
	
	// transfer handler
	@Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        // we only import FileList
        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            Log.message("Does not support files of type(s): "+info.getDataFlavors());
            return false;
        }
        return true;
    }

	// transfer handler
	@Override
    public boolean importData(TransferHandler.TransferSupport info) {
    	// only accept drops
        if (!info.isDrop()) return false;
        
        // recommended to explicitly call canImport from importData (see java documentation)
        if(!canImport(info)) return false;
        
        // Get the fileList that is being dropped.
        List<?> data = null;
        try {
        	data = (List<?>)info.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
        } 
        catch (Exception e) {
        	return false;
        }

        if(data==null) return false;
        // accept only one file at a time.
        if(data.size()!=1) return false;
        
        String filename = ((File)data.get(0)).getAbsolutePath();

        return openFileOnDemand(filename);
    }
	


	/**
	 * Open a file with a given LoadAndSaveFileType plugin.  
	 * The loader might spawn a new thread and return before the load is actually finished.
	 * @param filename absolute path of the file to load
	 * @param loader the plugin to use
	 * @return true if load is successful.
	 */
	public boolean openFileOnDemandWithLoader(String filename,LoadAndSaveFileType loader) {
		boolean success = false;
		try (final InputStream fileInputStream = new FileInputStream(filename)) {
			success=loader.load(fileInputStream,getSelectedTurtle());
		} catch(IOException e) {
			e.printStackTrace();
		}

		// TODO don't rely on success to be true, load may not have finished yet.
		if (success == true) {
			SoundSystem.playConversionFinishedSound();
			if( robot.getControlPanel() != null ) {
				robot.getControlPanel().updateButtonAccess();
			}
		}
		
		return success;
	}
	
	/**
	 * User has asked that a file be opened.
	 * @param filename the file to be opened.
	 * @return true if file was loaded successfully.  false if it failed.
	 */
	public boolean openFileOnDemand(String filename) {
		Log.message(Translator.get("OpeningFile") + filename + "...");

		ServiceLoader<LoadAndSaveFileType> imageLoaders = ServiceLoader.load(LoadAndSaveFileType.class);
		Iterator<LoadAndSaveFileType> i = imageLoaders.iterator();
		while(i.hasNext()) {
			LoadAndSaveFileType loader = i.next();
			if(!loader.canLoad()) continue;  // TODO feels redundant given the next line
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
		// create the chooser
		JFileChooser fc = new JFileChooser();
		
		// list available loaders
		ServiceLoader<LoadAndSaveFileType> imageLoaders = ServiceLoader.load(LoadAndSaveFileType.class);
		for( LoadAndSaveFileType lft : imageLoaders ) {
			if(lft.canLoad()) {
				FileFilter filter = lft.getFileNameFilter();
				fc.addChoosableFileFilter(filter);
			}
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
			FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter)fc.getFileFilter();

			// figure out which of the loaders was requested.
			for( LoadAndSaveFileType loader : imageLoaders ) {
				if( !isMatchingFileFilter(selectedFilter, (FileNameExtensionFilter)loader.getFileNameFilter()) ) continue;
				boolean success = openFileOnDemandWithLoader(selectedFile,loader);
				if(success) {
					lastFilterIn = selectedFilter;
					lastFileIn = selectedFile;
					
					if( robot.getControlPanel() != null ) {
						robot.getControlPanel().updateButtonAccess();
					}
					break;
				}
			}
		}
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
		ServiceLoader<LoadAndSaveFileType> imageSavers = ServiceLoader.load(LoadAndSaveFileType.class);
		for( LoadAndSaveFileType lft : imageSavers ) {
			if(lft.canSave()) {
				FileFilter filter = lft.getFileNameFilter();
				fc.addChoosableFileFilter(filter);
			}
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
			for( LoadAndSaveFileType lft : imageSavers ) {
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
					success=lft.save(fileOutputStream,myTurtles,robot);
				} catch(IOException e) {
					JOptionPane.showMessageDialog(getMainFrame(), "Save failed: "+e.getMessage());
					//e.printStackTrace();
				}
				if(success==true) {
					lastFileOut = selectedFile;
					lastFilterOut = selectedFilter;
					if( robot.getControlPanel() != null ) {
						robot.getControlPanel().updateButtonAccess();
					}
					break;
				}					
			}
			// No file filter was found.  Wait, what?!
		}
	}
	
	public String getLastFileIn() {
		return lastFileIn;
	}
	
	/**
	 * Adds a new turtle to the tail of the myTurtles list. 
	 * @return the newly created Turtle.
	 */
	public Turtle addTurtle() {
		Turtle t = new Turtle();
		myTurtles.add(t);
		return t;
	}

	public Turtle getSelectedTurtle() {
		return myTurtles.get(myTurtles.size()-1);
	}
	

	public void testGeneratorsAndConverters() {
		// temp
		//TurtleGenerator g = new Generator_Dragon();
		//TurtleGenerator g = new Generator_FibonacciSpiral();
		//TurtleGenerator g = new Generator_FillPage();
		//TurtleGenerator g = new Generator_GosperCurve();
		//TurtleGenerator g = new Generator_GraphPaper();
		//TurtleGenerator g = new Generator_HilbertCurve();
		//TurtleGenerator g = new Generator_KochCurve();
		//TurtleGenerator g = new Generator_Lissajous();
		//TurtleGenerator g = new Generator_LSystemTree();
		//TurtleGenerator g = new Generator_Maze();
		//TurtleGenerator g = new Generator_Package();
		//TurtleGenerator g = new Generator_Polyeder();
		//TurtleGenerator g = new Generator_SierpinskiTriangle();
		//TurtleGenerator g = new Generator_Spirograph();
		//Generator_Text g = new Generator_Text(); g.setMessage("Hello, World!");
		//myTurtles.add(g.generate());
		
		TransformedImage owl = TransformedImage.loadImage("C:\\Users\\aggra\\Documents\\GitHub\\makelangelo-software\\src\\test\\resources\\owl.jpg");
		owl.rotateAbsolute(-25);
		owl.setScale(0.5, 0.5);
		
		//ImageConverter c = new Converter_Boxes();
		ImageConverter c = new Converter_CMYK();
		//ImageConverter c = new Converter_Crosshatch();
		//ImageConverter c = new Converter_MagicCircle();
		//ImageConverter c = new Converter_Moire();
		//ImageConverter c = new Converter_Multipass();
		//ImageConverter c = new Converter_Pulse();
		/*
		ImageConverter c = new Converter_RandomLines();
		//ImageConverter c2 = new Converter_Sandy();
		owl.setTranslateY(120);
		c.setImage(owl);
		myTurtles.addAll(c.finish());

		ImageConverter c2 = new Converter_Spiral_CMYK();
		owl.setTranslateY(-240);
		c2.setImage(owl);
		myTurtles.addAll(c2.finish());
		*/
		//ImageConverter c = new Converter_Spiral_CMYK();
		//ImageConverter c = new Converter_Spiral();
		//ImageConverter c = new Converter_SpiralPulse();
		//ImageConverter c = new Converter_Wander();
		//ImageConverter c = new Converter_ZigZag();
		
		c.setImage(owl);
		myTurtles.addAll(c.finish());
		
		if(myTurtles.size()>0) {
			robot.setTurtles(myTurtles);
		}
	}
}

/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Makelangelo. If not, see <http://www.gnu.org/licenses/>.
 */
