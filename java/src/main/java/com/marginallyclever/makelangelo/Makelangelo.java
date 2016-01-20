package com.marginallyclever.makelangelo;
/**
 * @(#)drawbotGUI.java drawbot application with GUI
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */


// io functions

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import com.marginallyclever.communications.MarginallyCleverConnection;
import com.marginallyclever.communications.MarginallyCleverConnectionManager;
import com.marginallyclever.communications.SerialConnectionManager;


// TODO while not drawing, in-app gcode editing with immediate visual feedback ? edge tracing ?
// TODO filters -> vectors, vector <-> gcode.

/**
 * @author danroyer
 * @author Peter Colapietro
 * @since 0.0.1?
 */
public final class Makelangelo
implements ActionListener, MakelangeloRobotListener, MakelangeloRobotSettingsListener {
	static final long serialVersionUID = 1L;

	/**
	 * software VERSION. Defined in src/resources/makelangelo.properties and uses Maven's resource filtering to update
	 * the VERSION based upon VERSION defined in POM.xml. In this way we only define the VERSION once and prevent
	 * violating DRY.
	 */
	public static final String VERSION = PropertiesFileHelper.getMakelangeloVersionPropertyValue();

	@SuppressWarnings("deprecation")
	private Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);

	private MarginallyCleverConnectionManager connectionManager;
	private MakelangeloRobot robot;
	public GCodeFile gCode;
	
	private Translator translator;
	public SoundSystem soundSystem;
	
	// GUI elements
	private JFrame mainframe = null;
	private JPanel contentPane;
	
	// Top of window
	private JMenuBar menuBar;
	// menubar > Makelangelo > preferences
	private JMenuItem buttonAdjustSounds, buttonAdjustGraphics, buttonAdjustLanguage, buttonExportMachinePreferences, buttonImportMachinePreferences, buttonResetMachinePreferences;
	// menubar > Makelangelo
	private JMenuItem buttonAbout, buttonCheckForUpdate, buttonExit;
	// menubar > connect
	private JMenuItem buttonRescan, buttonDisconnect;
	// menubar > view
	private JMenuItem buttonZoomIn, buttonZoomOut, buttonZoomToFit;

	/**
	 * buttons that represent connections to real robots attached to the computer.
	 */
	private JMenuItem[] buttonConnections;
	
	// main window layout
	private Splitter splitLeftRight;
	private Splitter splitUpDown;
	
	// OpenGL window
	private DrawPanel cameraViewPanel;
	// Context sensitive menu
	private MakelangeloRobotPanel robotControlPanel;
	// Bottom of window
	public LogPanel logPanel;

	
	public static void main(String[] argv) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Makelangelo();
			}
		});
	}


	public Makelangelo() {
		Log.clear();

		Translator.start();

		soundSystem = new SoundSystem();
		
		// create a robot and listen to it for important news
		robot = new MakelangeloRobot(translator);
		robot.addListener(this);
		robot.settings.addListener(this);
		
		gCode = new GCodeFile();
		
		connectionManager = new SerialConnectionManager(prefs);
		
		createAndShowGUI();
	}
	
	
	public void updateMachineConfig() {
		if (cameraViewPanel != null) {
			cameraViewPanel.updateMachineConfig();
			cameraViewPanel.zoomToFitPaper();
		}
	}
	
	
	public String getTempDestinationFile() {
		return System.getProperty("user.dir") + "/temp.ngc";
	}
	
	
	/**
	 * Adjust graphics preferences
	 */
	protected void adjustGraphics() {
		final Preferences graphics_prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

		final JPanel panel = new JPanel(new GridBagLayout());

		//final JCheckBox allow_metrics = new JCheckBox(String.valueOf("I want to add the distance drawn to the // total"));
		//allow_metrics.setSelected(allowMetrics);

		final JCheckBox show_pen_up = new JCheckBox(Translator.get("MenuGraphicsPenUp"));
		final JCheckBox antialias_on = new JCheckBox(Translator.get("MenuGraphicsAntialias"));
		final JCheckBox speed_over_quality = new JCheckBox(Translator.get("MenuGraphicsSpeedVSQuality"));
		final JCheckBox draw_all_while_running = new JCheckBox(Translator.get("MenuGraphicsDrawWhileRunning"));

		show_pen_up.setSelected(graphics_prefs.getBoolean("show pen up", false));
		antialias_on.setSelected(graphics_prefs.getBoolean("antialias", true));
		speed_over_quality.setSelected(graphics_prefs.getBoolean("speed over quality", true));
		draw_all_while_running.setSelected(graphics_prefs.getBoolean("Draw all while running", true));

		GridBagConstraints c = new GridBagConstraints();
		//c.gridwidth=4;  c.gridx=0;  c.gridy=0;  driver.add(allow_metrics,c);

		int y = 0;

		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		panel.add(show_pen_up, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		panel.add(draw_all_while_running, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		panel.add(antialias_on, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		panel.add(speed_over_quality, c);
		y++;
		
		int result = JOptionPane.showConfirmDialog(this.mainframe, panel, Translator.get("MenuGraphicsTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			//allowMetrics = allow_metrics.isSelected();
			graphics_prefs.putBoolean("show pen up", show_pen_up.isSelected());
			graphics_prefs.putBoolean("antialias", antialias_on.isSelected());
			graphics_prefs.putBoolean("speed over quality", speed_over_quality.isSelected());
			graphics_prefs.putBoolean("Draw all while running", draw_all_while_running.isSelected());

			cameraViewPanel.setShowPenUp(show_pen_up.isSelected());
		}
	}


	/**
	 * Take the next line from the file and send it to the robot, if permitted.
	 */
	public void sendFileCommand() {
		if (robot.isRunning() == false 
				|| robot.isPaused() == true 
				|| gCode.isFileOpened() == false 
				|| (robot.getConnection() != null && robot.isPortConfirmed() == false) )
			return;

		String line;

		// are there any more commands?
		if( gCode.moreLinesAvailable() == false )  {
			// end of file
			// stop robot
			halt();
			// bask in the glory
			int x = gCode.getLinesTotal();
			if(robotControlPanel!=null) robotControlPanel.statusBar.setProgress(x, x);
			cameraViewPanel.setLinesProcessed(x);
			
			soundSystem.playDrawingFinishedSound();
			
			return;
		}
		
		int lineNumber = gCode.getLinesProcessed();
		line = gCode.nextLine();

		if (line.length() > 3) {
			line = "N" + lineNumber + " " + line;
			line += robot.generateChecksum(line);
		}
		robot.tweakAndSendLine( line, translator );

		cameraViewPanel.setLinesProcessed(lineNumber);
		if(robotControlPanel!=null) robotControlPanel.statusBar.setProgress(lineNumber, gCode.getLinesTotal());
		// loop until we find a line that gets sent to the robot, at which point we'll
		// pause for the robot to respond.  Also stop at end of file.
	}


	/**
	 * stop sending file commands to the robot.
	 * TODO add an e-stop command?
	 */
	public void halt() {
		robot.setRunning(false);
		robot.unPause();
		cameraViewPanel.setLinesProcessed(0);
		cameraViewPanel.setRunning(false);
		updateMenuBar();
		
	}

	public void startAt(long lineNumber) {
		gCode.setLinesProcessed(0);
		robot.sendLineToRobot("M110 N" + gCode.getLinesProcessed());
		cameraViewPanel.setLinesProcessed(gCode.getLinesProcessed());
		startDrawing();
	}

	private void startDrawing() {
		robot.unPause();
		robot.setRunning(true);
		cameraViewPanel.setRunning(true);
		updateMenuBar();
		if(robotControlPanel!=null) robotControlPanel.statusBar.start();
		sendFileCommand();
	}

	// The user has done something.  respond to it.
	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if (subject == buttonZoomIn) {
			cameraViewPanel.zoomIn();
			return;
		}
		if (subject == buttonZoomOut) {
			cameraViewPanel.zoomOut();
			return;
		}
		if (subject == buttonZoomToFit) {
			cameraViewPanel.zoomToFitPaper();
			return;
		}
		if (subject == buttonRescan) {
			connectionManager.listConnections();
			updateMenuBar();
			return;
		}
		if (subject == buttonDisconnect) {
			disconnect();
			return;
		}
		if (subject == buttonAdjustSounds) {
			soundSystem.adjust(this.mainframe,translator);
			return;
		}
		if (subject == buttonAdjustGraphics) {
			adjustGraphics();
			return;
		}
		if (subject == buttonAdjustLanguage) {
			Translator.chooseLanguage();
			updateMenuBar();
			return;
		}
		if (subject == buttonExportMachinePreferences) {
			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showSaveDialog(this.mainframe);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				final File file = fc.getSelectedFile();
				try (final OutputStream fileOutputStream = new FileOutputStream(file)) {
					prefs.exportSubtree(fileOutputStream);
				} catch (IOException | BackingStoreException pe) {
					Log.error(pe.getMessage());
				}
			}
			return;
		}
		if (subject == buttonImportMachinePreferences) {
			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(this.mainframe);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				final File file = fc.getSelectedFile();
				try (final InputStream fileInputStream = new FileInputStream(file)) {
					prefs.flush();
					Preferences.importPreferences(fileInputStream);
					prefs.flush();
				} catch (IOException | InvalidPreferencesFormatException | BackingStoreException pe) {
					Log.error(pe.getMessage());
				}
			}
			return;
		}
		if (subject == buttonResetMachinePreferences) {
			int dialogResult = JOptionPane.showConfirmDialog(this.mainframe, Translator.get("MenuResetMachinePreferencesWarning"), Translator.get("MenuResetMachinePreferencesWarningHeader"), JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION){
				try {
					prefs.removeNode();
					Preferences.userRoot().flush();
				} catch (BackingStoreException e1) {
					Log.error(e1.getMessage());
				}
			}
			return;
		}
		if (subject == buttonAbout) {
			DialogAbout about = new DialogAbout();
			about.display(translator,Makelangelo.VERSION);
			return;
		}
		if (subject == buttonCheckForUpdate) {
			checkForUpdate();
			return;
		}

		if (subject == buttonExit) {
			System.exit(0);
			return;
		}

		// Connecting to a machine
		String[] connections = connectionManager.listConnections();
		for (int i = 0; i < connections.length; ++i) {
			if (subject == buttonConnections[i]) {
				logPanel.clearLog();
				Log.message(Translator.get("ConnectingTo") + connections[i] + "...");

				MarginallyCleverConnection c = connectionManager.openConnection(connections[i]); 
				if (c == null) {
					Log.error(Translator.get("PortOpenFailed"));
				} else {
					robot.setConnection(c);
					Log.message( Translator.get("PortOpened") );
					updateMenuBar();
					soundSystem.playConnectSound();
				}
				return;
			}
		}
	}
	
	private void disconnect() {
		robot.getConnection().closeConnection();
		robot.setConnection(null);
		cameraViewPanel.setConnected(false);
		robotControlPanel.updateMachineNumberPanel();
		updateMenuBar();
		soundSystem.playDisconnectSound();

		// remove machine name from title
		mainframe.setTitle(Translator.get("TitlePrefix"));
	}
	
	
	/**
	 * If the menu bar exists, empty it.  If it doesn't exist, create it.
	 * @return the refreshed menu bar
	 */
	public JMenuBar createMenuBar() {
		menuBar = new JMenuBar();

		updateMenuBar();

		return menuBar;
	}

	/**
	 * Parse https://github.com/MarginallyClever/Makelangelo/releases/latest redirect notice
	 * to find the latest release tag.
	 */
	public void checkForUpdate() {
		try {
			URL github = new URL("https://github.com/MarginallyClever/Makelangelo/releases/latest");
			HttpURLConnection conn = (HttpURLConnection) github.openConnection();
			conn.setInstanceFollowRedirects(false);  //you still need to handle redirect manully.
			HttpURLConnection.setFollowRedirects(false);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String inputLine;
			if ((inputLine = in.readLine()) != null) {
				// parse the URL in the text-only redirect
				String matchStart = "<a href=\"";
				String matchEnd = "\">";
				int start = inputLine.indexOf(matchStart);
				int end = inputLine.indexOf(matchEnd);
				if (start != -1 && end != -1) {
					inputLine = inputLine.substring(start + matchStart.length(), end);
					// parse the last part of the redirect URL, which contains the release tag (which is the VERSION)
					inputLine = inputLine.substring(inputLine.lastIndexOf("/") + 1);

					System.out.println("last release: " + inputLine);
					System.out.println("your VERSION: " + VERSION);
					//System.out.println(inputLine.compareTo(VERSION));

					if (inputLine.compareTo(VERSION) > 0) {
						JOptionPane.showMessageDialog(null, Translator.get("UpdateNotice"));
					} else {
						JOptionPane.showMessageDialog(null, Translator.get("UpToDate"));
					}
				}
			} else {
				throw new Exception();
			}
			in.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, Translator.get("UpdateCheckFailed"));
		}
	}

	/**
	 * Rebuild the contents of the menu based on current program state
	 */
	public void updateMenuBar() {
		JMenu menu, preferencesSubMenu;
		ButtonGroup group;
		int i;

		if (robotControlPanel != null) {
			robotControlPanel.updateButtonAccess(robot.isPortConfirmed(), robot.isRunning());
		}

		menuBar.removeAll();

		// File menu
		menu = new JMenu(Translator.get("MenuMakelangelo"));
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		preferencesSubMenu = getPreferencesSubMenu();

		menu.add(preferencesSubMenu);

		buttonCheckForUpdate = new JMenuItem(Translator.get("MenuUpdate"), KeyEvent.VK_U);
		buttonCheckForUpdate.addActionListener(this);
		buttonCheckForUpdate.setEnabled(true);
		menu.add(buttonCheckForUpdate);

		buttonAbout = new JMenuItem(Translator.get("MenuAbout"), KeyEvent.VK_A);
		buttonAbout.addActionListener(this);
		menu.add(buttonAbout);

		menu.addSeparator();

		buttonExit = new JMenuItem(Translator.get("MenuQuit"), KeyEvent.VK_Q);
		buttonExit.addActionListener(this);
		menu.add(buttonExit);


		// Connect menu
		preferencesSubMenu = new JMenu(Translator.get("MenuConnect"));
		preferencesSubMenu.setEnabled(!robot.isRunning());
		group = new ButtonGroup();

		String[] connections = connectionManager.listConnections();
		buttonConnections = new JRadioButtonMenuItem[connections.length];
		for (i = 0; i < connections.length; ++i) {
			buttonConnections[i] = new JRadioButtonMenuItem(connections[i]);
			if (robot.getConnection() != null 
					&& robot.getConnection().isOpen() 
					&& robot.getConnection().getRecentConnection().equals(connections[i]) ) {
				buttonConnections[i].setSelected(true);
			}
			buttonConnections[i].addActionListener(this);
			group.add(buttonConnections[i]);
			preferencesSubMenu.add(buttonConnections[i]);
		}

		preferencesSubMenu.addSeparator();

		buttonRescan = new JMenuItem(Translator.get("MenuRescan"), KeyEvent.VK_N);
		buttonRescan.addActionListener(this);
		preferencesSubMenu.add(buttonRescan);

		buttonDisconnect = new JMenuItem(Translator.get("MenuDisconnect"), KeyEvent.VK_D);
		buttonDisconnect.addActionListener(this);
		buttonDisconnect.setEnabled(robot.getConnection() != null && robot.getConnection().isOpen());
		preferencesSubMenu.add(buttonDisconnect);

		menuBar.add(preferencesSubMenu);

		// view menu
		menu = new JMenu(Translator.get("MenuPreview"));
		buttonZoomOut = new JMenuItem(Translator.get("ZoomOut"));
		buttonZoomOut.addActionListener(this);
		buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.ALT_MASK));
		menu.add(buttonZoomOut);

		buttonZoomIn = new JMenuItem(Translator.get("ZoomIn"), KeyEvent.VK_EQUALS);
		buttonZoomIn.addActionListener(this);
		buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.ALT_MASK));
		menu.add(buttonZoomIn);

		buttonZoomToFit = new JMenuItem(Translator.get("ZoomFit"));
		buttonZoomToFit.addActionListener(this);
		menu.add(buttonZoomToFit);

		menuBar.add(menu);

		// finish
		menuBar.updateUI();
	}

	private JMenu getPreferencesSubMenu() {
		final JMenu preferencesSubMenu;
		preferencesSubMenu = new JMenu(Translator.get("MenuPreferences"));

		buttonAdjustSounds = initializeSubMenuButton(preferencesSubMenu, "MenuSoundsTitle");
		buttonAdjustGraphics = initializeSubMenuButton(preferencesSubMenu, "MenuGraphicsTitle");
		buttonAdjustLanguage = initializeSubMenuButton(preferencesSubMenu, "MenuLanguageTitle");
		preferencesSubMenu.add(new JSeparator());
		buttonExportMachinePreferences = initializeSubMenuButton(preferencesSubMenu, "Save");
		buttonImportMachinePreferences = initializeSubMenuButton(preferencesSubMenu, "Load");
		buttonResetMachinePreferences = initializeSubMenuButton(preferencesSubMenu, "MenuResetMachinePreferences");

		return preferencesSubMenu;
	}

	private JMenuItem initializeSubMenuButton(JMenu preferencesSubMenu, String translationKey) {
		final JMenuItem jMenuItem = new JMenuItem(Translator.get(translationKey));
		jMenuItem.addActionListener(this);
		preferencesSubMenu.add(jMenuItem);
		return jMenuItem;
	}

	public Container createContentPane() {
		//Create the content-pane-to-be.
		contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);

		cameraViewPanel = new DrawPanel();
		cameraViewPanel.setMachine(robot.settings);
		cameraViewPanel.setGCode(gCode);

		robotControlPanel = new MakelangeloRobotPanel(this, translator, robot);
		robotControlPanel.updateButtonAccess(false, false);
		
		logPanel = new LogPanel(translator, robot);
		logPanel.clearLog();

		// major layout
		splitLeftRight = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
		splitLeftRight.add(cameraViewPanel);
		splitLeftRight.add(robotControlPanel);

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


	public JFrame getParentFrame() {
		return this.mainframe;
	}


	// Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
	private void createAndShowGUI() {
		// Create and set up the window.
		this.mainframe = new JFrame(Translator.get("TitlePrefix"));
		this.mainframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		this.mainframe.setJMenuBar(createMenuBar());
		this.mainframe.setContentPane(createContentPane());

		// Display the window.
		int width = prefs.getInt("Default window width", (int) (1200.0));
		int height = prefs.getInt("Default window height", (int) (1020.0));
		this.mainframe.setSize(width, height);
		// center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.mainframe.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
		// show it
		this.mainframe.setVisible(true);

		cameraViewPanel.zoomToFitPaper();

		// 2015-05-03: option is meaningless, robot.connectionToRobot doesn't exist when software starts.
		// if(prefs.getBoolean("Reconnect to last port on start", false)) robot.connectionToRobot.reconnect();
		if (prefs.getBoolean("Check for updates", false)) checkForUpdate();
	}

	/**
	 * @return the <code>javax.swing.JFrame</code> representing the main frame of this GUI.
	 */
	public JFrame getMainframe() {
		return this.mainframe;
	}

	/**
	 * @return the <code>com.marginallyclever.makelangelo.DrawPanel</code> representing the preview pane of this GUI.
	 */
	public DrawPanel getDrawPanel() {
		return cameraViewPanel;
	}

	/**
	 * @return the <code>GCodeFile</code> representing the G-Code file used by this GUI.
	 */
	public GCodeFile getGcodeFile() {
		return gCode;
	}
	
	/**
	 * Notice received from MakelangeloRobot when serial connection is confirmed to point at something that speaks Makelangelo-firmware. 
	 */
	public void portConfirmed(MakelangeloRobot r) {
		// we shouldn't need to open the dialog if the default settings are correct.
		// the default settings must always match the values in the Marginally Clever tutorials.
		// the default settings must always match the Marginally Clever kit.
		/* if(r.settings.getLimitBottom()==0 
				&& r.settings.getLimitTop()==0
				&& r.settings.getLimitLeft()==0
				&& r.settings.getLimitRight()==0) {
			MakelangeloSettingsDialog m = new MakelangeloSettingsDialog(this,translator,r);
			m.run();
		}*/
		
	    getMainframe().setTitle(Translator.get("TitlePrefix") + " #" + Long.toString(robot.settings.getUID()));
	    
	    getDrawPanel().updateMachineConfig();
	    getDrawPanel().setConnected(true);

	    robotControlPanel.updateMachineNumberPanel();
		
	    updateMenuBar();
	}
	
	public void dataAvailable(MakelangeloRobot r,String data) {
		if(data.endsWith("\n")) data = data.substring(0, data.length()-1);
		Log.write( "#ffa500",data );  // #ffa500 = orange
	}
	
	public void connectionReady(MakelangeloRobot r) {
		sendFileCommand();
	}
	
	public void lineError(MakelangeloRobot r,int lineNumber) {
        getGcodeFile().setLinesProcessed(lineNumber);
        sendFileCommand();
	}
	
	public void settingsChangedEvent(MakelangeloRobotSettings settings) {
		getDrawPanel().repaint();
	}
	
	public void fileFinished(MakelangeloRobot r) {
		
	}
}


/**
 * This file is part of DrawbotGUI.
 * <p>
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */
