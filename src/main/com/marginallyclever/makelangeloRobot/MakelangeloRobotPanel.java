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
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.*;

import com.marginallyclever.artPipeline.ArtPipelinePanel;
import com.marginallyclever.artPipeline.ImageManipulator;
import com.marginallyclever.artPipeline.generators.Generator_Text;
import com.marginallyclever.artPipeline.generators.ImageGenerator;
import com.marginallyclever.artPipeline.generators.ImageGeneratorPanel;
import com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveFileType;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.SpringUtilities;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.SoundSystem;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloSettingsDialog;

import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;

/**
 * Control panel for a Makelangelo robot
 *
 * @author Dan Royer
 * @author Peter Colapietro
 * @since 7.1.4
 */
public class MakelangeloRobotPanel extends JPanel implements ActionListener, ItemListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -4703402918904039337L;

	// god objects ?
	protected MakelangeloRobot robot;
	protected Makelangelo makelangeloApp;

	// connect menu
	private CollapsiblePanel connectionPanel;
	private JButton buttonConnect;
	
	// machine options
	protected int generatorChoice = 0;
	
	private String[] machineConfigurations;
	private JComboBox<String> machineChoices;
	private JButton buttonOpenSettings;
	private JPanel machineNumberPanel;
	
	private JButton buttonOpenFile, buttonReopenFile, buttonNewFile, buttonGenerate, buttonSaveFile;

	private PiCaptureAction piCameraCaptureAction;
	private JButton buttonCapture;

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
		this.makelangeloApp = gui;
		this.robot = robot;
		
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

		add(createConnectPanel(), con1);		con1.gridy++;
		
		// settings
		machineNumberPanel = new JPanel(new GridBagLayout());
		updateMachineNumberPanel();
		add(machineNumberPanel, con1);
		con1.gridy++;

		try {
			piCameraCaptureAction = new PiCaptureAction(gui, Translator.get("MenuCaptureImage"));	
		} catch (FailedToRunRaspistillException e) {
			Log.message("Cannot run raspistill");
		}

		add(createAxisDrivingControls(),con1);	con1.gridy++;
		add(createCommonDriveControls(),con1);	con1.gridy++;
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
		NetworkConnection s = makelangeloApp.requestNewConnection();
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
		myArtPipelinePanel = new ArtPipelinePanel(makelangeloApp.getMainFrame());
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

		if (piCameraCaptureAction != null) {
            buttonCapture = new JButton(piCameraCaptureAction);
            panel.add(buttonCapture, con1);
            con1.gridy++;
        } else {
        	buttonCapture = null;
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

		goPaperBorder = new JButton(new PaperBorderAction(robot,Translator.get("GoPaperBorder")));
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
		else if (subject == buttonOpenFile) makelangeloApp.openFile();
		else if (subject == buttonReopenFile) makelangeloApp.reopenLastFile();
		else if (subject == buttonGenerate) generateImage();
		else if (subject == buttonSaveFile) makelangeloApp.saveFile();
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
		if(p.run(makelangeloApp.getMainFrame())==false) return;

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
		if(buttonCapture != null) {
            buttonCapture.setEnabled(!isRunning);
        }
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

	public Makelangelo getGui() {
		return makelangeloApp;
	}
}
