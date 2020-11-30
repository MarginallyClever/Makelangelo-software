package com.marginallyclever.makelangeloRobot;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.artPipeline.ArtPipelinePanel;
import com.marginallyclever.artPipeline.ImageManipulator;
import com.marginallyclever.artPipeline.generators.Generator_Text;
import com.marginallyclever.artPipeline.generators.ImageGenerator;
import com.marginallyclever.artPipeline.generators.ImageGeneratorPanel;
import com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveFileType;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.SpringUtilities;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.SoundSystem;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloSettingsDialog;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.enums.AWB;
import com.hopding.jrpicam.enums.DRC;
import com.hopding.jrpicam.enums.Encoding;
import com.hopding.jrpicam.enums.Exposure;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;

/**
 * Control panel for a Makelangelo robot
 *
 * @author dan royer
 * @author Peter Colapietro
 * @since 7.1.4
 */
public class MakelangeloRobotPanel extends JScrollPane implements ActionListener, ItemListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -4703402918904039337L;

	// god objects ?
	protected MakelangeloRobot robot;
	protected Makelangelo gui;

	// connect menu
	private CollapsiblePanel connectionPanel;
	private JButton buttonConnect;
	
	// machine options
	protected String lastFileIn = null;
	protected FileFilter lastFilterIn = null;
	protected String lastFileOut = null;
	protected FileFilter lastFilterOut = null;
	protected int generatorChoice = 0;
	
	private String[] machineConfigurations;
	private JComboBox<String> machineChoices;
	private JButton buttonOpenSettings;
	private JPanel machineNumberPanel;
	
	private JButton buttonOpenFile, buttonReopenFile, buttonNewFile, buttonCapture, buttonGenerate, buttonSaveFile;
	
	// picam controls
	private JButton	buttonCaptureImage, buttonUseCapture, buttonCancelCapture;
	private BufferedImage buffImg;
	private boolean useImage;
	private RPiCamera piCamera;
	private int awb, drc, exp, contrast, quality, sharpness;
    private static final int buttonHeight = 25;

    // live controls
    protected JButton buttonStart, buttonStartAt, buttonPause, buttonHalt;

	// driving controls
	private JButton down100,down10,down1,up1,up10,up100;
	private JButton left100,left10,left1,right1,right10,right100;
	private JButton goHome,findHome,setHome;
	private JButton goPaperBorder,penUp,penDown;
	private JButton toggleEngagedMotor;

	// pipeline controls
	private ArtPipelinePanel myArtPipelinePanel;
	
	private boolean isConnected;  // has pressed connect button
	
	public StatusBar statusBar;

	/**
	 * 
	 * @param gui
	 * @param robot
	 */
	public MakelangeloRobotPanel(Makelangelo gui, MakelangeloRobot robot) {
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

		panel.add(createConnectPanel(), con1);		con1.gridy++;
		
		// settings
		machineNumberPanel = new JPanel(new GridBagLayout());
		updateMachineNumberPanel();
		panel.add(machineNumberPanel, con1);
		con1.gridy++;

		// Create a piCamera
		try {
			piCamera = new RPiCamera("/home/pi/Pictures");
			// set the initial parameter settings.
			awb = 1;  // Auto
			drc = 1;  // High
			exp = 11; // VeryLong
			contrast = 0;
			quality = 75;
			sharpness = 0;
		} catch (FailedToRunRaspistillException e) {
			Log.message("Cannot run raspistill");
		}

		panel.add(createAxisDrivingControls(),con1);	con1.gridy++;
		panel.add(createCommonDriveControls(),con1);	con1.gridy++;
		panel.add(createCreativeControlPanel(), con1);	con1.gridy++;
		panel.add(createArtPipelinePanel(),con1);			con1.gridy++;
		panel.add(createAnimationPanel(),con1);			con1.gridy++;

		statusBar = new StatusBar();
		panel.add(statusBar, con1);
		con1.gridy++;

		// always have one extra empty at the end to push everything up.
		con1.weighty = 1;
		panel.add(new JLabel(), con1);
		
		// lastly, set the button states
		updateButtonAccess();
	}


	protected List<LoadAndSaveFileType> loadFileSavers() {
		return new ArrayList<LoadAndSaveFileType>();
	}


	public JButton createTightJButton(String label) {
		JButton b = new JButton(label);
		b.setMargin(new Insets(0,0,0,0));
		b.setPreferredSize(new Dimension(60,20));
		b.addActionListener(this);
		return b;
	}


	public JButton createNarrowJButton(String label) {
		JButton b = new JButton(label);
		b.setMargin(new Insets(0,0,0,0));
		b.setPreferredSize(new Dimension(40,20));
		b.addActionListener(this);
		return b;
	}


	protected JPanel createConnectPanel() {
		connectionPanel = new CollapsiblePanel(Translator.get("MenuConnect"));
		JPanel contents =connectionPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;
		
        buttonConnect = new JButton(Translator.get("ButtonConnect"));
        buttonConnect.addActionListener(this);
        buttonConnect.setForeground(Color.GREEN);

		contents.add(buttonConnect,con1);
		con1.gridy++;

	    return connectionPanel;
	}

	
	protected void closeConnection() {
		robot.halt();
		robot.closeConnection();
		buttonConnect.setText(Translator.get("ButtonConnect"));
		buttonConnect.setForeground(Color.GREEN);
		isConnected=false;
		updateButtonAccess();
	}
	
	protected void openConnection() {
		NetworkConnection s = gui.requestNewConnection();
		if(s!=null) {
			buttonConnect.setText(Translator.get("ButtonDisconnect"));
			buttonConnect.setForeground(Color.RED);
			robot.openConnection( s );
			//updateMachineNumberPanel();
			//updateButtonAccess();
			isConnected=true;
		}
	}
	

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object subject = e.getSource();

		if(subject == machineChoices && e.getStateChange()==ItemEvent.SELECTED) {
			updateMachineChoice();
		}
	}
	
	protected void updateMachineChoice() {
		int selectedIndex = machineChoices.getSelectedIndex();
		long newUID = Long.parseLong(machineChoices.getItemAt(selectedIndex));
		robot.getSettings().loadConfig(newUID);
	}

	
	private JPanel createAnimationPanel() {
		CollapsiblePanel animationPanel = new CollapsiblePanel(Translator.get("MenuAnimate"));
		JPanel drivePanel = animationPanel.getContentPane();
		
		drivePanel.setLayout(new GridLayout(4,1));
		buttonStart = new JButton(Translator.get("Start"));
		buttonStartAt = new JButton(Translator.get("StartAtLine"));
		buttonPause = new JButton(Translator.get("Pause"));
		buttonHalt = new JButton(Translator.get("Halt"));
		buttonStart.addActionListener(this);
		buttonStartAt.addActionListener(this);
		buttonPause.addActionListener(this);
		buttonHalt.addActionListener(this);

		drivePanel.add(buttonStart);
		drivePanel.add(buttonStartAt);
		drivePanel.add(buttonPause);
		drivePanel.add(buttonHalt);
		
		return animationPanel;
	}
	

	private CollapsiblePanel createArtPipelinePanel() {
		myArtPipelinePanel = new ArtPipelinePanel(gui.getMainFrame());
		myArtPipelinePanel.setPipeline(robot.getPipeline());
		
		return myArtPipelinePanel;
	}
	
	private JPanel createCreativeControlPanel() {
		CollapsiblePanel creativeControlPanel = new CollapsiblePanel(Translator.get("MenuCreativeControl"));
		JPanel panel = creativeControlPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;

		buttonNewFile = new JButton(Translator.get("MenuNewFile"));
		buttonNewFile.addActionListener(this);
		panel.add(buttonNewFile, con1);
		con1.gridy++;

		if (piCamera != null) {
            buttonCapture = new JButton(Translator.get("MenuCaptureImage"));
            buttonCapture.addActionListener(this);
            panel.add(buttonCapture, con1);
            con1.gridy++;
        }

		buttonOpenFile = new JButton(Translator.get("MenuOpenFile"));
		buttonOpenFile.addActionListener(this);
		panel.add(buttonOpenFile, con1);
		con1.gridy++;
		
		buttonReopenFile = new JButton(Translator.get("MenuReopenFile"));
		buttonReopenFile.addActionListener(this);
		panel.add(buttonReopenFile, con1);
		con1.gridy++;

		buttonGenerate = new JButton(Translator.get("MenuGenerate"));
		buttonGenerate.addActionListener(this);
		panel.add(buttonGenerate, con1);
		con1.gridy++;

		buttonSaveFile = new JButton(Translator.get("MenuSaveGCODEAs"));
		buttonSaveFile.addActionListener(this);
		panel.add(buttonSaveFile, con1);
		con1.gridy++;
		
		return creativeControlPanel;
	}
	
	private CollapsiblePanel createAxisDrivingControls() {
		CollapsiblePanel drivePanel = new CollapsiblePanel(Translator.get("MenuAxisDriveControls"));
		JPanel mainPanel = drivePanel.getContentPane();
		mainPanel.setLayout(new GridBagLayout());
		final GridBagConstraints cMain = new GridBagConstraints();
		cMain.fill=GridBagConstraints.HORIZONTAL;
		cMain.anchor=GridBagConstraints.NORTH;
		cMain.gridx=0;
		cMain.gridy=0;

		// manual axis driving
		JPanel axisControl = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		mainPanel.add(axisControl,cMain);
		cMain.gridy++;

		setHome = createTightJButton(Translator.get("SetHome"));
	    setHome.setPreferredSize(new Dimension(100,20));

		down100 = createTightJButton("-100");
		down10 = createTightJButton("-10");
		down1 = createTightJButton("-1");

		up1 = createTightJButton("1");
		up10 = createTightJButton("10");
		up100 = createTightJButton("100");

		left100 = createNarrowJButton("-100");
		left10 = createNarrowJButton("-10");
		left1 = createNarrowJButton("-1");
		
		right1 = createNarrowJButton("1");
		right10 = createNarrowJButton("10");
		right100 = createNarrowJButton("100");

		c.fill=GridBagConstraints.BOTH;
		c.gridx=3;  c.gridy=6;  axisControl.add(down100,c);
		c.gridx=3;  c.gridy=5;  axisControl.add(down10,c);
		c.gridx=3;  c.gridy=4;  axisControl.add(down1,c);

		c.gridx=3;  c.gridy=3;  axisControl.add(setHome,c);
		c.gridx=3;  c.gridy=2;  axisControl.add(up1,c);
		c.gridx=3;  c.gridy=1;  axisControl.add(up10,c);
		c.gridx=3;  c.gridy=0;  axisControl.add(up100,c);

		c.gridx=0;  c.gridy=3;  axisControl.add(left100,c);
		c.gridx=1;  c.gridy=3;  axisControl.add(left10,c);
		c.gridx=2;  c.gridy=3;  axisControl.add(left1,c);
		c.gridx=4;  c.gridy=3;  axisControl.add(right1,c);
		c.gridx=5;  c.gridy=3;  axisControl.add(right10,c);
		c.gridx=6;  c.gridy=3;  axisControl.add(right100,c);
		
		drivePanel.setCollapsed(true);
		
		return drivePanel;
	}
	
	private JPanel createCommonDriveControls() {
		CollapsiblePanel drivePanel = new CollapsiblePanel(Translator.get("MenuCommonDriveControls"));
		JPanel mainPanel = drivePanel.getContentPane();
		mainPanel.setLayout(new SpringLayout());

		goPaperBorder = new JButton(Translator.get("GoPaperBorder"));
		penUp    = new JButton(Translator.get("PenUp"));
		penDown  = new JButton(Translator.get("PenDown"));
		goHome   = new JButton(Translator.get("GoHome"));
		findHome = new JButton(Translator.get("FindHome"));
		toggleEngagedMotor = new JButton(Translator.get("DisengageMotors"));

		goPaperBorder.setMinimumSize(new Dimension(100,20));
		penUp   .setMinimumSize(new Dimension(100,20));
		penDown .setMinimumSize(new Dimension(100,20));
		goHome  .setMinimumSize(new Dimension(100,20));
		findHome.setMinimumSize(new Dimension(100,20));
		toggleEngagedMotor.setMinimumSize(new Dimension(100,20));
		
		mainPanel.add(goPaperBorder);
		mainPanel.add(toggleEngagedMotor);
		mainPanel.add(penUp);
		mainPanel.add(penDown);
		mainPanel.add(goHome);
		mainPanel.add(findHome);
		
		SpringUtilities.makeCompactGrid(mainPanel, 3, 2, 0, 0, 0, 0);
		
		goPaperBorder.addActionListener(this);
		toggleEngagedMotor.addActionListener(this);
		penUp.addActionListener(this);
		penDown.addActionListener(this);
		goHome.addActionListener(this);
		findHome.addActionListener(this);

		return drivePanel;
	}

	
	/**
	 * Refresh the list of available machine settings.  If we are connected to a machine, select that setting and disable the selection.
	 */
	public void updateMachineNumberPanel() {
		machineNumberPanel.removeAll();
		machineConfigurations = robot.getSettings().getKnownMachineNames();
		GridBagConstraints cMachine = new GridBagConstraints();
		cMachine.fill= GridBagConstraints.HORIZONTAL;
		cMachine.anchor = GridBagConstraints.CENTER;
		cMachine.gridx=0;
		cMachine.gridy=0;
		
		if( machineConfigurations.length>0 ) {
			machineChoices = new JComboBox<>(machineConfigurations);
			JLabel label = new JLabel(Translator.get("MachineNumber"));
			cMachine.insets = new Insets(0,0,0,5);
			machineNumberPanel.add(label,cMachine);
			cMachine.insets = new Insets(0,0,0,0);

			cMachine.gridx++;
			machineNumberPanel.add(machineChoices,cMachine);
			cMachine.gridx++;
			
			// if we're connected to a confirmed machine, don't let the user change the number panel or settings could get...weird.
			boolean state=false;
			if( robot.getConnection() == null ) state=true;
			else if( robot.getConnection().isOpen() == false ) state=true;
			else if( robot.isPortConfirmed() == false ) state=true;
			
			machineChoices.setEnabled( state );
			machineChoices.addItemListener(this);

			int index = robot.getSettings().getKnownMachineIndex();
			if( index<0 ) index=0;
			machineChoices.setSelectedIndex(index);

			// force the GUI to load the correct initial choice.
			updateMachineChoice();
		}

		buttonOpenSettings = new JButton(Translator.get("configureMachine"));
		buttonOpenSettings.addActionListener(this);
		buttonOpenSettings.setPreferredSize(buttonOpenSettings.getPreferredSize());
		machineNumberPanel.add(buttonOpenSettings,cMachine);
		cMachine.gridx++;
	}
	
	// The user has done something. respond to it.
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if( subject == buttonConnect ) {
			if(isConnected) {
				closeConnection();
			} else {
				openConnection();
			}
		}
		else if (subject == buttonOpenSettings) {
			Frame frame = (Frame)this.getRootPane().getParent();
			MakelangeloSettingsDialog m = new MakelangeloSettingsDialog(frame, robot);
			m.run();
			// we can only get here if the robot is connected and not running.
			// Save the gcode so that updates to settings are applied immediately + automatically.
			robot.saveCurrentTurtleToDrawing();
		}
		else if (subject == buttonNewFile) newFile();
		else if (subject == buttonCapture) captureFile();
		else if (subject == buttonOpenFile) openFile();
		else if (subject == buttonReopenFile) reopenFile();
		else if (subject == buttonGenerate) generateImage();
		else if (subject == buttonSaveFile) saveFileDialog();
		else if (subject == buttonStart) robot.startAt(0);
		else if (subject == buttonStartAt) startAt();
		else if (subject == buttonPause) {
			// toggle pause
			if (robot.isPaused() == true) {
				buttonPause.setText(Translator.get("Pause"));
				robot.unPause();
				robot.sendFileCommand();
			} else {
				buttonPause.setText(Translator.get("Unpause"));
				robot.pause();
			}
			return;
		}
		else if (subject == buttonHalt) robot.halt();		
		else if (subject == goHome  ) robot.goHome();
		else if (subject == findHome) robot.findHome();
		else if (subject == setHome ) {
			robot.setHome();
			updateButtonAccess();
		}
		else if (subject == goPaperBorder) {
			robot.movePenToEdgeTop();
			robot.lowerPen();
			robot.movePenToEdgeRight();
			robot.movePenToEdgeBottom();
			robot.movePenToEdgeLeft();
			robot.movePenToEdgeTop();
			robot.movePenAbsolute(0, robot.getPenY());
			robot.raisePen();
			robot.goHome();
		}
		else if (subject == penUp   ) robot.raisePen();
		else if (subject == penDown ) robot.lowerPen();
		else if (subject == toggleEngagedMotor) {
			if(robot.areMotorsEngaged() ) {
				disengageMotors();
			} else {
				engageMotors();
			}
		} else {
			float dx=0;
			float dy=0;
			
			if (subject == down100) dy = -100;
			if (subject == down10) dy = -10;
			if (subject == down1) dy = -1;
			if (subject == up100) dy = 100;
			if (subject == up10) dy = 10;
			if (subject == up1) dy = 1;
			
			if (subject == left100) dx = -100;
			if (subject == left10) dx = -10;
			if (subject == left1) dx = -1;
			if (subject == right100) dx = 100;
			if (subject == right10) dx = 10;
			if (subject == right1) dx = 1;

			if(dx!=0 || dy!=0) robot.movePenRelative(dx,dy);
		}
	}

	protected void disengageMotors() {
		robot.disengageMotors();
	}

	protected void engageMotors() {
		robot.engageMotors();
	}
	
	public void motorsHaveBeenDisengaged() {
		toggleEngagedMotor.setText(Translator.get("EngageMotors"));
	}
	public void motorsHaveBeenEngaged() {
		toggleEngagedMotor.setText(Translator.get("DisengageMotors"));
	}

	protected void startAt() {
		StartAtPanel p = new StartAtPanel();
		if(p.run(gui.getMainFrame())==false) return;

		int lineNumber = p.lineNumber;
		if (lineNumber != -1) {
			if(p.findPreviousPenDown==false) {
				robot.drawingProgress=lineNumber;
				if(p.addPenDownCommand==true) {
					robot.sendLineToRobot(robot.getSettings().getPenDownString());
				}
				robot.startAt(lineNumber);
			} else {
				int lineBefore = robot.findLastPenUpBefore(lineNumber);
				robot.startAt(lineBefore);
			}
		}
	}

	/**
	 * the moment a robot is confirmed to have connected 
	 */
	public void onConnect() {
		updateMachineNumberPanel();
		updateButtonAccess();
		engageMotors();
	}
	
	public void updateButtonAccess() {
		boolean isConfirmed=false;
		boolean isRunning=false;
		boolean didSetHome=false;
		
		if(robot!=null) {
			isConfirmed = robot.isPortConfirmed();
			isRunning = robot.isRunning();
			didSetHome = robot.didSetHome();
		}
		
		if (buttonGenerate != null)
			buttonGenerate.setEnabled(!isRunning);

		buttonOpenSettings.setEnabled(!isRunning);

		buttonStart.setEnabled(isConfirmed && didSetHome && !isRunning);
		buttonStartAt.setEnabled(isConfirmed && didSetHome && !isRunning);
		buttonPause.setEnabled(isConfirmed && isRunning);
		buttonHalt.setEnabled(isConfirmed && isRunning);

		if (!isConfirmed) {
			buttonPause.setText(Translator.get("Pause"));
		}

		toggleEngagedMotor.setEnabled(isConfirmed && !isRunning);
		buttonNewFile.setEnabled(!isRunning);
		if (piCamera != null) {
            buttonCapture.setEnabled(!isRunning);
        }
		buttonOpenFile.setEnabled(!isRunning);
		buttonReopenFile.setEnabled(!isRunning && lastFileIn!=null && !lastFileIn.isEmpty());
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

		goPaperBorder.setEnabled(isConfirmed && !isRunning && didSetHome);
		setHome .setEnabled( isConfirmed && !isRunning && !robot.getSettings().getHardwareProperties().canAutoHome() );
		findHome.setEnabled( isConfirmed && !isRunning &&  robot.getSettings().getHardwareProperties().canAutoHome() );
		goHome.setEnabled(isConfirmed && !isRunning && didSetHome);
		
		penUp.setEnabled(isConfirmed && !isRunning);
		penDown.setEnabled(isConfirmed && !isRunning);

		buttonSaveFile.setEnabled(robot!=null && robot.getTurtle().history.size()>0);
		
		this.validate();
	}
	

	public void newFile() {
		robot.setTurtle(new Turtle());
		updateButtonAccess();
	}

	
	private void reopenFile() {
		openFileOnDemand(lastFileIn);
	}

	/**
	 * Raspi camera capture to file for image processing
	 */
	private void captureFile() {
        // let's make the image the correct width and height for the paper
		useImage = false;
        int captureH = 650;
        int captureW = (int) ((double) captureH * robot.getSettings().getPaperWidth() / robot.getSettings().getPaperHeight());

		JDialog dialog = new JDialog(gui.getMainFrame(),Translator.get("CaptureImageTitle"), true);
        dialog.setLocation(gui.getMainFrame().getLocation());

        final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		final GridBagConstraints cMain = new GridBagConstraints();
		cMain.fill=GridBagConstraints.HORIZONTAL;
		cMain.anchor=GridBagConstraints.NORTH;
		cMain.gridx=0;
		cMain.gridy=0;
        cMain.gridheight = 1;
        cMain.gridwidth = 1;

		// create a frame to adjust the image

		panel.setBounds(1024, 100, 700, captureH);

        // if you add more things to the right side, you must increase this.
        cMain.gridheight = 16;
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(captureW, captureH));
  		panel.add(imageLabel, cMain);
        cMain.gridheight = 1;

        // all controls to the right
		cMain.gridx++;

		JLabel label = new JLabel(Translator.get("AWB"));
		label.setPreferredSize(new Dimension(100,buttonHeight));
		panel.add(label, cMain);
		cMain.gridy++;

		String[] awbComboBoxChoices = {
		        Translator.get("Off"),
                Translator.get("Auto"),
                Translator.get("Sun"),
                Translator.get("Cloud"),
                Translator.get("Shade"),
                Translator.get("Tungsten"),
                Translator.get("Fluorescent"),
                Translator.get("Incandescent"),
                Translator.get("Flash"),
                Translator.get("Horizon") };
		JComboBox<String> awbComboBox = new JComboBox<>(awbComboBoxChoices);
		awbComboBox.setPreferredSize(new Dimension(100,buttonHeight));
		awbComboBox.setSelectedIndex(awb);
		panel.add(awbComboBox, cMain);
		cMain.gridy++;

		JLabel lblNewLabel = new JLabel(Translator.get("DRC"));
		lblNewLabel.setPreferredSize(new Dimension(100,buttonHeight));
		panel.add(lblNewLabel, cMain);
		cMain.gridy++;

		String[] drcComboBoxChoices = {
                Translator.get("Off"),
                Translator.get("High"),
                Translator.get("Medium"),
                Translator.get("Low") };
		JComboBox<String> drcComboBox = new JComboBox<>(drcComboBoxChoices);
		drcComboBox.setPreferredSize(new Dimension(100,buttonHeight));
		drcComboBox.setSelectedIndex(drc);
		panel.add(drcComboBox, cMain);
		cMain.gridy++;

		JLabel label_1 = new JLabel(Translator.get("Exposure"));
		label_1.setPreferredSize(new Dimension(100,buttonHeight));
		panel.add(label_1, cMain);
		cMain.gridy++;

		String[] expComboBoxChoices = {
                Translator.get("Antishake"),
                Translator.get("Auto"),
                Translator.get("Backlight"),
                Translator.get("Beach"),
                Translator.get("Fireworks"),
                Translator.get("FixedFPS"),
                Translator.get("Night"),
                Translator.get("NightPreview"),
                Translator.get("Snow"),
                Translator.get("Sports"),
                Translator.get("Spotlight"),
                Translator.get("Verylong") };
		JComboBox<String> expComboBox = new JComboBox<>(expComboBoxChoices);
