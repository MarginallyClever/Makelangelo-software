package com.marginallyclever.makelangeloRobot;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ServiceLoader;

import javax.swing.*;

import com.marginallyclever.artPipeline.ArtPipelinePanel;
import com.marginallyclever.artPipeline.ImageManipulator;
import com.marginallyclever.artPipeline.generators.Generator_Text;
import com.marginallyclever.artPipeline.generators.ImageGenerator;
import com.marginallyclever.artPipeline.generators.ImageGeneratorPanel;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.SoundSystem;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloSettingsDialog;

import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;


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
	
	private SelectButton buttonNewFile, buttonOpenFile, buttonReopenFile, buttonGenerate, buttonSaveFile;

	private PiCaptureAction piCameraCaptureAction;
	private SelectButton buttonCapture;

    // live controls
	private SelectButton buttonStart, buttonStartAt, buttonPause, buttonHalt;

	// driving controls
	private JButton down100,down10,down1,up1,up10,up100;
	private JButton left100,left10,left1,right1,right10,right100;
	private JButton setHome;
	private SelectButton goHome,findHome;
	private SelectButton goPaperBorder,penUp,penDown;
	private SelectButton toggleEngagedMotor;

	// pipeline controls
	private ArtPipelinePanel myArtPipelinePanel;
	
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

		try {
			piCameraCaptureAction = new PiCaptureAction(gui, Translator.get("MenuCaptureImage"));	
		} catch (FailedToRunRaspistillException e) {
			Log.message("Raspistill unavailable.");
		}

		add(createAxisDrivingControls(),con1);		con1.gridy++;
		add(createCommonDriveControls(),con1);		con1.gridy++;
		add(createCreativeControlPanel(), con1);	con1.gridy++;
		add(createArtPipelinePanel(),con1);			con1.gridy++;
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


	private JButton createTightJButton(String label) {
		JButton b = new JButton(label);
		b.setMargin(new Insets(0,0,0,0));
		b.setPreferredSize(new Dimension(60,20));
		return b;
	}


	private JButton createNarrowJButton(String label) {
		JButton b = new JButton(label);
		b.setMargin(new Insets(0,0,0,0));
		b.setPreferredSize(new Dimension(40,20));
		return b;
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
				NetworkConnection s = makelangeloApp.requestNewConnection();
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


	private CollapsiblePanel createArtPipelinePanel() {
		myArtPipelinePanel = new ArtPipelinePanel(makelangeloApp.getMainFrame());
		myArtPipelinePanel.setPipeline(myRobot.getPipeline());
		
		return myArtPipelinePanel;
	}
	
	
	private JPanel createCreativeControlPanel() {
		CollapsiblePanel creativeControlPanel = new CollapsiblePanel(Translator.get("MenuCreativeControl"));
		SelectPanel panel = creativeControlPanel.getContentPane();

		if (piCameraCaptureAction != null) {
            panel.add(buttonCapture = new SelectButton(piCameraCaptureAction));
        } else {
        	buttonCapture = null;
        }

		panel.add(buttonNewFile = new SelectButton(Translator.get("MenuNewFile")));
		panel.add(buttonOpenFile = new SelectButton(Translator.get("MenuOpenFile")));
		panel.add(buttonReopenFile = new SelectButton(Translator.get("MenuReopenFile")));
		panel.add(buttonGenerate = new SelectButton(Translator.get("MenuGenerate")));
		panel.add(buttonSaveFile = new SelectButton(Translator.get("MenuSaveGCODEAs")));

		buttonNewFile.addPropertyChangeListener((evt)->{		newFile();							});
		buttonOpenFile.addPropertyChangeListener((evt)->{		makelangeloApp.openFile();			});
		buttonReopenFile.addPropertyChangeListener((evt)->{		makelangeloApp.reopenLastFile();	});
		buttonGenerate.addPropertyChangeListener((evt)->{		generateImage();					});
		buttonSaveFile.addPropertyChangeListener((evt)->{		makelangeloApp.saveFile();			});
		
		return creativeControlPanel;
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
		GridBagConstraints c = new GridBagConstraints();
		driveInterior.add(axisControl,cMain);
		cMain.gridy++;

		setHome = createTightJButton(Translator.get("SetHome"));
	    setHome.setPreferredSize(new Dimension(100,20));
	    setHome.addActionListener((e)->{ 
			myRobot.setHome();
			updateButtonAccess();
		});

		down100 = createTightJButton("-100");		down100	.addActionListener((e)->{	myRobot.movePenRelative(0,-100);	});
		down10 = createTightJButton("-10");			down10	.addActionListener((e)->{	myRobot.movePenRelative(0,-10);		});
		down1 = createTightJButton("-1");			down1	.addActionListener((e)->{	myRobot.movePenRelative(0,-1);		});
		up1 = createTightJButton("1");				up1		.addActionListener((e)->{	myRobot.movePenRelative(0,1);		});
		up10 = createTightJButton("10");			up10	.addActionListener((e)->{	myRobot.movePenRelative(0,10);		});
		up100 = createTightJButton("100");			down100	.addActionListener((e)->{	myRobot.movePenRelative(0,100);		});

		left100 = createNarrowJButton("-100");		left100	.addActionListener((e)->{	myRobot.movePenRelative(-100,0);	});
		left10 = createNarrowJButton("-10");		left10	.addActionListener((e)->{	myRobot.movePenRelative(-10,0);		});
		left1 = createNarrowJButton("-1");			left1	.addActionListener((e)->{	myRobot.movePenRelative(-1,0);		});
		right1 = createNarrowJButton("1");			right1	.addActionListener((e)->{	myRobot.movePenRelative(1,0);		});
		right10 = createNarrowJButton("10");		right10	.addActionListener((e)->{	myRobot.movePenRelative(10,0);		});
		right100 = createNarrowJButton("100");		right100.addActionListener((e)->{	myRobot.movePenRelative(100,0);		});

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
			myRobot.saveTurtleToDrawing(myRobot.getTurtle());
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
		
		if(buttonGenerate != null) buttonGenerate.setEnabled(!isRunning);

		buttonOpenSettings.setEnabled(!isRunning);

		buttonStart.setEnabled(isConfirmed && didSetHome && !isRunning);
		buttonStartAt.setEnabled(isConfirmed && didSetHome && !isRunning);
		buttonPause.setEnabled(isConfirmed && isRunning);
		buttonHalt.setEnabled(isConfirmed && isRunning);

		if(!isConfirmed) buttonPause.setText(Translator.get("Pause"));

		toggleEngagedMotor.setEnabled(isConfirmed && !isRunning);
		buttonNewFile.setEnabled(!isRunning);
		
		if(buttonCapture != null) buttonCapture.setEnabled(!isRunning);
		
		buttonOpenFile.setEnabled(!isRunning);
		buttonReopenFile.setEnabled(!isRunning && !makelangeloApp.getLastFileIn().isEmpty());
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
		setHome .setEnabled( isConfirmed && !isRunning && !myRobot.getSettings().getHardwareProperties().canAutoHome() );
		findHome.setEnabled( isConfirmed && !isRunning &&  myRobot.getSettings().getHardwareProperties().canAutoHome() );
		goHome.setEnabled(isConfirmed && !isRunning && didSetHome);
		
		penUp.setEnabled(isConfirmed && !isRunning);
		penDown.setEnabled(isConfirmed && !isRunning);

		buttonSaveFile.setEnabled(myRobot!=null && myRobot.getTurtle().history.size()>0);
		
		this.validate();
	}
	

	private void newFile() {
		myRobot.setTurtle(new Turtle());
	}
	
	
	private void generateImage() {
		// set up a card layout (https://docs.oracle.com/javase/tutorial/uiswing/layout/card.html)
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final JPanel cards = new JPanel(new CardLayout());
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
		
		JDialog dialog = new JDialog(makelangeloApp.getMainFrame(),Translator.get("MenuGenerate"));
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
		myRobot.setDecorator(chosenGenerator);
		chosenGenerator.setRobot(myRobot);
		
		// do the work
		chosenGenerator.generate();
		myRobot.getSettings().setRotationRef(0);
				
		Turtle t=chosenGenerator.turtle;

		if (myRobot.getSettings().shouldSignName()) {
			// Sign name
			Generator_Text ymh = new Generator_Text();
			ymh.setRobot(myRobot);
			ymh.signName();
			t.history.addAll(ymh.turtle.history);
		}
		myRobot.setDecorator(null);
		myRobot.setTurtle(t);
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
			statusBar.setProgress((long)e.extra, e.subject.getGCodeCommandsCount());
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
