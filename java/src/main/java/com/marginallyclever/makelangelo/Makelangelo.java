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
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.communications.MarginallyCleverConnectionManager;
import com.marginallyclever.communications.SerialConnectionManager;
import com.marginallyclever.makelangelo.preferences.MakelangeloAppPreferences;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotListener;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotPanel;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettingsListener;
import com.marginallyclever.util.PreferencesHelper;
import com.marginallyclever.util.PropertiesFileHelper;


/**
 * The root window of the GUI
 * @author danroyer
 * @author Peter Colapietro
 * @since 0.0.1?
 */
public final class Makelangelo
implements ActionListener, WindowListener, MakelangeloRobotListener, MakelangeloRobotSettingsListener {
	static final long serialVersionUID = 1L;

	/**
	 * software VERSION. Defined in src/resources/makelangelo.properties and uses Maven's resource filtering to update
	 * the VERSION based upon VERSION defined in POM.xml. In this way we only define the VERSION once and prevent
	 * violating DRY.
	 */
	public static final String VERSION = PropertiesFileHelper.getMakelangeloVersionPropertyValue();

	// only used on first run.
	private static final int DEFAULT_WINDOW_WIDTH = 1200;
	private static final int DEFAULT_WINDOW_HEIGHT = 1020;
	
	@SuppressWarnings("deprecation")
	private Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);

	private MakelangeloAppPreferences appPreferences;
	private MarginallyCleverConnectionManager connectionManager;
	private MakelangeloRobot robot;
	
	private Translator translator;
	
	// GUI elements
	private JFrame mainFrame = null;
	private JPanel contentPane;
	
	// Top of window
	private JMenuBar menuBar;
	// file menu
	private JMenuItem buttonAbout, buttonAdjustPreferences, buttonCheckForUpdate, buttonExit;
	// view menu
	private JMenuItem buttonZoomIn, buttonZoomOut, buttonZoomToFit;
	
	// main window layout
	private Splitter splitLeftRight;
	private Splitter splitUpDown;
	
	// OpenGL window
	private DrawPanel drawPanel;
	// Context sensitive menu
	private MakelangeloRobotPanel robotPanel;
	// Bottom of window
	private LogPanel logPanel;
	
	private FPSAnimator animator;
	
	// Drag & drop support
	private MakelangeloTransferHandler myTransferHandler;
	
	
	public static void main(String[] argv) {
		Log.clear();
		CommandLineOptions.setFromMain(argv);
		
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Makelangelo();
			}
		});
	}


	public Makelangelo() {
		appPreferences = new MakelangeloAppPreferences(this);
		
		Translator.start();
		
		// create a robot and listen to it for important news
		robot = new MakelangeloRobot();
		robot.addListener(this);
		robot.getSettings().addListener(this);
		
		myTransferHandler = new MakelangeloTransferHandler(robot);
		connectionManager = new SerialConnectionManager();
		
		createAndShowGUI();

		if (prefs.getBoolean("Check for updates", false)) checkForUpdate(true);
	}
	
		
	// The user has done something.  respond to it.
	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if (subject == buttonZoomIn) drawPanel.zoomIn();
		if (subject == buttonZoomOut) drawPanel.zoomOut();
		if (subject == buttonZoomToFit) drawPanel.zoomToFitPaper();
		if (subject == buttonAbout) (new DialogAbout()).display(this.mainFrame);
		if (subject == buttonAdjustPreferences) {
			appPreferences.run();
		}
		if (subject == buttonCheckForUpdate) checkForUpdate(false);
		if (subject == buttonExit) onClose();
	}
	
	
	/**
	 * If the menu bar exists, empty it.  If it doesn't exist, create it.
	 * @return the refreshed menu bar
	 */
	public JMenuBar createMenuBar() {
		menuBar = new JMenuBar();

		JMenu menu;

		// File menu
		menu = new JMenu(Translator.get("MenuMakelangelo"));
		menuBar.add(menu);

		buttonAdjustPreferences = new JMenuItem(Translator.get("MenuPreferences"));
		buttonAdjustPreferences.addActionListener(this);
		menu.add(buttonAdjustPreferences);

		buttonCheckForUpdate = new JMenuItem(Translator.get("MenuUpdate"));
		buttonCheckForUpdate.addActionListener(this);
		menu.add(buttonCheckForUpdate);

		buttonAbout = new JMenuItem(Translator.get("MenuAbout"));
		buttonAbout.addActionListener(this);
		menu.add(buttonAbout);

		menu.addSeparator();

		buttonExit = new JMenuItem(Translator.get("MenuQuit"));
		buttonExit.addActionListener(this);
		menu.add(buttonExit);

		// preferences

		// view menu
		menu = new JMenu(Translator.get("MenuPreview"));
		buttonZoomOut = new JMenuItem(Translator.get("ZoomOut"));
		buttonZoomOut.addActionListener(this);
		buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(buttonZoomOut);

		buttonZoomIn = new JMenuItem(Translator.get("ZoomIn"), KeyEvent.VK_EQUALS);
		buttonZoomIn.addActionListener(this);
		buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(buttonZoomIn);

		buttonZoomToFit = new JMenuItem(Translator.get("ZoomFit"));
		buttonZoomToFit.addActionListener(this);
		menu.add(buttonZoomToFit);

		menuBar.add(menu);
		menuBar.updateUI();

		return menuBar;
	}

	/**
	 * Parse https://github.com/MarginallyClever/Makelangelo/releases/latest redirect notice
	 * to find the latest release tag.
	 */
	public void checkForUpdate(boolean announceIfFailure) {
		try {
			URL github = new URL("https://github.com/MarginallyClever/Makelangelo-Software/releases/latest");
			HttpURLConnection conn = (HttpURLConnection) github.openConnection();
			conn.setInstanceFollowRedirects(false);  //you still need to handle redirect manully.
			HttpURLConnection.setFollowRedirects(false);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String inputLine = in.readLine();
			if(inputLine == null) {
				throw new Exception("Could not read from update server.");
			}
			
			// parse the URL in the text-only redirect
			String matchStart = "<a href=\"";
			String matchEnd = "\">";
			int start = inputLine.indexOf(matchStart);
			int end = inputLine.indexOf(matchEnd);
			if (start != -1 && end != -1) {
				String line2 = inputLine.substring(start + matchStart.length(), end);
				// parse the last part of the redirect URL, which contains the release tag (which is the VERSION)
				line2 = line2.substring(line2.lastIndexOf("/") + 1);

				System.out.println("latest release: " + line2+"; this version: " + VERSION);
				//System.out.println(inputLine.compareTo(VERSION));

				int comp = line2.compareTo(VERSION);
				String results;
				if     (comp>0) {
					results = Translator.get("UpdateNotice");
					//TODO downloadUpdate();
				} else if(comp<0) results = "This version is from the future?!";
				else 			results = Translator.get("UpToDate");

				JOptionPane.showMessageDialog(mainFrame, results);
			}
			in.close();
		} catch (Exception e) {
			if(announceIfFailure) {
				JOptionPane.showMessageDialog(null, Translator.get("UpdateCheckFailed"));
			}
			e.printStackTrace();
		}
	}

	
	/**
	 * See http://www.dreamincode.net/forums/topic/190944-creating-an-updater-in-java/
	 *//*
	private void downloadUpdate() {
		String[] run = {"java","-jar","updater/update.jar"};
        try {
            Runtime.getRuntime().exec(run);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(0);
	}*/
	
	
	/**
	 * Rebuild the contents of the menu based on current program state
	 */
	public void updateMenuBar() {
		if (robotPanel != null) robotPanel.updateButtonAccess();
	}

	
	public Container createContentPane() {
		//Create the content-pane-to-be.
		contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		/*/
        GLCapabilities caps = new GLCapabilities(null);
        caps.setSampleBuffers(true);
        caps.setHardwareAccelerated(true);
        caps.setNumSamples(4);
        /*/
		GLCapabilities caps = new GLCapabilities(null);
		//*/
		drawPanel = new DrawPanel(caps);
		drawPanel.setRobot(robot);

		robotPanel = robot.createControlPanel(this);
		
		logPanel = new LogPanel(translator, robot);

		// major layout
		splitLeftRight = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
		splitLeftRight.add(drawPanel);
		splitLeftRight.add(robotPanel);

		splitUpDown = new Splitter(JSplitPane.VERTICAL_SPLIT);
		
		splitUpDown.add(splitLeftRight);
		splitUpDown.add(logPanel);
		splitUpDown.setResizeWeight(0.9);
		splitUpDown.setOneTouchExpandable(true);
		splitUpDown.setDividerLocation(800);
		Dimension minimumSize = new Dimension(100, 100);
		splitLeftRight.setMinimumSize(minimumSize);
		logPanel.setMinimumSize(minimumSize);
		
		contentPane.add(splitUpDown, BorderLayout.CENTER);

		return contentPane;
	}


	// Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
	private void createAndShowGUI() {
		// Create and set up the window.
		mainFrame = new JFrame(Translator.get("TitlePrefix"));
    	mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(this);
		// Create and set up the content pane.
		mainFrame.setJMenuBar(createMenuBar());
		mainFrame.setContentPane(createContentPane());
		
		// add the drag & drop support
		mainFrame.setTransferHandler(myTransferHandler);
		
		// adjust the window size
		setupFrameRealEstate();
		
		mainFrame.setVisible(true);

		drawPanel.zoomToFitPaper();

		// start animation system        
        animator = new FPSAnimator(30);
        animator.add(drawPanel);
        animator.start();
	}

	
	private void setupFrameRealEstate() {
		int maxWidth = DEFAULT_WINDOW_WIDTH;
		int maxHeight = DEFAULT_WINDOW_HEIGHT;
		int width = prefs.getInt("Default window width", maxWidth );
		int height = prefs.getInt("Default window height", maxHeight );
		
		// Get default screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		maxWidth = screenSize.width;
		maxHeight = screenSize.height;

		// Set window size
		if(width > maxWidth || height > maxHeight ) {
			width = maxWidth;
			height = maxHeight;
			prefs.putInt("Default window width", maxWidth );
			prefs.putInt("Default window height", maxHeight );
		}

		mainFrame.setSize(width, height);
		
		// Set window location
		// by default center the window.  Later use preferences.
		int defaultLocationX = (screenSize.width - width) / 2;
		int defaultLocationY = (screenSize.height - height) / 2;
		mainFrame.setLocation(defaultLocationX,defaultLocationY);
		//int locationX = prefs.getInt("Default window location x", defaultLocationX);
		//int locationY = prefs.getInt("Default window location y", defaultLocationY);
		//mainFrame.setLocation(locationX,locationY);
	}
	

	/**
	 * @return the <code>com.marginallyclever.makelangelo.DrawPanel</code> representing the preview pane of this GUI.
	 */
	public DrawPanel getDrawPanel() {
		return drawPanel;
	}

	
	@Override
	public void portConfirmed(MakelangeloRobot r) {
		drawPanel.repaint();
	}
	
	
	@Override
	public void firmwareVersionBad(MakelangeloRobot r,long versionFound) {
		(new DialogBadFirmwareVersion()).display(this.mainFrame,Long.toString(versionFound));
	}
	
	@Override
	public void dataAvailable(MakelangeloRobot r,String data) {
		if(data.endsWith("\n")) data = data.substring(0, data.length()-1);
		Log.write( "#ffa500",data );  // #ffa500 = orange
	}
	

	@Override
	public void sendBufferEmpty(MakelangeloRobot r) {}
	

	@Override
	public void lineError(MakelangeloRobot r,int lineNumber) {}


	@Override
	public void disconnected(MakelangeloRobot r) {
		drawPanel.repaint();
		SoundSystem.playDisconnectSound();
	}
	
	
	public void settingsChangedEvent(MakelangeloRobotSettings settings) {
		drawPanel.repaint();
	}
	
	
	public MarginallyCleverConnectionManager getConnectionManager() {
		return connectionManager;
	}


	@Override
	public void windowClosing(WindowEvent e) {
		onClose();
	}

	
	private void onClose() {
        int result = JOptionPane.showConfirmDialog(
            mainFrame,
            Translator.get("ConfirmQuitQuestion"),
            Translator.get("ConfirmQuitTitle"),
            JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
        	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        	saveWindowRealEstate();
        	robot.getSettings().saveConfig();

    		// Run this on another thread than the AWT event queue to
    		// make sure the call to Animator.stop() completes before
    		// exiting
    		new Thread(new Runnable() {
    			public void run() {
    				animator.stop();
    				System.exit(0);
    			}
    		}).start();
        }
	}
	
	
	/**
	 * save window position and size
	 */
	private void saveWindowRealEstate() {
		Dimension size = this.mainFrame.getSize();
		prefs.putInt("Default window width", size.width );
		prefs.putInt("Default window height", size.height );
		
		Point location = this.mainFrame.getLocation();
		prefs.putInt("Default window location x", location.x);
		prefs.putInt("Default window location y", location.y);
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
}


/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */
