package com.marginallyclever.makelangelo;
/**
 * @(#)drawbotGUI.java drawbot application with GUI
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */


// io functions

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marginallyclever.communications.MarginallyCleverConnection;
import com.marginallyclever.communications.MarginallyCleverConnectionManager;
import com.marginallyclever.communications.SerialConnectionManager;
import com.marginallyclever.makelangelo.settings.MakelangeloSettingsDialog;


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
	
	// GUI elements
	private JFrame mainframe = null;
	// Top of window
	private JMenuBar menuBar;
	private JMenuItem buttonExit;
	private JMenuItem buttonAdjustSounds, buttonAdjustGraphics, buttonAdjustLanguage, buttonExportMachinePreferences, buttonImportMachinePreferences, buttonResetMachinePreferences;
	private JMenuItem buttonRescan, buttonDisconnect;
	private JMenuItem buttonZoomIn, buttonZoomOut, buttonZoomToFit;
	private JMenuItem buttonAbout, buttonCheckForUpdate;

	/**
	 * buttons that represent connections to real robots attached to the computer.
	 */
	private JMenuItem[] buttonConnections;
	
	// main window layout
	private Splitter splitLeftRight;
	// OpenGL window
	private DrawPanel drawPanel;
	// Context sensitive menu
	private JTabbedPane contextMenu;
	// Menu tabs
	private PanelPrepareImage prepareImage;
	private MakelangeloDriveControls driveControls;
	private PanelLog logPanel;
	// Bottom of window
	public StatusBar statusBar;

	/**
	 * logging
	 * @see org.slf4j.Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(Makelangelo.class);


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
		startLog();
		startTranslator();
		gCode = new GCodeFile();
		robot = new MakelangeloRobot(translator);
		robot.addListener(this);
		robot.settings.addListener(this);
		connectionManager = new SerialConnectionManager(prefs);
		createAndShowGUI();
	}



	public void startLog() {
		Path p = FileSystems.getDefault().getPath("log.html");
		try {
			Files.deleteIfExists(p);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// print starting time
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log("<h3>" + sdf.format(cal.getTime()) + "</h3>\n");
	}


	// appends a message to the log tab and system out.
	public void log(String msg) {
		try (Writer fileWriter = new FileWriter("log.html", true)) {
			PrintWriter logToFile = new PrintWriter(fileWriter);
			logToFile.write(msg);
			logToFile.flush();
		} catch (IOException e) {
			logger.error("{}", e);
		}
		
		if(logPanel!=null) {
			logPanel.log(msg);
		}		
	}

	public void clearLog() {
		logPanel.clearLog();
	}
	
	
	public void startTranslator() {
		translator = new Translator();
		if (translator.isThisTheFirstTimeLoadingLanguageFiles()) {
			chooseLanguage();
		}
	}

	// display a dialog box of available languages and let the user select their preference.
	public void chooseLanguage() {
		JPanel panel = new JPanel(new GridBagLayout());

		final String[] languageList = translator.getLanguageList();
		final JComboBox<String> languageOptions = new JComboBox<>(languageList);
		int currentIndex = translator.getCurrentLanguageIndex();
		languageOptions.setSelectedIndex(currentIndex);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(languageOptions, c);

		int result;
		do {
			result = JOptionPane.showConfirmDialog(this.mainframe, panel, "Language", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE);
		} while(result != JOptionPane.OK_OPTION);
		
		translator.setCurrentLanguage(languageList[languageOptions.getSelectedIndex()]);
		translator.saveConfig();
	}


	public void raisePen() {
		sendLineToRobot("G00 Z" + robot.settings.getPenUpString());
		prepareImage.raisePen();
	}

	public void lowerPen() {
		sendLineToRobot("G00 Z" + robot.settings.getPenDownString());
		prepareImage.lowerPen();
	}

	public void updateMachineConfig() {
		if (drawPanel != null) {
			drawPanel.updateMachineConfig();
			drawPanel.zoomToFitPaper();
		}
	}

	private void playSound(String url) {
		if (url.isEmpty()) return;

		try {
			Clip clip = AudioSystem.getClip();
			BufferedInputStream x = new BufferedInputStream(new FileInputStream(url));
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(x);
			clip.open(inputStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}

	public void playConnectSound() {
		playSound(prefs.get("sound_connect", ""));
	}

	private void playDisconnectSound() {
		playSound(prefs.get("sound_disconnect", ""));
	}

	public void playConversionFinishedSound() {
		playSound(prefs.get("sound_conversion_finished", ""));
	}

	private void playDrawingFinishedSound() {
		playSound(prefs.get("sound_drawing_finished", ""));
	}


	public String getTempDestinationFile() {
		return System.getProperty("user.dir") + "/temp.ngc";
	}


	private String selectFile() {
		JFileChooser choose = new JFileChooser();
		int returnVal = choose.showOpenDialog(this.mainframe);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = choose.getSelectedFile();
			return file.getAbsolutePath();
		} else {
			//System.out.println("File access cancelled by user.");
			return "";
		}
	}

	/**
	 * Adjust sound preferences
	 */
	protected void adjustSounds() {
		final JDialog driver = new JDialog(this.mainframe, translator.get("MenuSoundsTitle"), true);
		driver.setLayout(new GridBagLayout());
		
		final JTextField sound_connect = new JTextField(prefs.get("sound_connect", ""), 32);
		final JTextField sound_disconnect = new JTextField(prefs.get("sound_disconnect", ""), 32);
		final JTextField sound_conversion_finished = new JTextField(prefs.get("sound_conversion_finished", ""), 32);
		final JTextField sound_drawing_finished = new JTextField(prefs.get("sound_drawing_finished", ""), 32);

		final JButton change_sound_connect = new JButton(translator.get("MenuSoundsConnect"));
		final JButton change_sound_disconnect = new JButton(translator.get("MenuSoundsDisconnect"));
		final JButton change_sound_conversion_finished = new JButton(translator.get("MenuSoundsFinishConvert"));
		final JButton change_sound_drawing_finished = new JButton(translator.get("MenuSoundsFinishDraw"));

		//final JCheckBox allow_metrics = new JCheckBox(String.valueOf("I want to add the distance drawn to the // total"));
		//allow_metrics.setSelected(allowMetrics);

		final JButton cancel = new JButton(translator.get("Cancel"));
		final JButton save = new JButton(translator.get("Save"));

		GridBagConstraints c = new GridBagConstraints();
		//c.gridwidth=4;  c.gridx=0;  c.gridy=0;  driver.add(allow_metrics,c);

		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 3;
		driver.add(change_sound_connect, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 3;
		driver.add(sound_connect, c);
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 4;
		driver.add(change_sound_disconnect, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 4;
		driver.add(sound_disconnect, c);
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 5;
		driver.add(change_sound_conversion_finished, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 5;
		driver.add(sound_conversion_finished, c);
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 6;
		driver.add(change_sound_drawing_finished, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 6;
		driver.add(sound_drawing_finished, c);

		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 2;
		c.gridy = 12;
		driver.add(save, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 3;
		c.gridy = 12;
		driver.add(cancel, c);

		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();
				if (subject == change_sound_connect) sound_connect.setText(selectFile());
				if (subject == change_sound_disconnect) sound_disconnect.setText(selectFile());
				if (subject == change_sound_conversion_finished) sound_conversion_finished.setText(selectFile());
				if (subject == change_sound_drawing_finished) sound_drawing_finished.setText(selectFile());

				if (subject == save) {
					//allowMetrics = allow_metrics.isSelected();
					prefs.put("sound_connect", sound_connect.getText());
					prefs.put("sound_disconnect", sound_disconnect.getText());
					prefs.put("sound_conversion_finished", sound_conversion_finished.getText());
					prefs.put("sound_drawing_finished", sound_drawing_finished.getText());
					robot.settings.saveConfig();
					driver.dispose();
				}
				if (subject == cancel) {
					driver.dispose();
				}
			}
		};

		change_sound_connect.addActionListener(driveButtons);
		change_sound_disconnect.addActionListener(driveButtons);
		change_sound_conversion_finished.addActionListener(driveButtons);
		change_sound_drawing_finished.addActionListener(driveButtons);

		save.addActionListener(driveButtons);
		cancel.addActionListener(driveButtons);
		driver.getRootPane().setDefaultButton(save);
		driver.pack();
		driver.setVisible(true);
	}


	/**
	 * Adjust graphics preferences
	 */
	protected void adjustGraphics() {
		final Preferences graphics_prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

		final JPanel driver = new JPanel(new GridBagLayout());

		//final JCheckBox allow_metrics = new JCheckBox(String.valueOf("I want to add the distance drawn to the // total"));
		//allow_metrics.setSelected(allowMetrics);

		final JCheckBox show_pen_up = new JCheckBox(translator.get("MenuGraphicsPenUp"));
		final JCheckBox antialias_on = new JCheckBox(translator.get("MenuGraphicsAntialias"));
		final JCheckBox speed_over_quality = new JCheckBox(translator.get("MenuGraphicsSpeedVSQuality"));
		final JCheckBox draw_all_while_running = new JCheckBox(translator.get("MenuGraphicsDrawWhileRunning"));

		show_pen_up.setSelected(graphics_prefs.getBoolean("show pen up", false));
		antialias_on.setSelected(graphics_prefs.getBoolean("antialias", true));
		speed_over_quality.setSelected(graphics_prefs.getBoolean("speed over quality", true));
		draw_all_while_running.setSelected(graphics_prefs.getBoolean("Draw all while running", true));

		final JButton cancel = new JButton(translator.get("Cancel"));
		final JButton save = new JButton(translator.get("Save"));

		GridBagConstraints c = new GridBagConstraints();
		//c.gridwidth=4;  c.gridx=0;  c.gridy=0;  driver.add(allow_metrics,c);

		int y = 0;

		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		driver.add(show_pen_up, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		driver.add(draw_all_while_running, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		driver.add(antialias_on, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		driver.add(speed_over_quality, c);
		y++;

		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 2;
		c.gridy = y;
		driver.add(save, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 3;
		c.gridy = y;
		driver.add(cancel, c);
		
		int result = JOptionPane.showConfirmDialog(null, this.mainframe, translator.get("MenuGraphicsTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			//allowMetrics = allow_metrics.isSelected();
			graphics_prefs.putBoolean("show pen up", show_pen_up.isSelected());
			graphics_prefs.putBoolean("antialias", antialias_on.isSelected());
			graphics_prefs.putBoolean("speed over quality", speed_over_quality.isSelected());
			graphics_prefs.putBoolean("Draw all while running", draw_all_while_running.isSelected());

			drawPanel.setShowPenUp(show_pen_up.isSelected());
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
		do {
			// are there any more commands?
			if( gCode.moreLinesAvailable() == false ) break;
			
			int lineNumber = gCode.getLinesProcessed();
			line = gCode.nextLine();

			// catch pen up/down status here
			if (line.contains("Z" + robot.settings.getPenUpString())) {
				prepareImage.raisePen();
			}
			if (line.contains("Z" + robot.settings.getPenDownString())) {
				prepareImage.lowerPen();
			}


			if (line.length() > 3) {
				line = "N" + lineNumber + " " + line;
				line += robot.generateChecksum(line);
			}

			drawPanel.setLinesProcessed(lineNumber);
			statusBar.setProgress(lineNumber, gCode.getLinesTotal());
			// loop until we find a line that gets sent to the robot, at which point we'll
			// pause for the robot to respond.  Also stop at end of file.
		} while ( tweakAndSendLine(line) );

		if (gCode.moreLinesAvailable() == false) {
			// end of file
			playDrawingFinishedSound();
			halt();
			sayHooray();
		}
	}


	private void sayHooray() {
		JOptionPane.showMessageDialog(null,
				translator.get("Finished") + " " +
						gCode.getLinesProcessed() +
						translator.get("LineSegments") + "\n" +
						statusBar.getElapsed() + "\n" +
						translator.get("SharePromo")
				);
	}


	private void changeToTool(String changeToolString) {
		int i = Integer.decode(changeToolString);

		String[] toolNames = robot.settings.getToolNames();

		if (i < 0 || i > toolNames.length) {
			log("<span style='color:red'>" + translator.get("InvalidTool") + i + "</span>");
			i = 0;
		}
		JOptionPane.showMessageDialog(null, translator.get("ChangeToolPrefix") + toolNames[i] + translator.get("ChangeToolPostfix"));
	}


	/**
	 * removes comments, processes commands drawbot shouldn't have to handle.
	 *
	 * @param line command to send
	 * @return true if the robot is ready for another command to be sent.
	 */
	public boolean tweakAndSendLine(String line) {
		if (robot.getConnection() == null || !robot.isPortConfirmed() || !robot.isRunning()) return false;

		// tool change request?
		String[] tokens = line.split("(\\s|;)");

		// tool change?
		if (Arrays.asList(tokens).contains("M06") || Arrays.asList(tokens).contains("M6")) {
			for (String token : tokens) {
				if (token.startsWith("T")) {
					changeToTool(token.substring(1));
				}
			}
		}

		// end of program?
		if (Objects.equals(tokens[0], "M02") || Objects.equals(tokens[0], "M2") || Objects.equals(tokens[0], "M30")) {
			playDrawingFinishedSound();
			halt();
			return false;
		}


		// send relevant part of line to the robot
		sendLineToRobot(line);

		return false;
	}


	/**
	 * Sends a single command the robot.  Could be anything.
	 *
	 * @param line command to send.
	 * @return <code>true</code> if command was sent to the robot; <code>false</code> otherwise.
	 */
	public boolean sendLineToRobot(String line) {
		if (robot.getConnection() == null || !robot.isPortConfirmed()) return false;

		if (line.trim().equals("")) return false;
		String reportedline = line;
		// does it have a checksum?  hide it in the log
		if (reportedline.contains(";")) {
			String[] lines = line.split(";");
			reportedline = lines[0];
		}
		if(reportedline.trim().equals("")) return false;
				
		log("<font color='0xFFFFFF'>" + reportedline + "</font>");
		line += "\n";

		// send unmodified line
		try {
			robot.getConnection().sendMessage(line);
		} catch (Exception e) {
			log("<font color='FF0000'>" + e.getMessage() + "</font>");
			return false;
		}
		return true;
	}

	/**
	 * stop sending file commands to the robot.
	 * TODO add an e-stop command?
	 */
	public void halt() {
		robot.setRunning(false);
		robot.unPause();
		drawPanel.setLinesProcessed(0);
		drawPanel.setRunning(false);
		updateMenuBar();
	}

	public void startAt(long lineNumber) {
		gCode.setLinesProcessed(0);
		sendLineToRobot("M110 N" + gCode.getLinesProcessed());
		drawPanel.setLinesProcessed(gCode.getLinesProcessed());
		startDrawing();
	}

	private void startDrawing() {
		robot.unPause();
		robot.setRunning(true);
		drawPanel.setRunning(true);
		updateMenuBar();
		statusBar.start();
		sendFileCommand();
	}

	// The user has done something.  respond to it.
	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if (subject == buttonZoomIn) {
			drawPanel.zoomIn();
			return;
		}
		if (subject == buttonZoomOut) {
			drawPanel.zoomOut();
			return;
		}
		if (subject == buttonZoomToFit) {
			drawPanel.zoomToFitPaper();
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
			adjustSounds();
			return;
		}
		if (subject == buttonAdjustGraphics) {
			adjustGraphics();
			return;
		}
		if (subject == buttonAdjustLanguage) {
			chooseLanguage();
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
					logger.error("{}", pe.getMessage());
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
					logger.error("{}", pe.getMessage());
				}
			}
			return;
		}
		if (subject == buttonResetMachinePreferences) {
			int dialogResult = JOptionPane.showConfirmDialog(this.mainframe, translator.get("MenuResetMachinePreferencesWarning"), translator.get("MenuResetMachinePreferencesWarningHeader"), JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION){
				try {
					prefs.removeNode();
					Preferences.userRoot().flush();
				} catch (BackingStoreException e1) {
					logger.error("{}", e1.getMessage());
				}
			}
			return;
		}
		if (subject == buttonAbout) {
			displayAbout();
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

		String[] connections = connectionManager.listConnections();
		for (int i = 0; i < connections.length; ++i) {
			if (subject == buttonConnections[i]) {
				clearLog();
				log("<font color='green'>" + translator.get("ConnectingTo") + connections[i] + "...</font>\n");

				MarginallyCleverConnection c = connectionManager.openConnection(connections[i]); 
				if (c == null) {
					log("<span style='color:red'>" + translator.get("PortOpenFailed") + "</span>\n");
				} else {
					robot.setConnection(c);
					log("<span style='color:green'>" + translator.get("PortOpened") + "</span>\n");
					updateMenuBar();
					playConnectSound();
				}
				return;
			}
		}
	}
	
	private void disconnect() {
		robot.getConnection().closeConnection();
		robot.setConnection(null);
		drawPanel.setConnected(false);
		updateMenuBar();
		playDisconnectSound();

		// update window title
		this.mainframe.setTitle(translator.get("TitlePrefix")
				+ Long.toString(robot.settings.getUID())
				+ translator.get("TitleNotConnected"));
	}
	
	/**
	 * @return byte array containing data for image icon.
	 */
	private ImageIcon getImageIcon(String iconResourceName) {
		ImageIcon icon = null;
		try {
			final byte[] imageData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(iconResourceName));
			icon = new ImageIcon(imageData);
		} catch (NullPointerException | IOException e) {
			logger.error("Error getting image icon: {}", e);
		}
		return icon;
	}

	/**
	 * <p>
	 * Uses {@link java.lang.StringBuilder#append(String)} to create an internationalization supported {@code String}
	 * representing the About Message Dialog's HTML.
	 * </p>
	 * <p>
	 * <p>
	 * The summation of {@link String#length()} for each of the respective values retrieved with the
	 * {@code "AboutHTMLBeforeVersionNumber"}, and {@code "AboutHTMLAfterVersionNumber"} {@link Translator} keys,
	 * in conjunction with {@link Makelangelo#VERSION} is calculated for use with {@link java.lang.StringBuilder#StringBuilder(int)}.
	 * </p>
	 *
	 * @return An HTML string used for the About Message Dialog.
	 */
	private String getAboutHtmlFromMultilingualString() {
		final String aboutHtmlBeforeVersionNumber = translator.get("AboutHTMLBeforeVersionNumber");
		final String aboutHmlAfterVersionNumber = translator.get("AboutHTMLAfterVersionNumber");
		final int aboutHTMLBeforeVersionNumberLength = aboutHtmlBeforeVersionNumber.length();
		final int versionNumberStringLength = VERSION.length();
		final int aboutHtmlAfterVersionNumberLength = aboutHmlAfterVersionNumber.length();
		final int aboutHtmlStringBuilderCapacity = aboutHTMLBeforeVersionNumberLength + versionNumberStringLength + aboutHtmlAfterVersionNumberLength;
		final StringBuilder aboutHtmlStringBuilder = new StringBuilder(aboutHtmlStringBuilderCapacity);
		aboutHtmlStringBuilder.append(aboutHtmlBeforeVersionNumber);
		aboutHtmlStringBuilder.append(VERSION);
		aboutHtmlStringBuilder.append(aboutHmlAfterVersionNumber);
		return aboutHtmlStringBuilder.toString();
	}

	/**
	 * @param html String of valid HTML.
	 * @return a
	 */
	public JTextComponent createHyperlinkListenableJEditorPane(String html) {
		final JEditorPane bottomText = new JEditorPane();
		bottomText.setContentType("text/html");
		bottomText.setEditable(false);
		bottomText.setText(html);
		bottomText.setOpaque(false);
		final HyperlinkListener hyperlinkListener = new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
				if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(hyperlinkEvent.getURL().toURI());
						} catch (IOException | URISyntaxException exception) {
							// Auto-generated catch block
							exception.printStackTrace();
						}
					}

				}
			}
		};
		bottomText.addHyperlinkListener(hyperlinkListener);
		return bottomText;
	}


	/**
	 * Display the about dialog.
	 */
	private void displayAbout() {
		final String aboutHtml = getAboutHtmlFromMultilingualString();
		final JTextComponent bottomText = createHyperlinkListenableJEditorPane(aboutHtml);
		ImageIcon icon = getImageIcon("logo.png");
		final String menuAboutValue = translator.get("MenuAbout");
		if (icon == null) {
			icon = getImageIcon("resources/logo.png");
		}
		JOptionPane.showMessageDialog(null, bottomText, menuAboutValue, JOptionPane.INFORMATION_MESSAGE, icon);
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
						JOptionPane.showMessageDialog(null, translator.get("UpdateNotice"));
					} else {
						JOptionPane.showMessageDialog(null, translator.get("UpToDate"));
					}
				}
			} else {
				throw new Exception();
			}
			in.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, translator.get("UpdateCheckFailed"));
		}
	}

	/**
	 * Rebuild the contents of the menu based on current program state
	 */
	public void updateMenuBar() {
		JMenu menu, preferencesSubMenu;
		ButtonGroup group;
		int i;

		boolean isConfirmed = robot.getConnection() != null 
				&& robot.getConnection().isOpen()
				&& robot.isPortConfirmed();

		if (prepareImage != null) {
			prepareImage.updateButtonAccess(isConfirmed, robot.isRunning());
		}
		if (driveControls != null) {
			driveControls.updateButtonAccess(isConfirmed, robot.isRunning());
		}


		menuBar.removeAll();


		// File menu
		menu = new JMenu(translator.get("MenuMakelangelo"));
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		preferencesSubMenu = getPreferencesSubMenu();

		menu.add(preferencesSubMenu);

		buttonCheckForUpdate = new JMenuItem(translator.get("MenuUpdate"), KeyEvent.VK_U);
		buttonCheckForUpdate.addActionListener(this);
		buttonCheckForUpdate.setEnabled(true);
		menu.add(buttonCheckForUpdate);

		buttonAbout = new JMenuItem(translator.get("MenuAbout"), KeyEvent.VK_A);
		buttonAbout.addActionListener(this);
		menu.add(buttonAbout);

		menu.addSeparator();

		buttonExit = new JMenuItem(translator.get("MenuQuit"), KeyEvent.VK_Q);
		buttonExit.addActionListener(this);
		menu.add(buttonExit);


		// Connect menu
		preferencesSubMenu = new JMenu(translator.get("MenuConnect"));
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

		buttonRescan = new JMenuItem(translator.get("MenuRescan"), KeyEvent.VK_N);
		buttonRescan.addActionListener(this);
		preferencesSubMenu.add(buttonRescan);

		buttonDisconnect = new JMenuItem(translator.get("MenuDisconnect"), KeyEvent.VK_D);
		buttonDisconnect.addActionListener(this);
		buttonDisconnect.setEnabled(robot.getConnection() != null && robot.getConnection().isOpen());
		preferencesSubMenu.add(buttonDisconnect);

		menuBar.add(preferencesSubMenu);

		// view menu
		menu = new JMenu(translator.get("MenuPreview"));
		buttonZoomOut = new JMenuItem(translator.get("ZoomOut"));
		buttonZoomOut.addActionListener(this);
		buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.ALT_MASK));
		menu.add(buttonZoomOut);

		buttonZoomIn = new JMenuItem(translator.get("ZoomIn"), KeyEvent.VK_EQUALS);
		buttonZoomIn.addActionListener(this);
		buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.ALT_MASK));
		menu.add(buttonZoomIn);

		buttonZoomToFit = new JMenuItem(translator.get("ZoomFit"));
		buttonZoomToFit.addActionListener(this);
		menu.add(buttonZoomToFit);

		menuBar.add(menu);

		// finish
		menuBar.updateUI();
	}

	private JMenu getPreferencesSubMenu() {
		final JMenu preferencesSubMenu;
		preferencesSubMenu = new JMenu(translator.get("MenuPreferences"));

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
		final JMenuItem jMenuItem = new JMenuItem(translator.get(translationKey));
		jMenuItem.addActionListener(this);
		preferencesSubMenu.add(jMenuItem);
		return jMenuItem;
	}

	public Container createContentPane() {
		//Create the content-pane-to-be.
		final JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);

		logPanel = new PanelLog();
		logPanel.createPanel(this, translator, robot.settings);
		clearLog();

		drawPanel = new DrawPanel(robot.settings);
		drawPanel.setGCode(gCode);

		prepareImage = new PanelPrepareImage();
		prepareImage.createPanel(this, translator, robot);
		prepareImage.updateButtonAccess(false, false);

		driveControls = new MakelangeloDriveControls();
		driveControls.createPanel(this, translator, robot.settings);
		driveControls.updateButtonAccess(false, false);

		statusBar = new StatusBar(translator);

		contextMenu = new JTabbedPane();
		contextMenu.setPreferredSize(new Dimension(450,100));
		contextMenu.addTab(translator.get("MenuGCODE"), null, prepareImage, null);
		contextMenu.addTab(translator.get("MenuDraw"), null, driveControls, null);
		contextMenu.addTab(translator.get("MenuLog"), null, logPanel, null);

		// major layout
		splitLeftRight = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
		splitLeftRight.add(drawPanel);
		splitLeftRight.add(contextMenu);

		contentPane.add(statusBar, BorderLayout.SOUTH);
		contentPane.add(splitLeftRight, BorderLayout.CENTER);

		return contentPane;
	}


	public JFrame getParentFrame() {
		return this.mainframe;
	}


	// Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
	private void createAndShowGUI() {
		// Create and set up the window.
		this.mainframe = new JFrame("Makelangelo");
		this.mainframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		this.mainframe.setJMenuBar(createMenuBar());
		this.mainframe.setContentPane(createContentPane());

		// Display the window.
		int width = prefs.getInt("Default window width", (int) (1200.0));
		int height = prefs.getInt("Default window height", (int) (1020.0));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.mainframe.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
		this.mainframe.setSize(width, height);
		this.mainframe.setVisible(true);

		drawPanel.zoomToFitPaper();

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
		return drawPanel;
	}

	/**
	 * driveControls the <code>javax.swing.JPanel</code> representing the preview pane of this GUI.
	 */
	public void updatedriveControls() {
		driveControls.createPanel(this, translator, robot.settings);
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
		if(r.settings.getLimitBottom()==0 
				&& r.settings.getLimitTop()==0
				&& r.settings.getLimitLeft()==0
				&& r.settings.getLimitRight()==0) {
			MakelangeloSettingsDialog m = new MakelangeloSettingsDialog(this,translator,r);
			m.run();
		}
		
	    getMainframe().setTitle(translator.get("TitlePrefix")
	        + Long.toString(this.robot.settings.getUID())
	        + translator.get("TitlePostfix"));

	    robot.sendConfig();
	    
	    getDrawPanel().updateMachineConfig();
	    getDrawPanel().setConnected(true);

	    prepareImage.updateMachineNumberPanel();
	    
	    updateMenuBar();

	    // rebuild the drive pane so that the feed rates are correct.
	    updatedriveControls();
	}
	
	public void dataAvailable(MakelangeloRobot r,String data) {
		log("<font color='#ffa500'>"+data+"</font>");
	}
	
	public void connectionReady(MakelangeloRobot r) {
		sendFileCommand();
	}
	
	public void lineError(MakelangeloRobot r,int lineNumber) {
        getGcodeFile().setLinesProcessed(lineNumber);
        sendFileCommand();
	}
	
	public void settingsChanged(MakelangeloRobotSettings settings) {
		getDrawPanel().repaint();
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