//		expComboBox.setBounds(584, 362, 90, 20);
		expComboBox.setPreferredSize(new Dimension(100,buttonHeight));
		expComboBox.setSelectedIndex(exp);
		panel.add(expComboBox, cMain);
		cMain.gridy++;

		JLabel lblContrast = new JLabel(Translator.get("Contrast"));
//		lblContrast.setBounds(588, 393, 67, 14);
		lblContrast.setPreferredSize(new Dimension(100,buttonHeight));
		panel.add(lblContrast, cMain);
		cMain.gridy++;

		JSlider contrastSlider = new JSlider();
		contrastSlider.setMinimum(-100);
//		contrastSlider.setBounds(588, 418, 90, 23);
		contrastSlider.setValue(contrast);
		panel.add(contrastSlider, cMain);
		cMain.gridy++;

		JLabel lblQuality = new JLabel(Translator.get("Quality"));
//		lblQuality.setBounds(588, 452, 46, 14);
		lblQuality.setPreferredSize(new Dimension(100,buttonHeight));
		panel.add(lblQuality, cMain);
		cMain.gridy++;

		JSlider qualitySlider = new JSlider();
		qualitySlider.setValue(quality);
//		qualitySlider.setBounds(584, 477, 90, 29);
		panel.add(qualitySlider, cMain);
		cMain.gridy++;

		JLabel lblSharpness = new JLabel(Translator.get("Sharpness"));
//		lblSharpness.setBounds(585, 517, 66, 14);
		lblSharpness.setPreferredSize(new Dimension(100,buttonHeight));
		panel.add(lblSharpness, cMain);
		cMain.gridy++;

		JSlider sharpnessSlider = new JSlider();
		sharpnessSlider.setMinimum(-100);
		sharpnessSlider.setValue(sharpness);
//		sharpnessSlider.setBounds(588, 542, 90, 23);
		panel.add(sharpnessSlider, cMain);
		cMain.gridy++;

		// I need 3 buttons one for Capture and one for Use if we have captured an image and one to just Cancel

        // a little space between everything else
        cMain.insets = new Insets(10,0,0,0);  //top padding

        buttonCaptureImage = new JButton(Translator.get("CaptureImage"));
		buttonCaptureImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					piCamera.turnOnPreview(gui.getMainFrame().getLocationOnScreen().x + 50, gui.getMainFrame().getLocationOnScreen().y + 100, captureW, captureH);
					piCamera.setAWB(AWB.valueOf(((String) awbComboBox.getSelectedItem()).toUpperCase()));
					piCamera.setDRC(DRC.valueOf(((String) drcComboBox.getSelectedItem()).toUpperCase()));
					piCamera.setExposure(Exposure.valueOf(((String) expComboBox.getSelectedItem()).toUpperCase()));
					piCamera.setEncoding(Encoding.JPG);
					piCamera.setWidth(captureW);
					piCamera.setHeight(captureH);
					piCamera.setContrast(contrastSlider.getValue());
					piCamera.setQuality(qualitySlider.getValue());
					piCamera.setSharpness(sharpnessSlider.getValue());
					piCamera.setTimeout(3000);
					buffImg = piCamera.takeBufferedStill();
					Log.message("Executed this command:\n\t" + piCamera.getPrevCommand());
					ImageIcon icon = new ImageIcon(buffImg);
					imageLabel.setIcon(icon);
					buttonUseCapture.setEnabled(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		buttonCaptureImage.setPreferredSize(new Dimension(89, buttonHeight));
		panel.add(buttonCaptureImage, cMain);
		cMain.gridy++;
        cMain.insets = new Insets(2,0,0,0);  //top padding

		buttonUseCapture = new JButton(Translator.get("UseCapture"));
		buttonUseCapture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// we like this image, save off the parameters used.
				awb = awbComboBox.getSelectedIndex();
				drc = drcComboBox.getSelectedIndex();
				exp = expComboBox.getSelectedIndex();
				contrast = contrastSlider.getValue();
				quality = qualitySlider.getValue();
				sharpness = sharpnessSlider.getValue();

				File saveFile = new File("/home/pi/Pictures/capture.jpg");
				try {
					ImageIO.write(buffImg, "jpg", saveFile);
					useImage = true;
				} catch (IOException e) {
					e.printStackTrace();
				}

				dialog.dispose();
			};
		});
		buttonUseCapture.setPreferredSize(new Dimension(89, buttonHeight));
		buttonUseCapture.setEnabled(false);
		panel.add(buttonUseCapture, cMain);
		cMain.gridy++;

		buttonCancelCapture = new JButton(Translator.get("CancelCapture"));
		buttonCancelCapture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dialog.dispose();
				useImage = false;
			};
		});
		buttonCancelCapture.setPreferredSize(new Dimension(89, buttonHeight));
		buttonCancelCapture.setEnabled(true);
		panel.add(buttonCancelCapture, cMain);

//		piCamera.setAWB(AWB.AUTO);	    // Change Automatic White Balance setting to automatic
//		piCamera.setDRC(DRC.OFF); 			// Turn off Dynamic Range Compression
//		piCamera.setContrast(100); 			// Set maximum contrast
//		piCamera.setSharpness(100);		    // Set maximum sharpness
//		piCamera.setQuality(100); 		    // Set maximum quality
//		piCamera.setTimeout(10000);		    // Wait 1 second to take the image
//		piCamera.turnOnPreview(200, 200, captureW, captureH);            // Turn on image preview
//		piCamera.setEncoding(Encoding.JPG); // Change encoding of images to PNG

		// Take a still image and save it as "/home/pi/Pictures/cameraCapture.jpg"

		Log.message("We are about to display dialog\n");
		dialog.add(panel);
		dialog.pack();
		dialog.setVisible(true);
//			Log.message("We are about to take a still image\n");
//			File image = piCamera.takeStill("cameraCapture.jpg", captureW, captureH);
//			Log.message("New JPG capture saved to:\n\t" + image.getAbsolutePath());
//			piCamera.turnOffPreview();
		// setup for reopen

		if (useImage) {
			// process the image
			openFileOnDemand("/home/pi/Pictures/capture.jpg");
		}
	}

	public void openFile() {
		// list available loaders
		File lastDir = (lastFileIn==null?null : new File(lastFileIn));
		JFileChooser fc = new JFileChooser(lastDir);
		ServiceLoader<LoadAndSaveFileType> imageLoaders = ServiceLoader.load(LoadAndSaveFileType.class);
		for( LoadAndSaveFileType lft : imageLoaders ) {
			if(lft.canLoad()) {
				FileFilter filter = lft.getFileNameFilter();
				fc.addChoosableFileFilter(filter);
			}
		}
		
		// no wild card filter, please.
		fc.setAcceptAllFileFilterUsed(false);
		// remember the last path & filter used.
		if(lastFilterIn!=null) fc.setFileFilter(lastFilterIn);
		
		// run the dialog
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter)fc.getFileFilter();

			// figure out which of the loaders was requested.
			for( LoadAndSaveFileType loader : imageLoaders ) {
				if( !isMatchingFileFilter(selectedFilter, (FileNameExtensionFilter)loader.getFileNameFilter()) ) continue;
				boolean success = openFileOnDemandWithLoader(selectedFile,loader);
				if(success) {
					lastFilterIn = selectedFilter;
					updateButtonAccess();
					break;
				}
			}
		}
	}
	
	public void generateImage() {
		// set up a card layout (https://docs.oracle.com/javase/tutorial/uiswing/layout/card.html)
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel cards = new JPanel(new CardLayout());
		ServiceLoader<ImageGenerator> imageGenerators = ServiceLoader.load(ImageGenerator.class);
		int i=0;
		for( ImageGenerator ici : imageGenerators ) {
			cards.add(ici.getPanel().getPanel(),ici.getName());
			i++;
		}
		
		String[] imageGeneratorNames = new String[i];
		
		i=0;
		for( ImageManipulator f : imageGenerators ) {
			imageGeneratorNames[i++] = f.getName();
		}

		final JComboBox<String> options = new JComboBox<String>(imageGeneratorNames);
		options.setSelectedIndex(generatorChoice);

		panel.add(options,BorderLayout.PAGE_START);
		panel.add(cards,BorderLayout.CENTER);

		options.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
			    CardLayout cl = (CardLayout)(cards.getLayout());
			    cl.show(cards, (String)e.getItem());
			    
				changeGeneratorPanel(options.getSelectedIndex());
			}
		});
		
		changeGeneratorPanel(options.getSelectedIndex());
		
		JDialog dialog = new JDialog(gui.getMainFrame(),Translator.get("MenuGenerate"));
		dialog.add(panel);
		dialog.pack();
		dialog.setVisible(true);
		// other app buttons are still accessible.
	}

	private void changeGeneratorPanel(int index) {
		ImageGeneratorPanel.makelangeloRobotPanel = this;
		ImageGenerator chosenGenerator = getGenerator(index);
		ImageGeneratorPanel chosenGeneratorPanel = chosenGenerator.getPanel();
		if(chosenGeneratorPanel!=null) {
			Log.message("Generator="+chosenGenerator.getName());
			JPanel p = chosenGeneratorPanel.getPanel();
			p.setBorder(BorderFactory.createLineBorder(Color.RED));
			try {
				regenerate(chosenGenerator);
			} catch(Exception e){}
		}
	}
	
	public void regenerate(ImageGenerator chosenGenerator) {
		robot.setDecorator(chosenGenerator);
		chosenGenerator.setRobot(robot);
		
		// do the work
		chosenGenerator.generate();
		robot.getSettings().setRotationRef(0);
				
		Turtle t=chosenGenerator.turtle;

		if (robot.getSettings().shouldSignName()) {
			// Sign name
			Generator_Text ymh = new Generator_Text();
			ymh.setRobot(robot);
			ymh.signName();
			t.history.addAll(ymh.turtle.history);
		}
		robot.setDecorator(null);
		robot.setTurtle(t);
		Log.message(Translator.get("Finished"));
		SoundSystem.playConversionFinishedSound();
		updateButtonAccess();
	}

	private ImageGenerator getGenerator(int arg0) throws IndexOutOfBoundsException {
		ServiceLoader<ImageGenerator> imageGenerators = ServiceLoader.load(ImageGenerator.class);
		int i=0;
		for( ImageGenerator chosenGenerator : imageGenerators ) {
			if(i==arg0) {
				return chosenGenerator;
			}
			i++;
		}
		
		throw new IndexOutOfBoundsException();
	}
	
	public void saveFileDialog() {
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
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
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
					success=lft.save(fileOutputStream,robot);
				} catch(IOException e) {
					JOptionPane.showMessageDialog(gui.getMainFrame(), "Save failed: "+e.getMessage());
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
			success=loader.load(fileInputStream,robot);
		} catch(IOException e) {
			e.printStackTrace();
		}

		// TODO don't rely on success to be true, load may not have finished yet.

		if (success == true) {
			lastFileIn=filename;
			Log.message(Translator.get("Finished"));
			SoundSystem.playConversionFinishedSound();
			updateButtonAccess();
			statusBar.clear();
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
		boolean success=false;
		boolean attempted=false;

		ServiceLoader<LoadAndSaveFileType> imageLoaders = ServiceLoader.load(LoadAndSaveFileType.class);
		Iterator<LoadAndSaveFileType> i = imageLoaders.iterator();
		while(i.hasNext()) {
			LoadAndSaveFileType loader = i.next();
			if(!loader.canLoad()) continue;
			if(!loader.canLoad(filename)) continue;
			
			attempted=true;
			success=openFileOnDemandWithLoader(filename,loader);
			if(success==true) break;
		}
		
		if(attempted == false) {
			Log.error(Translator.get("UnknownFileType"));
		}
		
		return success;
	}
	
	public boolean canLoad(String filename) {
		ServiceLoader<LoadAndSaveFileType> imageLoaders = ServiceLoader.load(LoadAndSaveFileType.class);
		Iterator<LoadAndSaveFileType> i = imageLoaders.iterator();
		while(i.hasNext()) {
			LoadAndSaveFileType loader = i.next();
			if(!loader.canLoad()) continue;
			if(!loader.canLoad(filename)) continue;
			// potentially yes, we can load this type.
			return true;
		}
		// nothing can load this type
		return false;
	}


	public Makelangelo getGui() {
		return gui;
	}
}
