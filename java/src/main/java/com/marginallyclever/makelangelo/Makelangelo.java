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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
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
import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.util.Animator;
import com.marginallyclever.communications.MarginallyCleverConnectionManager;
import com.marginallyclever.communications.SerialConnectionManager;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotListener;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotPanel;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotSettings;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotSettingsListener;


/**
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

	private MarginallyCleverConnectionManager connectionManager;
	private MakelangeloRobot robot;
	
	private Translator translator;
	
	// GUI elements
	private JFrame mainFrame = null;
	private JPanel contentPane;
	
	// Top of window
	private JMenuBar menuBar;
	// file menu
	private JMenuItem buttonAbout, buttonCheckForUpdate, buttonExit;
	// preferences menu
	private JMenuItem buttonAdjustSounds, buttonAdjustGraphics, buttonAdjustLanguage, buttonExportMachinePreferences, buttonImportMachinePreferences, buttonResetMachinePreferences;
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
	
	private Animator animator;
	
	// Drag & drop support
	private MakelangeloTransferHandler myTransferHandler;
	
	
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
		SoundSystem.start();
		
		// create a robot and listen to it for important news
		robot = new MakelangeloRobot(translator);
		robot.addListener(this);
		robot.getSettings().addListener(this);
		
		myTransferHandler = new MakelangeloTransferHandler(robot);
		
		connectionManager = new SerialConnectionManager(prefs);
		
		createAndShowGUI();

		if (prefs.getBoolean("Check for updates", false)) checkForUpdate();
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
		
		int result = JOptionPane.showConfirmDialog(this.mainFrame, panel, Translator.get("MenuGraphicsTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			//allowMetrics = allow_metrics.isSelected();
			graphics_prefs.putBoolean("show pen up", show_pen_up.isSelected());
			graphics_prefs.putBoolean("antialias", antialias_on.isSelected());
			graphics_prefs.putBoolean("speed over quality", speed_over_quality.isSelected());
			graphics_prefs.putBoolean("Draw all while running", draw_all_while_running.isSelected());

			robot.setShowPenUp(show_pen_up.isSelected());
		}
	}

	
	// The user has done something.  respond to it.
	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if (subject == buttonZoomIn) drawPanel.zoomIn();
		if (subject == buttonZoomOut) drawPanel.zoomOut();
		if (subject == buttonZoomToFit) drawPanel.zoomToFitPaper();
		if (subject == buttonAdjustSounds) SoundSystem.adjust(this.mainFrame,translator);
		if (subject == buttonAdjustGraphics) adjustGraphics();
		if (subject == buttonAdjustLanguage) adjustLanguage();
		if (subject == buttonExportMachinePreferences) exportPreferences();
		if (subject == buttonImportMachinePreferences) importPreferences();
		if (subject == buttonResetMachinePreferences) resetPreferences();
		if (subject == buttonAbout) (new DialogAbout()).display(translator,Makelangelo.VERSION,this.mainFrame);
		if (subject == buttonCheckForUpdate) checkForUpdate();
		if (subject == buttonExit) onClose();
	}

	
	private void adjustLanguage() {
		Translator.chooseLanguage();
		// TODO replace all strings with strings from new language.
		
		if (robotPanel != null) robotPanel.updateButtonAccess();
	}

	
	private void exportPreferences() {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this.mainFrame);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			final File file = fc.getSelectedFile();
			try (final OutputStream fileOutputStream = new FileOutputStream(file)) {
				prefs.exportSubtree(fileOutputStream);
			} catch (IOException | BackingStoreException pe) {
				Log.error(pe.getMessage());
			}
		}
	}
	
	
	private void importPreferences() {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(this.mainFrame);
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
	}
	
	
	private void resetPreferences() {
		int dialogResult = JOptionPane.showConfirmDialog(this.mainFrame, Translator.get("MenuResetMachinePreferencesWarning"), Translator.get("MenuResetMachinePreferencesWarningHeader"), JOptionPane.YES_NO_OPTION);
		if(dialogResult == JOptionPane.YES_OPTION){
			try {
				prefs.removeNode();
				Preferences.userRoot().flush();
			} catch (BackingStoreException e1) {
				Log.error(e1.getMessage());
			}
		}
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
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);


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

		// preferences
		menuBar.add(createPreferencesSubMenu());

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

					System.out.println("latest release: " + inputLine+"; this version: " + VERSION);
					//System.out.println(inputLine.compareTo(VERSION));

					int comp = inputLine.compareTo(VERSION);
					String results;
					if     (comp>0) results = Translator.get("UpdateNotice");
					else if(comp<0) results = "This version is from the future?!";
					else 			results = Translator.get("UpToDate");

					JOptionPane.showMessageDialog(mainFrame, results);
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
		if (robotPanel != null) robotPanel.updateButtonAccess();
	}

	
	private JMenu createPreferencesSubMenu() {
		final JMenu preferencesSubMenu;
		preferencesSubMenu = new JMenu(Translator.get("MenuPreferences"));

		buttonAdjustSounds = createSubMenuItem(preferencesSubMenu, "MenuSoundsTitle");
		buttonAdjustGraphics = createSubMenuItem(preferencesSubMenu, "MenuGraphicsTitle");
		buttonAdjustLanguage = createSubMenuItem(preferencesSubMenu, "MenuLanguageTitle");
		preferencesSubMenu.add(new JSeparator());
		buttonExportMachinePreferences = createSubMenuItem(preferencesSubMenu, "Save");
		buttonImportMachinePreferences = createSubMenuItem(preferencesSubMenu, "Load");
		buttonResetMachinePreferences = createSubMenuItem(preferencesSubMenu, "MenuResetMachinePreferences");

		return preferencesSubMenu;
	}
	

	private JMenuItem createSubMenuItem(JMenu preferencesSubMenu, String translationKey) {
		final JMenuItem jMenuItem = new JMenuItem(Translator.get(translationKey));
		jMenuItem.addActionListener(this);
		preferencesSubMenu.add(jMenuItem);
		return jMenuItem;
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
		
		mainFrame.setTransferHandler(myTransferHandler);
		
		setupFrameRealEstate();
		mainFrame.setVisible(true);

		drawPanel.zoomToFitPaper();

		// start animation system        
        animator = new Animator();
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
		if( screenSize.width > 0 && screenSize.width < maxWidth ) {
			maxHeight *= screenSize.width / maxWidth;
			maxWidth = screenSize.width;
		}
		if( screenSize.height > 0 && screenSize.height < maxHeight ) {
			maxWidth *= screenSize.height / maxHeight;
			maxHeight = screenSize.height;
		}
			
		// set window size
		if(width > maxWidth || height > maxHeight ) {
			width = maxWidth;
			height = maxHeight;
			prefs.putInt("Default window width", maxWidth );
			prefs.putInt("Default window height", maxHeight );
		}
		
		mainFrame.setSize(width, height);
		
		// set window location
		// by default center the window.  Later use preferences.
		int defaultLocationX = (screenSize.width - width) / 2;
		int defaultLocationY = (screenSize.height - height) / 2;
		int locationX = prefs.getInt("Default window location x", defaultLocationX);
		int locationY = prefs.getInt("Default window location y", defaultLocationY);
		mainFrame.setLocation(locationX,locationY);
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
        	savePreferences();

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
	
	
	private void savePreferences() {
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
	public void windowClosed(WindowEvent e) {
		System.out.println("windowClosed");
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
