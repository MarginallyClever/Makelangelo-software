package com.marginallyclever.makelangeloRobot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.basictypes.ImageManipulator;
import com.marginallyclever.communications.MarginallyCleverConnection;
import com.marginallyclever.generators.ImageGenerator;
import com.marginallyclever.loaders.LoadFileType;
import com.marginallyclever.loaders.LoadGCode;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.SoundSystem;
import com.marginallyclever.makelangelo.StatusBar;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.settings.MakelangeloSettingsDialog;
import com.marginallyclever.basictypes.CollapsiblePanel;
import com.marginallyclever.savers.SaveFileType;

/**
 * Controls related to converting an image to gcode
 *
 * @author dan royer
 * @author Peter Colapietro
 * @since 7.1.4
 */
public class MakelangeloRobotPanel extends JScrollPane implements ActionListener, ChangeListener, MouseListener, MouseMotionListener, ItemListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -4703402918904039337L;

	// god objects ?
	protected Translator translator;
	protected MakelangeloRobot robot;
	protected Makelangelo gui;

	// connect menu
	private CollapsiblePanel connectionPanel;
	private JPanel connectionList;
	private JRadioButtonMenuItem[] buttonConnections;
	private JComboBox<String> connectionComboBox;
	private boolean ignoreSelectionEvents=false;
	private JButton buttonRescan;
	
	// machine options
	protected String lastFileIn = "";
	protected String lastFileOut = "";
	protected int generatorChoice=0;
	
	private String[] machineConfigurations;
	private JComboBox<String> machineChoices;
	private JButton openConfig;
	private JSlider paperMargin;
	private JPanel machineNumberPanel;
	private JButton buttonOpenFile, buttonNewFile, buttonGenerate, buttonSaveFile;
	protected JButton buttonStart, buttonStartAt, buttonPause, buttonHalt;

	// driving controls
	private JPanel driveControlPanel;
	private JButton down100,down10,down1,up1,up10,up100;
	private JButton left100,left10,left1,right1,right10,right100;
	private JButton goHome,setHome;
	private JButton goTop,goBottom,goLeft,goRight,penUp,penDown;

	// speed
	private JFormattedTextField feedRate;
	private JButton setFeedRate;
	private JButton disengageMotors;

	// etch-a-sketch test
	private JLabel coordinates;
	private JPanel dragAndDrive;
	private boolean mouseInside,mouseOn;
	double last_x,last_y;

	// status bar
	public StatusBar statusBar;


	protected List<SaveFileType> loadFileSavers() {
		return new ArrayList<SaveFileType>();
	}


	public JButton tightJButton(String label) {
		JButton b = new JButton(label);
		b.setMargin(new Insets(0,0,0,0));
		b.setPreferredSize(new Dimension(60,20));
		return b;
	}


	protected JPanel getConnectMenu() {
		connectionPanel = new CollapsiblePanel(Translator.get("MenuConnect"));
		JPanel contents =connectionPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;
		
		connectionList = new JPanel(new GridLayout(0,1));
		rescanConnections();
		
        buttonRescan = new JButton(Translator.get("MenuRescan"));
        buttonRescan.addActionListener(this);

        contents.add(connectionList,con1);
		con1.gridy++;
		contents.add(buttonRescan,con1);
		con1.gridy++;

	    return connectionPanel;
	}
	
	
	protected void rescanConnections() {
		ignoreSelectionEvents=true;
	    connectionComboBox = new JComboBox<String>();
        connectionComboBox.addItemListener(this);
        connectionList.removeAll();
        connectionList.add(connectionComboBox);
	    
        connectionComboBox.addItem("No connection"); // index 0
	    connectionComboBox.setSelectedIndex(0);
	    
	    String recentConnection = "";
	    if(robot.getConnection()!=null) {
	    	recentConnection = robot.getConnection().getRecentConnection();
	    }

	    if(gui.getConnectionManager()!=null) {
			String [] portsDetected = gui.getConnectionManager().listConnections();
			int i;
		    for(i=0;i<portsDetected.length;++i) {
		    	connectionComboBox.addItem(portsDetected[i]);
		    	if(recentConnection.equals(portsDetected[i])) {
		    		connectionComboBox.setSelectedIndex(i+1);
		    	}
		    }
	    }
        ignoreSelectionEvents=false;
	}
	

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object subject = e.getSource();
		
		if(subject == connectionComboBox) {
			if(ignoreSelectionEvents==false && e.getStateChange()==ItemEvent.SELECTED) {
				if(connectionComboBox.getSelectedIndex()==0) {
					// disconnect
					robot.setConnection(null);
				} else {
					String connectionName = connectionComboBox.getItemAt(connectionComboBox.getSelectedIndex());
					robot.setConnection( gui.getConnectionManager().openConnection(connectionName) );
				}
				rescanConnections();
				return;
			}
		}
	}
	
	
	public MakelangeloRobotPanel(Makelangelo gui, Translator translator, MakelangeloRobot robot) {
		GridBagConstraints c;
		
		this.translator = translator;
		this.gui = gui;
		this.robot = robot;

		this.setBorder(BorderFactory.createEmptyBorder());

		JPanel panel = new JPanel(new GridBagLayout());
		this.setViewportView(panel);

		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx = 0;
		con1.gridy = 0;
		con1.weightx = 1;
		con1.weighty = 0;
		con1.fill = GridBagConstraints.HORIZONTAL;
		con1.anchor = GridBagConstraints.NORTHWEST;

		JPanel connectPanel = getConnectMenu();
		panel.add(connectPanel, con1);
		con1.gridy++;

		
		// settings
		machineNumberPanel = new JPanel(new GridLayout(1, 0));
		updateMachineNumberPanel();
		panel.add(machineNumberPanel, con1);
		con1.gridy++;

		// margins
		JPanel marginPanel = new JPanel(new GridLayout(1, 0));
		paperMargin = new JSlider(JSlider.HORIZONTAL, 0, 50, 100 - (int) (robot.settings.getPaperMargin() * 100));
		paperMargin.setMajorTickSpacing(10);
		paperMargin.setMinorTickSpacing(5);
		paperMargin.setPaintTicks(false);
		paperMargin.setPaintLabels(true);
		paperMargin.addChangeListener(this);
		marginPanel.add(new JLabel(Translator.get("PaperMargin")));
		marginPanel.add(paperMargin);
		panel.add(marginPanel, con1);
		con1.gridy++;
		panel.add(new JSeparator(),con1);
		con1.gridy++;

		// feed rate
		JPanel feedRateControl = new JPanel();
		feedRateControl.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		feedRate = new JFormattedTextField(NumberFormat.getInstance());  feedRate.setPreferredSize(new Dimension(100,20));
		feedRate.setText(Double.toString(robot.settings.getFeedRate()));
		setFeedRate = new JButton(Translator.get("Set"));
		setFeedRate.addActionListener(this);
		disengageMotors = new JButton(Translator.get("DisengageMotors"));
		disengageMotors.addActionListener(this);

		c.gridx=3;  c.gridy=0;  feedRateControl.add(new JLabel(Translator.get("Speed")),c);
		c.gridx=4;  c.gridy=0;  feedRateControl.add(feedRate,c);
		c.gridx=5;  c.gridy=0;  feedRateControl.add(new JLabel(Translator.get("Rate")),c);
		c.gridx=6;  c.gridy=0;  feedRateControl.add(setFeedRate,c);
		c.gridx=7;  c.gridy=0;  feedRateControl.add(disengageMotors,c);

		panel.add(feedRateControl,con1);
		con1.gridy++;



		// Driving controls
		mouseInside=false;
		mouseOn=false;
		last_x=last_y=0;

		makeDriveControls();
		panel.add(driveControlPanel,con1);
		con1.gridy++;
		panel.add(new JSeparator(),con1);
		con1.gridy++;



		// drive to corners
		
		JPanel corners = new JPanel();
		corners.setLayout(new GridBagLayout());
		goTop = new JButton(Translator.get("Top"));       goTop.setPreferredSize(new Dimension(80,20));
		goBottom = new JButton(Translator.get("Bottom")); goBottom.setPreferredSize(new Dimension(80,20));
		goLeft = new JButton(Translator.get("Left"));     goLeft.setPreferredSize(new Dimension(80,20));
		goRight = new JButton(Translator.get("Right"));   goRight.setPreferredSize(new Dimension(80,20));
		penUp = new JButton(Translator.get("PenUp"));      penUp.setPreferredSize(new Dimension(100,20));
		penDown = new JButton(Translator.get("PenDown"));  penDown.setPreferredSize(new Dimension(100,20));
		//final JButton find = new JButton("FIND HOME");    find.setPreferredSize(new Dimension(100,20));
		//setHome = new JButton(translator.get("SetHome"));     setHome.setPreferredSize(new Dimension(100,20));
		goHome = new JButton(Translator.get("GoHome"));     goHome.setPreferredSize(new Dimension(100,20));
		JLabel horizontalFiller = new JLabel(" ");
		c = new GridBagConstraints();
		c.gridx=2;  c.gridy=0;  corners.add(goTop,c);
		c.gridx=2;  c.gridy=1;  corners.add(goHome,c);
		c.gridx=2;  c.gridy=2;  corners.add(goBottom,c);
		c.gridx=1;  c.gridy=1;  corners.add(goLeft,c);
		c.gridx=3;  c.gridy=1;  corners.add(goRight,c);
		c.weightx=1;
		c.gridx=4;  c.gridy=0;  corners.add(horizontalFiller,c);
		c.weightx=0;
		c.gridx=5;  c.gridy=0;  corners.add(penUp,c);
		c.gridx=5;  c.gridy=2;  corners.add(penDown,c);

		//c.gridx=0;  c.gridy=0;  corners.add(setHome,c);
		c.insets = new Insets(0,0,0,0);
		goTop.addActionListener(this);
		goBottom.addActionListener(this);
		goLeft.addActionListener(this);
		goRight.addActionListener(this);
		penUp.addActionListener(this);
		penDown.addActionListener(this);
		setHome.addActionListener(this);
		goHome.addActionListener(this);

		dragAndDrive = new JPanel(new GridBagLayout());
		dragAndDrive.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		dragAndDrive.addMouseListener(this);
		dragAndDrive.addMouseMotionListener(this);

		coordinates = new JLabel(Translator.get("ClickAndDrag"));
		c.anchor = GridBagConstraints.CENTER;

		// TODO dimensioning doesn't work right.  The better way would be a pen tool to drag on the 3d view.  That's a lot of work.
		Dimension dims = new Dimension();
		dims.setSize( 150, 150 * (double)robot.settings.getPaperWidth()/(double)robot.settings.getPaperHeight());
		dragAndDrive.setPreferredSize(dims);
		dragAndDrive.add(coordinates,c);

		con1.weightx=1;
		con1.weighty=0;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTHWEST;


		panel.add(corners,con1);
		con1.gridy++;
		panel.add(new JSeparator(),con1);
		con1.gridy++;
		//con1.weighty=1;
		//p.add(dragAndDrive,con1);
		//con1.weighty=0;
		//con1.gridy++;



		// File conversion
		buttonNewFile = new JButton(Translator.get("MenuNewFile"));
		buttonNewFile.addActionListener(this);
		panel.add(buttonNewFile, con1);
		con1.gridy++;

		buttonOpenFile = new JButton(Translator.get("MenuOpenFile"));
		buttonOpenFile.addActionListener(this);
		panel.add(buttonOpenFile, con1);
		con1.gridy++;

		buttonGenerate = new JButton(Translator.get("MenuGenerate"));
		buttonGenerate.addActionListener(this);
		panel.add(buttonGenerate, con1);
		con1.gridy++;

		buttonSaveFile = new JButton(Translator.get("MenuSaveGCODEAs"));
		buttonSaveFile.addActionListener(this);
		panel.add(buttonSaveFile, con1);
		con1.gridy++;


		// drive menu
		JPanel drivePanel = new JPanel(new GridLayout(1,4));
		buttonStart = new JButton(Translator.get("Start"));
		buttonStartAt = new JButton(Translator.get("StartAtLine"));
		buttonPause = new JButton(Translator.get("Pause"));
		buttonHalt = new JButton(Translator.get("Halt"));
		drivePanel.add(buttonStart);
		drivePanel.add(buttonStartAt);
		drivePanel.add(buttonPause);
		drivePanel.add(buttonHalt);
		buttonStart.addActionListener(this);
		buttonStartAt.addActionListener(this);
		buttonPause.addActionListener(this);
		buttonHalt.addActionListener(this);
		panel.add(drivePanel, con1);
		con1.gridy++;

		panel.add(new JSeparator(), con1);
		con1.gridy++;



		statusBar = new StatusBar(translator);
		panel.add(statusBar, con1);
		con1.gridy++;


		// always have one extra empty at the end to push everything up.
		con1.weighty = 1;
		panel.add(new JLabel(), con1);
	}

	private void makeDriveControls() {
		driveControlPanel = new JPanel();
		JPanel axisControl = new JPanel();
		axisControl.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		down100 = tightJButton("-100");
		down10 = tightJButton("-10");
		down1 = tightJButton("-1");

		setHome = tightJButton(Translator.get("SetHome"));

		up1 = tightJButton("1");
		up10 = tightJButton("10");
		up100 = tightJButton("100");

		left100 = tightJButton("-100");
		left10 = tightJButton("-10");
		left1 = tightJButton("-1");
		right1 = tightJButton("1");
		right10 = tightJButton("10");
		right100 = tightJButton("100");

		c.fill=GridBagConstraints.BOTH;
		c.gridx=3;  c.gridy=6;  axisControl.add(down100,c);
		c.gridx=3;  c.gridy=5;  axisControl.add(down10,c);
		c.gridx=3;  c.gridy=4;  axisControl.add(down1,c);
		c.gridx=3;  c.gridy=3;  axisControl.add(setHome,c);     setHome.setPreferredSize(new Dimension(100,20));
		c.gridx=3;  c.gridy=2;  axisControl.add(up1,c);
		c.gridx=3;  c.gridy=1;  axisControl.add(up10,c);
		c.gridx=3;  c.gridy=0;  axisControl.add(up100,c);

		c.gridx=0;  c.gridy=3;  axisControl.add(left100,c);
		c.gridx=1;  c.gridy=3;  axisControl.add(left10,c);
		c.gridx=2;  c.gridy=3;  axisControl.add(left1,c);
		c.gridx=4;  c.gridy=3;  axisControl.add(right1,c);
		c.gridx=5;  c.gridy=3;  axisControl.add(right10,c);
		c.gridx=6;  c.gridy=3;  axisControl.add(right100,c);
		up1.addActionListener(this);
		up10.addActionListener(this);
		up100.addActionListener(this);
		down1.addActionListener(this);
		down10.addActionListener(this);
		down100.addActionListener(this);
		left1.addActionListener(this);
		left10.addActionListener(this);
		left100.addActionListener(this);
		right1.addActionListener(this);
		right10.addActionListener(this);
		right100.addActionListener(this);
		
		driveControlPanel.add(axisControl);
	}

	
	public void updateMachineNumberPanel() {
		machineNumberPanel.removeAll();
		machineConfigurations = robot.settings.getKnownMachineNames();
		if( machineConfigurations.length>0 ) {
			machineChoices = new JComboBox<>(machineConfigurations);
			machineNumberPanel.add(new JLabel(Translator.get("MachineNumber")));
			machineNumberPanel.add(machineChoices);

			int index = robot.settings.getKnownMachineIndex();
			if( index<0 ) index=0;
			machineChoices.setSelectedIndex(index);
			
			// if we're connected to a confirmed machine, don't let the user change the number panel or settings could get...weird.
			boolean state=false;
			if( robot.getConnection() == null ) state=true;
			else if( robot.getConnection().isOpen() == false ) state=true;
			else if( robot.isPortConfirmed() == false ) state=true;
			
			machineChoices.setEnabled( state );
		}

		openConfig = new JButton(Translator.get("configureMachine"));
		openConfig.addActionListener(this);
		openConfig.setPreferredSize(openConfig.getPreferredSize());
		machineNumberPanel.add(openConfig);
	}

	
	public void stateChanged(ChangeEvent e) {
		e.getSource();
		double pm = (100 - paperMargin.getValue()) * 0.01;
		if (Double.compare(robot.settings.getPaperMargin(), pm) != 0) {
			robot.settings.setPaperMargin(pm);
			robot.settings.saveConfig();
		}
	}

	// The user has done something. respond to it.
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if( machineChoices != null ) {
			// TODO: maybe only run this when the machineChoices comboBox changes to a new value
			int selectedIndex = machineChoices.getSelectedIndex();
			long newUID = Long.parseLong(machineChoices.getItemAt(selectedIndex));
			robot.settings.loadConfig(newUID);
		}
		
		if( subject == buttonRescan ) {
			rescanConnections();
			return;
		}

		if (subject == openConfig) {
			Frame frame = (Frame)this.getRootPane().getParent();
			MakelangeloSettingsDialog m = new MakelangeloSettingsDialog(frame, translator, robot);
			m.run();
			return;
		}

		if (subject == buttonNewFile) {
			newFile();
			return;
		}
		if (subject == buttonOpenFile) {
			openFileDialog();
			return;
		}
		if (subject == buttonGenerate) {
			generateImage();
			return;
		}
		if (subject == buttonSaveFile) {
			saveFileDialog();
			return;
		}

		if (subject == buttonStart) {
			gui.startAt(0);
			updateButtonAccess(robot.isPortConfirmed(), true);
			return;
		}
		if (subject == buttonStartAt) {
			Long lineNumber = getStartingLineNumber();
			if (lineNumber != -1) {
				gui.startAt(lineNumber);
			}
			updateButtonAccess(robot.isPortConfirmed(), true);
			return;
		}
		if (subject == buttonPause) {
			// toggle pause
			if (robot.isPaused() == true) {
				// we were paused.
				// update button text
				buttonPause.setText(Translator.get("Pause"));
				// ready
				robot.unPause();
				// TODO: if the robot is not ready to unpause, this
				// might fail and the program would appear to hang until
				// a dis- and re-connect.
				gui.sendFileCommand();
			} else {
				robot.pause();
				// change button text
				buttonPause.setText(Translator.get("Unpause"));
			}
			return;
		}
		if (subject == buttonHalt) {
			gui.halt();
			return;
		}
		
		if      (subject == goHome  ) robot.sendLineToRobot("G00 X0 Y0");
		else if (subject == setHome ) robot.sendLineToRobot("G92 X0 Y0");
		else if (subject == goLeft  ) robot.sendLineToRobot("G00 X" + (robot.settings.getPaperLeft() * 10));
		else if (subject == goRight ) robot.sendLineToRobot("G00 X" + (robot.settings.getPaperRight() * 10));
		else if (subject == goTop   ) robot.sendLineToRobot("G00 Y" + (robot.settings.getPaperTop() * 10));
		else if (subject == goBottom) robot.sendLineToRobot("G00 Y" + (robot.settings.getPaperBottom() * 10));
//		else if (b == find    ) robot.sendLineToRobot("G28");
		else if (subject == penUp   ) robot.raisePen();
		else if (subject == penDown ) robot.lowerPen();
		else if (subject == setFeedRate) {
			// get the feedrate value
			String fr = feedRate.getText();
			fr = fr.replaceAll("[ ,]", "");
			// trim it to 3 decimal places
			double parsedFeedRate = 0;
			try {
				parsedFeedRate = Double.parseDouble(fr);

				if (parsedFeedRate < 0.001) parsedFeedRate = 0.001;
				// update the input field
				feedRate.setText(Double.toString(parsedFeedRate));
				robot.setFeedRate(parsedFeedRate);
			} catch(NumberFormatException e1) {}
		} else if (subject == disengageMotors) {
			robot.sendLineToRobot("M18");
		} else {
			String command="";

			if (subject == down100) command = "G0 Y-100";
			if (subject == down10) command = "G0 Y-10";
			if (subject == down1) command = "G0 Y-1";
			if (subject == up100) command = "G0 Y100";
			if (subject == up10) command = "G0 Y10";
			if (subject == up1) command = "G0 Y1";

			if (subject == left100) command = "G0 X-100";
			if (subject == left10) command = "G0 X-10";
			if (subject == left1) command = "G0 X-1";
			if (subject == right100) command = "G0 X100";
			if (subject == right10) command = "G0 X10";
			if (subject == right1) command = "G0 X1";

			if(command != "") {
				robot.sendLineToRobot("G91");  // set relative mode
				robot.sendLineToRobot(command);
				robot.sendLineToRobot("G90");  // return to absolute mode
			}
		}

		// Connecting to a machine
		String[] connections = gui.getConnectionManager().listConnections();
		for (int i = 0; i < connections.length; ++i) {
			if (subject == buttonConnections[i]) {
				Log.clear();
				Log.message(Translator.get("ConnectingTo") + connections[i] + "...");

				MarginallyCleverConnection c = robot.getConnection().getManager().openConnection(connections[i]); 
				if (c == null) {
					Log.error(Translator.get("PortOpenFailed"));
				} else {
					robot.setConnection(c);
					Log.message( Translator.get("PortOpened") );
					gui.updateMenuBar();
					SoundSystem.playConnectSound();
				}
				return;
			}
		}
	}

	/**
	 * open a dialog to ask for the line number.
	 *
	 * @return <code>lineNumber</code> greater than or equal to zero if user hit
	 *         ok.
	 */
	private long getStartingLineNumber() {
		final JPanel panel = new JPanel(new GridBagLayout());
		final JTextField starting_line = new JTextField("0", 8);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JLabel(Translator.get("StartAtLine")), c);
		c.gridwidth = 2;
		c.gridx = 2;
		c.gridy = 0;
		panel.add(starting_line, c);

		int result = JOptionPane.showConfirmDialog(null, panel, Translator.get("StartAt"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			long lineNumber;
			try {
				lineNumber = Long.decode(starting_line.getText());
			} catch (Exception e) {
				lineNumber = -1;
			}

			return lineNumber;
		}
		return -1;
	}

	public void updateButtonAccess(boolean isConfirmed, boolean isRunning) {
		if (buttonGenerate != null)
			buttonGenerate.setEnabled(!isRunning);

		openConfig.setEnabled(!isRunning);

		buttonStart.setEnabled(isConfirmed && !isRunning);
		buttonStartAt.setEnabled(isConfirmed && !isRunning);
		buttonPause.setEnabled(isConfirmed && isRunning);
		buttonHalt.setEnabled(isConfirmed && isRunning);

		if (!isConfirmed) {
			buttonPause.setText(Translator.get("Pause"));
		}

		disengageMotors.setEnabled(isConfirmed && !isRunning);
		buttonNewFile.setEnabled(!isRunning);
		buttonOpenFile.setEnabled(!isRunning);
		buttonGenerate.setEnabled(!isRunning);

		down100.setEnabled(isConfirmed && !isRunning);
		down10.setEnabled(isConfirmed && !isRunning);
		down1.setEnabled(isConfirmed && !isRunning);
		up1.setEnabled(isConfirmed && !isRunning);
		up10.setEnabled(isConfirmed && !isRunning);
		up100.setEnabled(isConfirmed && !isRunning);

		left100.setEnabled(isConfirmed && !isRunning);
		left10.setEnabled(isConfirmed && !isRunning);
		left1.setEnabled(isConfirmed && !isRunning);
		right1.setEnabled(isConfirmed && !isRunning);
		right10.setEnabled(isConfirmed && !isRunning);
		right100.setEnabled(isConfirmed && !isRunning);

		goTop.setEnabled(isConfirmed && !isRunning);
		goBottom.setEnabled(isConfirmed && !isRunning);
		goLeft.setEnabled(isConfirmed && !isRunning);
		goRight.setEnabled(isConfirmed && !isRunning);

		setHome.setEnabled(isConfirmed && !isRunning);
		goHome.setEnabled(isConfirmed && !isRunning);

		penUp.setEnabled(isConfirmed && !isRunning);
		penDown.setEnabled(isConfirmed && !isRunning);

		setFeedRate.setEnabled(isConfirmed && !isRunning);
	}

	public void newFile() {
		gui.gCode.reset();
		gui.updateMenuBar();
		gui.updateMachineConfig();
	}

	/**
	 * Creates a file open dialog. If you don't cancel it opens that file.
	 * Note: source for ExampleFileFilter can be found in FileChooserDemo, under the demo/jfc directory in the Java 2 SDK, Standard Edition.
	 */
	public void openFileDialog() {
		// Is you machine not yet calibrated?
		if (robot.settings.isPaperConfigured() == false) {
			// Hey!  Come back after you calibrate! 
			JOptionPane.showMessageDialog(null, Translator.get("SetPaperSize"));
			return;
		}

		JFileChooser fc = new JFileChooser(new File(lastFileIn));

		ServiceLoader<LoadFileType> imageLoaders = ServiceLoader.load(LoadFileType.class);
		Iterator<LoadFileType> i = imageLoaders.iterator();
		while(i.hasNext()) {
			LoadFileType lft = i.next();
			FileFilter filter = lft.getFileNameFilter();
			fc.addChoosableFileFilter(filter);
		}

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();

			openFileOnDemand(selectedFile);
		}
	}

	public void generateImage() {
		final JPanel panel = new JPanel(new GridBagLayout());

		ServiceLoader<ImageGenerator> imageGenerators = ServiceLoader.load(ImageGenerator.class);
		Iterator<ImageGenerator> ici = imageGenerators.iterator();
		int i=0;
		while(ici.hasNext()) {
			ici.next();
			i++;
		}
		
		String[] imageGeneratorNames = new String[i];
		
		i=0;
		ici = imageGenerators.iterator();
		while (ici.hasNext()) {
			ImageManipulator f = ici.next();
			imageGeneratorNames[i++] = f.getName();
		}

		final JComboBox<String> options = new JComboBox<String>(imageGeneratorNames);
		options.setSelectedIndex(generatorChoice);

		GridBagConstraints c = new GridBagConstraints();

		int y = 0;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = y;
		panel.add(new JLabel(Translator.get("ConversionStyle")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = y++;
		panel.add(options, c);

		int result = JOptionPane.showConfirmDialog(null, panel, Translator.get("ConversionOptions"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			generatorChoice = options.getSelectedIndex();

			ImageGenerator chosenGenerator = null; 
			ici = imageGenerators.iterator();
			i=0;
			while(ici.hasNext()) {
				chosenGenerator = ici.next();
				if(i==generatorChoice) {
					break;
				}
				i++;
			}
			
			robot.settings.saveConfig();

			String destinationFile = gui.getTempDestinationFile();

			chosenGenerator.setDrawPanel(gui.getDrawPanel());
			gui.getDrawPanel().setDecorator(chosenGenerator);

			chosenGenerator.setMachine(robot);
			chosenGenerator.generate(destinationFile);

			chosenGenerator.setDrawPanel(null);
			gui.getDrawPanel().setDecorator(null);

			LoadGCode loader = new LoadGCode();
			loader.load(destinationFile,robot,gui);
			
			SoundSystem.playConversionFinishedSound();

			// Force update of graphics layout.
			gui.updateMachineConfig();
			gui.getDrawPanel().repaintNow();
		}
	}

	public void saveFileDialog() {
		// Note: source for ExampleFileFilter can be found in FileChooserDemo,
		// under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		String filename = lastFileOut;

		FileFilter filterGCODE = new FileNameExtensionFilter(Translator.get("FileTypeGCode"), "ngc");

		JFileChooser fc = new JFileChooser(new File(filename));
		fc.addChoosableFileFilter(filterGCODE);
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();

			if (!selectedFile.toLowerCase().endsWith(".ngc")) {
				selectedFile += ".ngc";
			}

			try {
				gui.gCode.save(selectedFile);
			} catch (IOException e) {
				Log.error(Translator.get("Failed") + e.getMessage());
				return;
			}
		}
	}


	/**
	 * User has asked that a file be opened.
	 * @param filename the file to be opened.
	 */
	public void openFileOnDemand(String filename) {
		Log.message(Translator.get("OpeningFile") + filename + "...");
		boolean success=false;
		boolean attempted=false;

		ServiceLoader<LoadFileType> imageLoaders = ServiceLoader.load(LoadFileType.class);
		Iterator<LoadFileType> i = imageLoaders.iterator();
		while(i.hasNext()) {
			LoadFileType lft = i.next();
			if(lft.canLoad(filename)) {
				attempted=true;
				success=lft.load(filename, robot, gui);
				if(success==true) break;
			}
		}
		
		if(attempted == false) {
			Log.error(Translator.get("UnknownFileType"));
		}

		if (success == true) {
			lastFileIn = filename;
		}

		gui.updateMenuBar();
		statusBar.clear();
	}


	public void mouseClicked(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {
		mouseAction(e);
	}
	public void mouseEntered(MouseEvent e) {
		mouseInside=true;
	}
	public void mouseExited(MouseEvent e) {
		mouseInside=false;
		mouseOn=false;
	}
	public void mouseMoved(MouseEvent e) {
		mouseAction(e);
	}
	public void mousePressed(MouseEvent e) {
		mouseOn=true;
		mouseAction(e);
	}
	public void mouseReleased(MouseEvent e) {
		mouseOn=false;
	}
	public void mouseWheelMoved(MouseEvent e) {}

	public void mouseAction(MouseEvent e) {
		if(mouseInside && mouseOn) {
			double x = (double)e.getX();
			double y = (double)e.getY();
			Dimension d = dragAndDrive.getSize();
			double w = d.getWidth();
			double h = d.getHeight();
			double cx = w/2.0;
			double cy = h/2.0;
			x = x - cx;
			y = cy - y;
			x *= 10 * robot.settings.getPaperWidth()  / w;
			y *= 10 * robot.settings.getPaperHeight() / h;
			double dx = x-last_x;
			double dy = y-last_y;
			if(Math.sqrt(dx*dx+dy*dy)>=1) {
				last_x=x;
				last_y=y;
				String text = "X"+(Math.round(x*100)/100.0)+" Y"+(Math.round(y*100)/100.0);
				robot.sendLineToRobot("G00 "+text);
				coordinates.setText(text);
			} else {
				coordinates.setText("");
			}
		}
	}
}
