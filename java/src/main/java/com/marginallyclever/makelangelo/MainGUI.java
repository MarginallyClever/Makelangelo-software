package com.marginallyclever.makelangelo;
/**@(#)drawbotGUI.java
 *
 * drawbot application with GUI
 *
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */


// io functions
import com.marginallyclever.communications.MarginallyCleverConnection;
import com.marginallyclever.communications.MarginallyCleverConnectionManager;
import com.marginallyclever.communications.SerialConnectionManager;

import org.apache.commons.io.IOUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.Preferences;


// TODO while not drawing, in-app gcode editing with immediate visual feedback ?
// TODO image processing options - cutoff, exposure, resolution, edge tracing ?
// TODO filters > vector output, vector output > gcode.
/**
 * 
 * @author danroyer
 * @since 0.0.1?
 */
public class MainGUI
		extends JPanel
		implements ActionListener
{

	// Java required?
	static final long serialVersionUID=1L;

    /**
     * software version. Defined in src/resources/makelangelo.properties and uses Maven's resource filtering to update
	 * the version based upon version defined in POM.xml. In this way we only define the version once and prevent
	 * violating DRY.
     */
	public static final String version =
			PropertiesFileHelper.getMakelangeloVersionPropertyValue();;

	
	private Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MAKELANGELO_ROOT);
	
	private MarginallyCleverConnectionManager connectionManager;
	private MarginallyCleverConnection connectionToRobot=null;

	// GUI elements
	private static JFrame mainframe;
	private JMenuBar menuBar;
    private JMenuItem buttonExit;
    private JMenuItem buttonAdjustSounds, buttonAdjustGraphics, buttonAdjustLanguage;
    private JMenuItem buttonRescan, buttonDisconnect;
    private JMenuItem buttonZoomIn,buttonZoomOut,buttonZoomToFit;
    private JMenuItem buttonAbout,buttonCheckForUpdate;

    private JMenuItem [] buttonPorts;

    // logging
    private JTextPane log;
    private JScrollPane logPane;
    HTMLEditorKit kit;
    HTMLDocument doc;
    PrintWriter logToFile;

    // main window layout
    private Splitter split_left_right;
    // opengl window
    private DrawPanel drawPanel;
    // context sensitive menu
    private JTabbedPane contextMenu;
    // menu tabs
    private PrepareImagePanel prepareImage;
    private MakelangeloDriveControls driveControls;
	private MakelangeloSettingsPanel settingsPane;
	public StatusBar statusBar;
	
	// reading file
	private boolean isRunning = false;
	private boolean isPaused = true;
	public GCodeFile gCode = new GCodeFile();

	private MachineConfiguration machineConfiguration;
	private MultilingualSupport  translator;
	
	
	public MainGUI() {
		startLog();
		startTranslator();
		machineConfiguration = new MachineConfiguration(this,translator);
        connectionManager = new SerialConnectionManager(prefs, this, translator, machineConfiguration);
        createAndShowGUI();
//*
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = ge.getAllFonts();

        for (Font font : fonts) {
            System.out.print(font.getFontName() + " : ");
            System.out.println(font.getFamily());
        }
//*/
	}


	public void startTranslator() {
		translator = new MultilingualSupport();
		if(translator.isThisTheFirstTimeLoadingLanguageFiles()) {
			chooseLanguage();
		}
	}
	
	// display a dialog box of available languages and let the user select their preference.
	public void chooseLanguage() {
		final JDialog driver = new JDialog(mainframe,"Language",true);
		driver.setLayout(new GridBagLayout());

		final String [] languageList = translator.getLanguageList();
		final JComboBox<String> languageOptions = new JComboBox<>(languageList);
		final JButton save = new JButton(">>>");

		GridBagConstraints c = new GridBagConstraints();
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=2;	c.gridx=0;	c.gridy=0;	driver.add(languageOptions,c);
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=2;  c.gridy=0;  driver.add(save,c);
		
		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();
				// TODO prevent "close" icon.  Must press save to continue!
				if(subject == save) {
					translator.setCurrentLanguage(languageList[languageOptions.getSelectedIndex()]);
					translator.saveConfig();
					driver.dispose();
				}
			}
		};

		save.addActionListener(driveButtons);

		driver.pack();
		driver.setVisible(true);
	}
	

	public void raisePen() {
		sendLineToRobot("G00 Z"+machineConfiguration.getPenUpString());
		driveControls.raisePen();
	}
	
	public void lowerPen() {
		sendLineToRobot("G00 Z" + machineConfiguration.getPenDownString());
		driveControls.lowerPen();
	}
	
	public boolean isRunning() { return isRunning; }
	public boolean isPaused() { return isPaused; }
	
	protected void finalize() throws Throwable {
		//do finalization here
		endLog();
		super.finalize(); //not necessary if extending Object.
	} 
	
	private void startLog() {
		try {
			logToFile = new PrintWriter(new FileWriter("log.html"));
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			logToFile.write("<h3>"+sdf.format(cal.getTime())+"</h3>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void endLog() {
		logToFile.close();
	}

	
	public void updateMachineConfig() {
		if(drawPanel!=null) {
			drawPanel.updateMachineConfig();
			drawPanel.zoomToFitPaper();
		}
	}
	
	public ArrayList<String> getgCode() {
		return gCode.lines;
	}

	private void playSound(String url) {
		if(url.isEmpty()) return;
		
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
	
	private void playDawingFinishedSound() {
		playSound(prefs.get("sound_drawing_finished", ""));
	}
	

	// appends a message to the log tab and system out.
	public void log(String msg) {
		// remove the 
		if(msg.indexOf(';') != -1 ) msg = msg.substring(0,msg.indexOf(';'));
		
		msg=msg.replace("\n", "<br>\n")+"\n";
		msg=msg.replace("\n\n","\n");
		logToFile.write(msg);
		logToFile.flush();

		try {
			kit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
			int over_length = doc.getLength() - msg.length() - 5000;
			doc.remove(0, over_length);
			//logPane.getVerticalScrollBar().setValue(logPane.getVerticalScrollBar().getMaximum());
		} catch (BadLocationException e) {
			// Do we care if it fails?
		} catch (IOException e) {
			// Do we care if it fails?
		}
	}

	public void clearLog() {
		try {
			doc.replace(0, doc.getLength(), "", null);
			kit.insertHTML(doc, 0, "", 0, 0, null);
			//logPane.getVerticalScrollBar().setValue(logPane.getVerticalScrollBar().getMaximum());
		} catch (BadLocationException e) {
			// Do we care if it fails?
		} catch (IOException e) {
			// Do we care if it fails?
		}
	}
	
	
	public String getTempDestinationFile() {
		return System.getProperty("user.dir")+"/temp.ngc";
	}
	
	public boolean isFileLoaded() {
		return ( gCode.fileOpened && gCode.lines != null && gCode.lines.size() > 0 );
	}
	
	private String selectFile() {
		JFileChooser choose = new JFileChooser();
	    int returnVal = choose.showOpenDialog(this);
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
		final JDialog driver = new JDialog(mainframe,translator.get("MenuSoundsTitle"),true);
		driver.setLayout(new GridBagLayout());
		
		final JTextField sound_connect = new JTextField(prefs.get("sound_connect",""),32);
		final JTextField sound_disconnect = new JTextField(prefs.get("sound_disconnect", ""),32);
		final JTextField sound_conversion_finished = new JTextField(prefs.get("sound_conversion_finished", ""),32);
		final JTextField sound_drawing_finished = new JTextField(prefs.get("sound_drawing_finished", ""),32);

		final JButton change_sound_connect = new JButton(translator.get("MenuSoundsConnect"));
		final JButton change_sound_disconnect = new JButton(translator.get("MenuSoundsDisconnect"));
		final JButton change_sound_conversion_finished = new JButton(translator.get("MenuSoundsFinishConvert"));
		final JButton change_sound_drawing_finished = new JButton(translator.get("MenuSoundsFinishDraw"));
		
		//final JCheckBox allow_metrics = new JCheckBox(String.valueOf("I want to add the distance drawn to the // total"));
		//allow_metrics.setSelected(allowMetrics);
		
		final JButton cancel = new JButton(translator.get("Cancel"));
		final JButton save = new JButton(translator.get("Save"));
		
		GridBagConstraints c = new GridBagConstraints();
		//c.gridwidth=4; 	c.gridx=0;  c.gridy=0;  driver.add(allow_metrics,c);

		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=0;  c.gridy=3;  driver.add(change_sound_connect,c);								c.anchor=GridBagConstraints.WEST;	c.gridwidth=3;	c.gridx=1;  c.gridy=3;  driver.add(sound_connect,c);
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=0;  c.gridy=4;  driver.add(change_sound_disconnect,c);							c.anchor=GridBagConstraints.WEST;	c.gridwidth=3;	c.gridx=1;  c.gridy=4;  driver.add(sound_disconnect,c);
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=0;  c.gridy=5;  driver.add(change_sound_conversion_finished,c);					c.anchor=GridBagConstraints.WEST;	c.gridwidth=3;	c.gridx=1;  c.gridy=5;  driver.add(sound_conversion_finished,c);
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=0;  c.gridy=6;  driver.add(change_sound_drawing_finished,c);					c.anchor=GridBagConstraints.WEST;	c.gridwidth=3;	c.gridx=1;  c.gridy=6;  driver.add(sound_drawing_finished,c);
		
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=2;  c.gridy=12;  driver.add(save,c);
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=1;	c.gridx=3;  c.gridy=12;  driver.add(cancel,c);
		
		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					if(subject == change_sound_connect) sound_connect.setText(selectFile());
					if(subject == change_sound_disconnect) sound_disconnect.setText(selectFile());
					if(subject == change_sound_conversion_finished) sound_conversion_finished.setText(selectFile());
					if(subject == change_sound_drawing_finished) sound_drawing_finished.setText(selectFile());

					if(subject == save) {
						//allowMetrics = allow_metrics.isSelected();
						prefs.put("sound_connect",sound_connect.getText());
						prefs.put("sound_disconnect",sound_disconnect.getText());
						prefs.put("sound_conversion_finished",sound_conversion_finished.getText());
						prefs.put("sound_drawing_finished",sound_drawing_finished.getText());
						machineConfiguration.saveConfig();
						driver.dispose();
					}
					if(subject == cancel) {
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
		
		final JDialog driver = new JDialog(mainframe,translator.get("MenuGraphicsTitle"),true);
		driver.setLayout(new GridBagLayout());
		
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
		//c.gridwidth=4; 	c.gridx=0;  c.gridy=0;  driver.add(allow_metrics,c);

		int y=0;
		
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=1;	c.gridx=1;  c.gridy=y;  driver.add(show_pen_up,c);  y++;
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=1;	c.gridx=1;  c.gridy=y;  driver.add(draw_all_while_running,c);  y++;
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=1;	c.gridx=1;  c.gridy=y;  driver.add(antialias_on,c);  y++;
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=1;	c.gridx=1;  c.gridy=y;  driver.add(speed_over_quality,c);  y++;
		
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=2;  c.gridy=y;  driver.add(save,c);
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=1;	c.gridx=3;  c.gridy=y;  driver.add(cancel,c);
		
		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					if(subject == save) {
						//allowMetrics = allow_metrics.isSelected();
						graphics_prefs.putBoolean("show pen up", show_pen_up.isSelected());
						graphics_prefs.putBoolean("antialias", antialias_on.isSelected());
						graphics_prefs.putBoolean("speed over quality", speed_over_quality.isSelected());
						graphics_prefs.putBoolean("Draw all while running", draw_all_while_running.isSelected());

						drawPanel.setShowPenUp(show_pen_up.isSelected());
						driver.dispose();
					}
					if(subject == cancel) {
						driver.dispose();
					}
			  }
		};

		save.addActionListener(driveButtons);
		cancel.addActionListener(driveButtons);
	    driver.getRootPane().setDefaultButton(save);
		driver.pack();
		driver.setVisible(true);
	}
	
	
	/**
	 * Send the machine configuration to the robot
	 */
	public void sendConfig() {
		if(connectionToRobot!=null && !connectionToRobot.isRobotConfirmed()) return;
		
		// Send a command to the robot with new configuration values
		sendLineToRobot(machineConfiguration.getConfigLine()+"\n");
		sendLineToRobot(machineConfiguration.getBobbinLine()+"\n");
		sendLineToRobot("G92 X0 Y0\n");
	}
	
	
	/**
	 * Take the next line from the file and send it to the robot, if permitted. 
	 */
	public void sendFileCommand() {
		if(isRunning ==false || isPaused==true || gCode.fileOpened==false ||
				(connectionToRobot!=null && connectionToRobot.isRobotConfirmed()==false) || gCode.linesProcessed>= gCode.linesTotal) return;
		
		String line;
		do {			
			// are there any more commands?
			// TODO: find out how far the pen moved each line and add it to the distance total.
			int line_number = gCode.linesProcessed;
			gCode.linesProcessed++;
			line = gCode.lines.get(line_number).trim();

			// catch pen up/down status here
			if(line.contains("Z"+machineConfiguration.getPenUpString())) {
				driveControls.raisePen();
			}
			if(line.contains("Z"+machineConfiguration.getPenDownString())) {
				driveControls.lowerPen();
			}
			

			if(line.length()>3) {
				line="N"+line_number+" "+line;
			}
			line += generateChecksum(line);
			
			drawPanel.setLinesProcessed(gCode.linesProcessed);
			statusBar.setProgress(gCode.linesProcessed, gCode.linesTotal);
			// loop until we find a line that gets sent to the robot, at which point we'll
			// pause for the robot to respond.  Also stop at end of file.
		} while(processLine(line) && gCode.linesProcessed< gCode.linesTotal);
		
		if(gCode.linesProcessed== gCode.linesTotal) {
			// end of file
			playDawingFinishedSound();
			halt();
			sayHooray();
		}
	}
	
	
	private void sayHooray() {
		long num_lines = gCode.linesProcessed;
		
		JOptionPane.showMessageDialog(null,
				translator.get("Finished") + " " +
				num_lines +
				translator.get("LineSegments") + 
				"\n" +
				statusBar.getElapsed() +
				"\n" +
				translator.get("SharePromo")
				);
	}
	
	
	private void changeToTool(String changeToolString) {
		int i = Integer.decode(changeToolString);
		
		String [] toolNames = machineConfiguration.getToolNames();
		
		if(i<0 || i>toolNames.length) {
			log("<span style='color:red'>" + translator.get("InvalidTool") + i +"</span>");
			i=0;
		}
		JOptionPane.showMessageDialog(null, translator.get("ChangeToolPrefix") + toolNames[i] + translator.get("ChangeToolPostfix"));
	}
	
	
	/**
	 * removes comments, processes commands drawbot shouldn't have to handle.
	 * @param line command to send
	 * @return true if the robot is ready for another command to be sent.
	 */
	public boolean processLine(String line) {
		if(connectionToRobot == null || !connectionToRobot.isRobotConfirmed() || !isRunning) return false;

		// tool change request?
		String [] tokens = line.split("(\\s|;)");

		// tool change?
		if(Arrays.asList(tokens).contains("M06") || Arrays.asList(tokens).contains("M6")) {
			for(int i=0;i<tokens.length;++i) {
				if(tokens[i].startsWith("T")) {
					changeToTool(tokens[i].substring(1));
				}
			}
		}
		
		// end of program?
		if(tokens[0]=="M02" || tokens[0]=="M2" || tokens[0]=="M30") {
			playDawingFinishedSound();
			halt();
			return false;
		}
		
		
		// send relevant part of line to the robot
		sendLineToRobot(line);
		
		return false;
	}
	
	
	protected String generateChecksum(String line) {
		byte checksum=0;
		
		for( int i=0; i<line.length(); ++i ) {
			checksum ^= line.charAt(i);
		}
		
		return "*"+((int)checksum);
	}
	

	/**
	 * Sends a single command the robot.  Could be anything.
	 * @param line command to send.
	 * @return <code>true</code> if command was sent to the robot; <code>false</code> otherwise.
	 */
	public boolean sendLineToRobot(String line) {
		if(connectionToRobot==null || !connectionToRobot.isRobotConfirmed()) return false;
		
		if(line.trim().equals("")) return false;
		String reportedline = line;
		if(line.contains(";")) {
			String [] lines = line.split(";");
			reportedline = lines[0];
		}
		log("<font color='white'>" + reportedline + "</font>");
		line += "\n";
		
		try {
			connectionToRobot.sendMessage(line);
		}
		catch(Exception e) {
			log(e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * stop sending file commands to the robot.
	 * TODO add an e-stop command?
	 */
	public void halt() {
		isRunning = false;
		isPaused = false;
	    drawPanel.setLinesProcessed(0);
		drawPanel.setRunning(isRunning);
		updateMenuBar();
	}
	
	public void startAt(long lineNumber) {
		gCode.linesProcessed=0;
		sendLineToRobot("M110 N" + gCode.linesProcessed);
		drawPanel.setLinesProcessed(gCode.linesProcessed);
		startDrawing();
	}
	
	public void pause() {
		isPaused=true;
	}
	
	public void unPause() {
		isPaused=false;
	}

	private void startDrawing() {
		isPaused=false;
		isRunning =true;
		drawPanel.setRunning(isRunning);
		updateMenuBar();
		statusBar.start();
		sendFileCommand();
	}
	
	// The user has done something.  respond to it.
    @Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if( subject == buttonZoomIn ) {
			drawPanel.zoomIn();
			return;
		}
		if( subject == buttonZoomOut ) {
			drawPanel.zoomOut();
			return;
		}
		if( subject == buttonZoomToFit ) {
			drawPanel.zoomToFitPaper();
			return;
		}
		if( subject == buttonRescan ) {
			connectionManager.listConnections();
			updateMenuBar();
			return;
		}
		if( subject == buttonDisconnect ) {
			connectionToRobot.closeConnection();
			connectionToRobot=null;
			clearLog();
			drawPanel.setConnected(false);
			updateMenuBar();
			playDisconnectSound();

			// update window title
			mainframe.setTitle(translator.get("TitlePrefix")
					+ Long.toString(machineConfiguration.getUID())
					+ translator.get("TitleNotConnected"));
			return;
		}
		if( subject == buttonAdjustSounds ) {
			adjustSounds();
			return;
		}
		if( subject == buttonAdjustGraphics ) {
			adjustGraphics();
			return;
		}
		if( subject == buttonAdjustLanguage ) {
			translator.chooseLanguage();
			updateMenuBar();
		}
		if( subject == buttonAbout ) {
			displayAbout();
			return;
		}
		if( subject == buttonCheckForUpdate ) {
			checkForUpdate();
			return;
		}
		
		if( subject == buttonExit ) {
			System.exit(0);
			return;
		}
		
		String [] connections = connectionManager.listConnections(); 
		for(int i=0;i<connections.length;++i) {
			if(subject == buttonPorts[i]) {

				log("<font color='green'>" + translator.get("ConnectingTo") + connections[i] + "...</font>\n");

				connectionToRobot = connectionManager.openConnection(connections[i]);
				if(connectionToRobot!=null) {
					log("<span style='color:green'>" + translator.get("PortOpened") + "</span>\n");
					updateMenuBar();
					playConnectSound();
				} else {
					log("<span style='color:red'>" + translator.get("PortOpenFailed") + "</span>\n");
				}
				return;
			}
		}
	}

	/**
	 *
	 * @return byte array containing data for image icon.
	 */
	private ImageIcon getImageIcon(String iconResourceName) {
		ImageIcon icon = null;
		try {
            final byte[] imageData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(iconResourceName));
            icon = new ImageIcon(imageData);
        } catch(NullPointerException | IOException exceptionLoadingIconImage) {
            System.err.print(exceptionLoadingIconImage);
        }
		return icon;
	}

	/**
	 * 
     * <p>
     * Uses {@link java.lang.StringBuilder#append(String)} to create an internationalization supported {@code String}
     * representing the About Message Dialog's HTML.
     * </p>
     *
     * <p>
     * The summation of {@link String#length()} for each of the respective values retrieved with the
     * {@code "AboutHTMLBeforeVersionNumber"}, and {@code "AboutHTMLAfterVersionNumber"} {@link MultilingualSupport} keys,
     * in conjunction with {@link MainGUI#version} is calculated for use with {@link java.lang.StringBuilder#StringBuilder(int)}.
     * </p>
     *
     * @return An HTML string used for the About Message Dialog.
     */
    private String getAboutHtmlFromMultilingualString() {
        final String aboutHtmlBeforeVersionNumber = translator.get("AboutHTMLBeforeVersionNumber");
        final String aboutHmlAfterVersionNumber = translator.get("AboutHTMLAfterVersionNumber");
        final int aboutHTMLBeforeVersionNumberLength = aboutHtmlBeforeVersionNumber.length();
        final int versionNumberStringLength = version.length();
        final int aboutHtmlAfterVersionNumberLength = aboutHmlAfterVersionNumber.length();
        final int aboutHtmlStringBuilderCapacity = aboutHTMLBeforeVersionNumberLength + versionNumberStringLength + aboutHtmlAfterVersionNumberLength;
        final StringBuilder aboutHtmlStringBuilder = new StringBuilder(aboutHtmlStringBuilderCapacity);
        aboutHtmlStringBuilder.append(aboutHtmlBeforeVersionNumber);
        aboutHtmlStringBuilder.append(version);
        aboutHtmlStringBuilder.append(aboutHmlAfterVersionNumber);
        return aboutHtmlStringBuilder.toString();
    }

    /**
	 * 
	 * @param html String of valid HTML.
	 * @return a 
	 */
	private JTextComponent createHyperlinkListenableJEditorPane(String html) {
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
	 * display the about dialog.
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
	
	public JMenuBar createMenuBar() {
        // If the menu bar exists, empty it.  If it doesn't exist, create it.
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
	        if((inputLine = in.readLine()) != null) {
	        	// parse the URL in the text-only redirect
	        	String matchStart = "<a href=\"";
	        	String matchEnd = "\">";
	        	int start = inputLine.indexOf(matchStart);
	        	int end = inputLine.indexOf(matchEnd);
	        	if(start != -1 && end != -1) {
	        		inputLine = inputLine.substring(start+matchStart.length(),end);
	        		// parse the last part of the redirect URL, which contains the release tag (which is the version)
	        		inputLine = inputLine.substring(inputLine.lastIndexOf("/")+1);

	        		System.out.println("last release: "+inputLine);
	        		System.out.println("your version: "+version);
	        		//System.out.println(inputLine.compareTo(version));
	        		
	        		if( inputLine.compareTo(version) > 0 ) {
		        		JOptionPane.showMessageDialog(null,translator.get("UpdateNotice"));
		        	} else {
		        		JOptionPane.showMessageDialog(null,translator.get("UpToDate"));
		        	}
	        	}
	        } else {
	        	throw new Exception();
	        }
	        in.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,translator.get("UpdateCheckFailed"));
		}
	}

	// Rebuild the contents of the menu based on current program state
	public void updateMenuBar() {
		JMenu menu, subMenu;
		ButtonGroup group;
        int i;

    	boolean isConfirmed = connectionToRobot!=null && connectionToRobot.isRobotConfirmed();
    	
        if(settingsPane!=null) {
        	settingsPane.updateButtonAccess(isConfirmed, isRunning);
        }
        if(prepareImage!=null) {
        	prepareImage.updateButtonAccess(isRunning);
        }
        if(driveControls!=null) {
        	driveControls.updateButtonAccess(isConfirmed, isRunning);
        }
        
        
        menuBar.removeAll();
        
        
        // File menu
        menu = new JMenu(translator.get("MenuMakelangelo"));
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        
        subMenu = new JMenu(translator.get("MenuPreferences"));
        
        buttonAdjustSounds = new JMenuItem(translator.get("MenuSoundsTitle"));
        buttonAdjustSounds.addActionListener(this);
        subMenu.add(buttonAdjustSounds);

        buttonAdjustGraphics = new JMenuItem(translator.get("MenuGraphicsTitle"));
        buttonAdjustGraphics.addActionListener(this);
        subMenu.add(buttonAdjustGraphics);

        buttonAdjustLanguage = new JMenuItem(translator.get("MenuLanguageTitle"));
        buttonAdjustLanguage.addActionListener(this);
        subMenu.add(buttonAdjustLanguage);
        menu.add(subMenu);
        
        buttonCheckForUpdate = new JMenuItem(translator.get("MenuUpdate"),KeyEvent.VK_U);
        buttonCheckForUpdate.addActionListener(this);
        buttonCheckForUpdate.setEnabled(true);
        menu.add(buttonCheckForUpdate);
        
        buttonAbout = new JMenuItem(translator.get("MenuAbout"),KeyEvent.VK_A);
        buttonAbout.addActionListener(this);
        menu.add(buttonAbout);

        menu.addSeparator();
        
        buttonExit = new JMenuItem(translator.get("MenuQuit"),KeyEvent.VK_Q);
        buttonExit.addActionListener(this);
        menu.add(buttonExit);
        
        
        // Connect menu
        subMenu = new JMenu(translator.get("MenuConnect"));
        subMenu.setEnabled(!isRunning);
        group = new ButtonGroup();

        String [] connections = connectionManager.listConnections();
        buttonPorts = new JRadioButtonMenuItem[connections.length];
        for(i=0;i<connections.length;++i) {
        	buttonPorts[i] = new JRadioButtonMenuItem(connections[i]);
            if(connectionToRobot!=null && connectionToRobot.getRecentConnection().equals(connections[i]) && connectionToRobot.isConnectionOpen()) {
            	buttonPorts[i].setSelected(true);
            }
            buttonPorts[i].addActionListener(this);
            group.add(buttonPorts[i]);
            subMenu.add(buttonPorts[i]);
        }
        
        subMenu.addSeparator();

        buttonRescan = new JMenuItem(translator.get("MenuRescan"),KeyEvent.VK_N);
        buttonRescan.addActionListener(this);
        subMenu.add(buttonRescan);

        buttonDisconnect = new JMenuItem(translator.get("MenuDisconnect"),KeyEvent.VK_D);
        buttonDisconnect.addActionListener(this);
        buttonDisconnect.setEnabled(connectionToRobot!=null && connectionToRobot.isConnectionOpen());
        subMenu.add(buttonDisconnect);
        
        menuBar.add(subMenu);
        
        // view menu
        menu = new JMenu(translator.get("MenuPreview"));
        buttonZoomOut = new JMenuItem(translator.get("ZoomOut"));
        buttonZoomOut.addActionListener(this);
        buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.ALT_MASK));
        menu.add(buttonZoomOut);
        
        buttonZoomIn = new JMenuItem(translator.get("ZoomIn"),KeyEvent.VK_EQUALS);
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

	public Container createContentPane() {
        //Create the content-pane-to-be.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
        
        // the log panel
        log = new JTextPane();
        log.setEditable(false);
        log.setBackground(Color.BLACK);
        logPane = new JScrollPane(log);
        kit = new HTMLEditorKit();
        doc = new HTMLDocument();
        log.setEditorKit(kit);
        log.setDocument(doc);
        DefaultCaret c = (DefaultCaret)log.getCaret();
        c.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        clearLog();

        settingsPane = new MakelangeloSettingsPanel();
        settingsPane.createPanel(this, translator, machineConfiguration);
        
        drawPanel = new DrawPanel(machineConfiguration);
        drawPanel.setGCode(gCode);

        prepareImage = new PrepareImagePanel();
        prepareImage.createPanel(this,translator,machineConfiguration);
        prepareImage.updateButtonAccess(false);
		
        driveControls = new MakelangeloDriveControls();
		driveControls.createPanel(this,translator,machineConfiguration);
		driveControls.updateButtonAccess(false, false);
        
		statusBar = new StatusBar(translator);

        contextMenu = new JTabbedPane();
        contextMenu.addTab(translator.get("MenuSettings"),null,settingsPane,null);
        contextMenu.addTab(translator.get("MenuGCODE"),null,prepareImage,null);
        contextMenu.addTab(translator.get("MenuDraw"),null,driveControls,null);
        contextMenu.addTab(translator.get("MenuLog"),null,logPane,null);

        // major layout
        split_left_right = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
        split_left_right.add(drawPanel);
        split_left_right.add(contextMenu);

        contentPane.add(statusBar,BorderLayout.SOUTH);
        contentPane.add(split_left_right,BorderLayout.CENTER);
		
        return contentPane;
    }


    public JFrame getParentFrame() {
    	return mainframe;
    }
    
    
    // Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
    private void createAndShowGUI() {
        // Create and set up the window.
    	mainframe = new JFrame("Makelangelo");
        mainframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        // Create and set up the content pane.
        mainframe.setJMenuBar(createMenuBar());
        mainframe.setContentPane(createContentPane());
 
        // Display the window.
        int width =prefs.getInt("Default window width", (int)(1200.0*5.0/5.0));
        int height=prefs.getInt("Default window height", (int)(1020.0*5.0/5.0));
        mainframe.setSize(width,height);
        mainframe.setVisible(true);
        
        drawPanel.zoomToFitPaper();
        
        // 2015-05-03: option is meaningless, connectionToRobot doesn't exist when software starts.
        // if(prefs.getBoolean("Reconnect to last port on start", false)) connectionToRobot.reconnect();
        if(prefs.getBoolean("Check for updates", false)) checkForUpdate();
    }

	/**
	 *
	 * @return the <code>javax.swing.JFrame</code> representing the main frame of this GUI.
	 */
	public JFrame getMainframe() {
		return mainframe;
	}

	/**
	 *
	 * @return the <code>com.marginallyclever.makelangelo.DrawPanel</code> representing the preview pane of this GUI.
	 */
	public DrawPanel getDrawPanel() {
		return drawPanel;
	}

	/**
	 *
	 * driveControls the <code>javax.swing.JPanel</code> representing the preview pane of this GUI.
	 */
	public void updatedriveControls() {
		driveControls.createPanel(this, translator, machineConfiguration);
	}

	/**
	 *
	 * @return the <code>GCodeFile</code> representing the G-Code file used by this GUI.
	 */
	public GCodeFile getGcodeFile() {
		return gCode;
	}
}


/**
 * This file is part of DrawbotGUI.
 *
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */
