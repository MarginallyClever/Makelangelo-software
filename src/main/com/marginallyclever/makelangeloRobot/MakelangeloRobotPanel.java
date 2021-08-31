package com.marginallyclever.makelangeloRobot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.communications.ConnectionManager;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloSettingsDialog;


/**
 * Control panel for a Makelangelo robot
 *
 * @author Dan Royer
 * @author Peter Colapietro
 * @since 7.1.4
 */
public class MakelangeloRobotPanel extends JPanel implements MakelangeloRobotListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -4703402918904039337L;

	// god objects?
	private MakelangeloRobot myRobot;
	private Makelangelo makelangeloApp;
	
	// connect menu
	private SelectPanel connectionPanel;
	private SelectButton buttonConnect;
	
	// machine options
	protected int generatorChoice = 0;
	
	private String[] machineConfigurations;
	private JComboBox<String> machineChoices;
	private JButton buttonOpenSettings;
	private JPanel machineNumberPanel;
	
	private SelectButton buttonCapture;

    // live controls
	private SelectButton buttonStart, buttonStartAt, buttonPause, buttonHalt;

	// driving controls
	private CartesianButtons driveButtons;
	private SelectButton goHome,findHome;
	private SelectButton goPaperBorder,penUp,penDown;
	private SelectButton toggleEngagedMotor;

	private boolean isConnected;  // has pressed connect button
	
	public StatusBar statusBar;

	/**
	 * @param gui
	 * @param myRobot
	 */
	public MakelangeloRobotPanel(Makelangelo gui, MakelangeloRobot robot) {
		super();
		
		makelangeloApp = gui;
		myRobot = robot;
		robot.addListener(this);		
		
		this.removeAll();
		this.setBorder(BorderFactory.createEmptyBorder());
		setLayout(new GridBagLayout());

		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx = 0;
		con1.gridy = 0;
		con1.weightx = 1;
		con1.weighty = 0;
		con1.fill = GridBagConstraints.HORIZONTAL;
		con1.anchor = GridBagConstraints.NORTHWEST;

		add(createConnectSubPanel(), con1);
		con1.gridy++;
		
		// settings
		machineNumberPanel = new JPanel(new GridBagLayout());
		updateMachineNumberPanel();
		add(machineNumberPanel, con1);
		con1.gridy++;

		add(createAxisDrivingControls(),con1);		con1.gridy++;
		add(createCommonDriveControls(),con1);		con1.gridy++;
		add(createAnimationPanel(),con1);			con1.gridy++;

		statusBar = new StatusBar();
		add(statusBar, con1);
		con1.gridy++;

		// always have one extra empty at the end to push everything up.
		con1.weighty = 1;
		add(new JLabel(), con1);
		
		// lastly, set the button states
		updateButtonAccess();
	}

	private JPanel createConnectSubPanel() {
		connectionPanel = new SelectPanel();
				
        buttonConnect = new SelectButton(Translator.get("ButtonConnect"));
        buttonConnect.addPropertyChangeListener((evt)->{
			if(isConnected) {
				myRobot.halt();
				myRobot.closeConnection();
				buttonConnect.setText(Translator.get("ButtonConnect"));
				buttonConnect.setForeground(Color.GREEN);
				isConnected=false;
				updateButtonAccess();
			} else {
				NetworkConnection s = ConnectionManager.requestNewConnection(makelangeloApp.getMainFrame());
				if(s!=null) {
					buttonConnect.setText(Translator.get("ButtonDisconnect"));
					buttonConnect.setForeground(Color.RED);
					myRobot.openConnection( s );
					//updateMachineNumberPanel();
					//updateButtonAccess();
					isConnected=true;
				}
			}
		});
        buttonConnect.setForeground(Color.GREEN);

        connectionPanel.add(buttonConnect);

	    return connectionPanel;
	}
	
	protected void updateMachineChoice() {
		int selectedIndex = machineChoices.getSelectedIndex();
		long newUID = Long.parseLong(machineChoices.getItemAt(selectedIndex));
		myRobot.getSettings().loadConfig(newUID);
	}

	private JPanel createAnimationPanel() {
		CollapsiblePanel animationPanel = new CollapsiblePanel(Translator.get("MenuAnimate"));
		SelectPanel animationInterior = animationPanel.getContentPane();
		
		animationInterior.add(buttonStart = new SelectButton(Translator.get("Start")));
		animationInterior.add(buttonStartAt = new SelectButton(Translator.get("StartAtLine")));
		animationInterior.add(buttonPause = new SelectButton(Translator.get("Pause")));
		animationInterior.add(buttonHalt = new SelectButton(Translator.get("Halt")));
		
		buttonHalt		.addPropertyChangeListener((evt)->{	myRobot.halt();			});
		buttonStart		.addPropertyChangeListener((evt)->{	myRobot.startAt(0);		});
		buttonStartAt	.addPropertyChangeListener((evt)->{	startAt();				});
		buttonPause		.addPropertyChangeListener((evt)->{ updatePauseButton();	});
		
		return animationPanel;
	}

	private void updatePauseButton() {
		if (myRobot.isPaused()) {
			buttonPause.setText(Translator.get("Pause"));
			myRobot.unPause();
		} else {
			buttonPause.setText(Translator.get("Unpause"));
			myRobot.pause();
		}
	}
	
	private CollapsiblePanel createAxisDrivingControls() {
		CollapsiblePanel drivePanel = new CollapsiblePanel(Translator.get("MenuAxisDriveControls"));
		JPanel driveInterior = drivePanel.getContentPane();
		driveInterior.setLayout(new GridBagLayout());
		final GridBagConstraints cMain = new GridBagConstraints();
		cMain.fill=GridBagConstraints.HORIZONTAL;
		cMain.anchor=GridBagConstraints.NORTH;
		cMain.gridx=0;
		cMain.gridy=0;

		// manual axis driving
		JPanel axisControl = new JPanel(new GridBagLayout());
		driveInterior.add(axisControl,cMain);
		cMain.gridy++;

	    driveButtons = new CartesianButtons(); 
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx=0;
		c.gridy=0;
	    axisControl.add(driveButtons,c);
	    driveButtons.addActionListener((e)->{
	    	int id = e.getID();
	    	if(CartesianButtons.isCenterZone(id)) {
				myRobot.setHome();
				updateButtonAccess();
	    		return;
	    	}
	    	int q=CartesianButtons.getQuadrant(id);
	    	int z=CartesianButtons.getZone(id);
	    	int x,y;
	    	if((q%2)==0) {
	    		x=0;
	    		y=100;
	    	} else {
	    		x=100;
	    		y=0;
	    	}
	    	int scale = (int)Math.pow(10, z);
	    	x/=scale;
	    	y/=scale;
	    	System.out.println("Move "+x+","+y);
	    	//myRobot.movePenRelative(x,y);
	    });
		
		drivePanel.setCollapsed(false);
		
		return drivePanel;
	}
	
	private JPanel createCommonDriveControls() {
		CollapsiblePanel commonControlsPanel = new CollapsiblePanel(Translator.get("MenuCommonDriveControls"));
		SelectPanel commonInterior = commonControlsPanel.getContentPane();

		goPaperBorder = new SelectButton(Translator.get("GoPaperBorder"));
		penUp    = new SelectButton(Translator.get("PenUp"));
		penDown  = new SelectButton(Translator.get("PenDown"));
		goHome   = new SelectButton(Translator.get("GoHome"));
		findHome = new SelectButton(Translator.get("FindHome"));
		toggleEngagedMotor = new SelectButton(Translator.get("DisengageMotors"));
		
		commonInterior.add(goPaperBorder);
		commonInterior.add(toggleEngagedMotor);
		commonInterior.add(penUp);
		commonInterior.add(penDown);
		commonInterior.add(goHome);
		commonInterior.add(findHome);
		
		goPaperBorder.addPropertyChangeListener((evt)->{
			myRobot.movePenToEdgeTop();
			myRobot.lowerPen();
			myRobot.movePenToEdgeRight();
			myRobot.movePenToEdgeBottom();
			myRobot.movePenToEdgeLeft();
			myRobot.movePenToEdgeTop();
			myRobot.movePenAbsolute(0, myRobot.getPenY());
			myRobot.raisePen();
			myRobot.goHome();
		});
		toggleEngagedMotor.addPropertyChangeListener((evt)->{
			if(myRobot.areMotorsEngaged() ) myRobot.disengageMotors();
			else 							myRobot.engageMotors();
		});
		penUp	.addPropertyChangeListener((evt)->{  	myRobot.raisePen();		});
		penDown	.addPropertyChangeListener((evt)->{		myRobot.lowerPen();		});
		goHome	.addPropertyChangeListener((evt)->{		myRobot.goHome();		});
		findHome.addPropertyChangeListener((evt)->{		myRobot.findHome();		});

		commonControlsPanel.setCollapsed(true);
		
		return commonControlsPanel;
	}
	
	/**
	 * Refresh the list of available known machines. 
	 * If we are connected to a machine, select that machine number and disable the ability to change selection.
	 */
	private void updateMachineNumberPanel() {
		machineNumberPanel.removeAll();
		machineConfigurations = myRobot.getSettings().getKnownMachineNames();
		GridBagConstraints cMachine = new GridBagConstraints();
		cMachine.fill= GridBagConstraints.HORIZONTAL;
		cMachine.anchor = GridBagConstraints.CENTER;
		cMachine.gridx=0;
		cMachine.gridy=0;
		
		if( machineConfigurations.length>0 ) {
			machineChoices = new JComboBox<String>(machineConfigurations);
			JLabel label = new JLabel(Translator.get("MachineNumber"));
			cMachine.insets = new Insets(0,0,0,5);
			machineNumberPanel.add(label,cMachine);
			cMachine.insets = new Insets(0,0,0,0);

			cMachine.gridx++;
			machineNumberPanel.add(machineChoices,cMachine);
			cMachine.gridx++;
			
			// if we're connected to a confirmed machine, don't let the user change the number panel or settings could get...weird.
			boolean state=false;
			if( myRobot.getConnection() == null ) state=true;
			else if( myRobot.getConnection().isOpen() == false ) state=true;
			else if( myRobot.isPortConfirmed() == false ) state=true;
			
			machineChoices.setEnabled( state );
			machineChoices.addItemListener((e)->{
				if(e.getStateChange()==ItemEvent.SELECTED) updateMachineChoice();
			});

			int index = myRobot.getSettings().getKnownMachineIndex();
			if( index<0 ) index=0;
			machineChoices.setSelectedIndex(index);

			// force the GUI to load the correct initial choice.
			updateMachineChoice();
		}

		buttonOpenSettings = new JButton(Translator.get("configureMachine"));
		buttonOpenSettings.addActionListener((e)->{
			Frame frame = (Frame)getRootPane().getParent();
			MakelangeloSettingsDialog m = new MakelangeloSettingsDialog(frame, myRobot);
			m.run();
			// we can only get here if the robot is connected and not running.
			// Save the gcode so that updates to settings are applied immediately + automatically.
			myRobot.setTurtle(myRobot.getTurtle());
		});
		buttonOpenSettings.setPreferredSize(buttonOpenSettings.getPreferredSize());
		machineNumberPanel.add(buttonOpenSettings,cMachine);
		cMachine.gridx++;
	}
	
	private void motorsHaveBeenDisengaged() {
		toggleEngagedMotor.setText(Translator.get("EngageMotors"));
	}
	
	private void motorsHaveBeenEngaged() {
		toggleEngagedMotor.setText(Translator.get("DisengageMotors"));
	}

	private void startAt() {
		StartAtPanel p = new StartAtPanel();
		if(p.run(makelangeloApp.getMainFrame())==false) return;

		int lineNumber = p.getLineNumber();
		if (lineNumber != -1) {
			if(p.isFindPreviousPenDown()) lineNumber = myRobot.findLastPenUpBefore(lineNumber);
			if(p.isAddPenDownCommand()) myRobot.sendLineToRobot(myRobot.getSettings().getPenDownString());
			myRobot.startAt(lineNumber);
		}
	}
	
	// the moment a robot is confirmed to have connected
	private void onConnect() {
		updateMachineNumberPanel();
		updateButtonAccess();
		myRobot.engageMotors();
	}
		
	public void updateButtonAccess() {
		boolean isConfirmed=false;
		boolean isRunning=false;
		boolean didSetHome=false;
		
		if(myRobot!=null) {
			isConfirmed = myRobot.isPortConfirmed();
			isRunning = myRobot.isRunning();
			didSetHome = myRobot.didSetHome();
		}
		
		buttonOpenSettings.setEnabled(!isRunning);

		buttonStart.setEnabled(isConfirmed && didSetHome && !isRunning);
		buttonStartAt.setEnabled(isConfirmed && didSetHome && !isRunning);
		buttonPause.setEnabled(isConfirmed && isRunning);
		buttonHalt.setEnabled(isConfirmed && isRunning);

		if(!isConfirmed) buttonPause.setText(Translator.get("Pause"));

		toggleEngagedMotor.setEnabled(isConfirmed && !isRunning);
		
		if(buttonCapture != null) buttonCapture.setEnabled(!isRunning);
		
		driveButtons.setEnabled(isConfirmed && !isRunning);

		goPaperBorder.setEnabled(isConfirmed && !isRunning && didSetHome);
		findHome.setEnabled( isConfirmed && !isRunning &&  myRobot.getSettings().getHardwareProperties().canAutoHome() );
		goHome.setEnabled(isConfirmed && !isRunning && didSetHome);
		
		penUp.setEnabled(isConfirmed && !isRunning);
		penDown.setEnabled(isConfirmed && !isRunning);
		
		this.validate();
	}

	@Override
	public void makelangeloRobotUpdate(MakelangeloRobotEvent e) {
		if(e.type==MakelangeloRobotEvent.CONNECTION_READY) {
			String hardwareVersion = myRobot.getSettings().getHardwareVersion();
			onConnect();
			myRobot.getSettings().setHardwareVersion(hardwareVersion);
		}
		if(e.type==MakelangeloRobotEvent.DISCONNECT) {
			updateButtonAccess();
		}
		if(e.type==MakelangeloRobotEvent.START) {
			statusBar.start();
			updateButtonAccess();
		}
		if(e.type==MakelangeloRobotEvent.STOP) updateButtonAccess();
		if(e.type==MakelangeloRobotEvent.PROGRESS_SOFAR) { 
			statusBar.setProgress((int)e.extra, e.subject.getGCodeCommandsCount());
		}
		if(e.type==MakelangeloRobotEvent.NEW_GCODE) { 
			MakelangeloFirmwareSimulation m = new MakelangeloFirmwareSimulation(e.subject.getSettings());
			double eta= m.getTimeEstimate(e.subject.getTurtle());
			Log.message("Run time estimate=" +Log.secondsToHumanReadable(eta));

			statusBar.setProgressEstimate(eta, e.subject.getGCodeCommandsCount());
			updateButtonAccess();
		}
		if(e.type==MakelangeloRobotEvent.MOTORS_ENGAGED) {
			if(((boolean)e.extra)==true) motorsHaveBeenEngaged();
			else 						 motorsHaveBeenDisengaged();
		}
	}
}
