package com.marginallyclever.makelangelo.robot;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.*;

import com.marginallyclever.artPipeline.ArtPipelinePanel;
import com.marginallyclever.artPipeline.nodes.LoadAndSaveFile;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.nodes.Node;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.SoundSystem;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.robot.settings.MakelangeloSettingsDialog;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;

/**
 * Control panel for a Makelangelo robot
 *
 * @author Dan Royer
 * @author Peter Colapietro
 * @since 7.1.4
 */
public class MakelangeloRobotPanel extends JPanel implements ActionListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -4703402918904039337L;

	// god objects?
	protected MakelangeloRobot robot;
	protected Makelangelo makelangeloApp;

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
    protected SelectButton buttonStart, buttonStartAt, buttonPause, buttonHalt;

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


	protected List<LoadAndSaveFile> loadFileSavers() {
		return new ArrayList<LoadAndSaveFile>();
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


	protected JPanel createConnectSubPanel() {
		connectionPanel = new SelectPanel();
				
        buttonConnect = new SelectButton(Translator.get("ButtonConnect"));
        buttonConnect.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(isConnected) {
					robot.halt();
					robot.closeConnection();
					buttonConnect.setText(Translator.get("ButtonConnect"));
					buttonConnect.setForeground(Color.GREEN);
					isConnected=false;
					updateButtonAccess();
				} else {
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
			}
		});
        buttonConnect.setForeground(Color.GREEN);

        connectionPanel.add(buttonConnect);

	    return connectionPanel;
	}

	
	protected void updateMachineChoice() {
		int selectedIndex = machineChoices.getSelectedIndex();
		long newUID = Long.parseLong(machineChoices.getItemAt(selectedIndex));
		robot.getSettings().loadConfig(newUID);
	}


	private JPanel createAnimationPanel() {
		CollapsiblePanel animationPanel = new CollapsiblePanel(Translator.get("MenuAnimate"));
		SelectPanel animationInterior = animationPanel.getContentPane();
		
		buttonStart = new SelectButton(Translator.get("Start"));
		buttonStartAt = new SelectButton(Translator.get("StartAtLine"));
		buttonPause = new SelectButton(Translator.get("Pause"));
		buttonHalt = new SelectButton(Translator.get("Halt"));
		buttonStart.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.startAt(0);
			}
		});
		buttonStartAt.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				startAt();
			}
		});
		buttonPause.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// toggle pause
				if (robot.isPaused() == true) {
					buttonPause.setText(Translator.get("Pause"));
					robot.unPause();
					robot.sendFileCommand();
				} else {
					buttonPause.setText(Translator.get("Unpause"));
					robot.pause();
				}
			}
		});
		buttonHalt.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.halt();	
			}
		});

		animationInterior.add(buttonStart);
		animationInterior.add(buttonStartAt);
		animationInterior.add(buttonPause);
		animationInterior.add(buttonHalt);
		
		return animationPanel;
	}
	

	private CollapsiblePanel createArtPipelinePanel() {
		myArtPipelinePanel = new ArtPipelinePanel(makelangeloApp.getMainFrame());
		myArtPipelinePanel.setPipeline(robot.getPipeline());
		
		return myArtPipelinePanel;
	}
	
	private JPanel createCreativeControlPanel() {
		CollapsiblePanel creativeControlPanel = new CollapsiblePanel(Translator.get("MenuCreativeControl"));
		SelectPanel panel = creativeControlPanel.getContentPane();


		if (piCameraCaptureAction != null) {
            buttonCapture = new SelectButton(piCameraCaptureAction);
            panel.add(buttonCapture);
        } else {
        	buttonCapture = null;
        }
		

		buttonNewFile = new SelectButton(Translator.get("MenuNewFile"));
		panel.add(buttonNewFile);
		buttonNewFile.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				newFile();
			}
		});
		
		buttonOpenFile = new SelectButton(Translator.get("MenuOpenFile"));
		panel.add(buttonOpenFile);
		buttonOpenFile.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				makelangeloApp.openFile();
			}
		});
		
		buttonReopenFile = new SelectButton(Translator.get("MenuReopenFile"));
		panel.add(buttonReopenFile);
		buttonReopenFile.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				makelangeloApp.reopenLastFile();
			}
		});

		buttonGenerate = new SelectButton(Translator.get("MenuGenerate"));
		panel.add(buttonGenerate);
		buttonGenerate.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				generateImage();
			}
		});

		buttonSaveFile = new SelectButton(Translator.get("MenuSaveGCODEAs"));
		panel.add(buttonSaveFile);
		buttonSaveFile.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				makelangeloApp.saveFile();	
			}
		});
		
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
		CollapsiblePanel commonControlsPanel = new CollapsiblePanel(Translator.get("MenuCommonDriveControls"));
		SelectPanel commonInterior = commonControlsPanel.getContentPane();

		goPaperBorder = new SelectButton(new PaperBorderAction(robot,Translator.get("GoPaperBorder")));
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
		
		toggleEngagedMotor.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(robot.areMotorsEngaged() ) {
					robot.disengageMotors();
				} else {
					robot.engageMotors();
				}
			}
		});
		penUp.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.raisePen();
			}
		});
		penDown.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.lowerPen();
			}
		});
		
		goHome.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.goHome();	
			}
		});
		findHome.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.findHome();
			}
		});

		commonControlsPanel.setCollapsed(true);
		
		return commonControlsPanel;
	}

	
	/**
	 * Refresh the list of available known machines. 
	 * If we are connected to a machine, select that machine number and disable the ability to change selection.
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
			machineChoices.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED) {
						updateMachineChoice();
					}
				}
			});

			int index = robot.getSettings().getKnownMachineIndex();
			if( index<0 ) index=0;
			machineChoices.setSelectedIndex(index);

			// force the GUI to load the correct initial choice.
			updateMachineChoice();
		}

		buttonOpenSettings = new JButton(Translator.get("configureMachine"));
		buttonOpenSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Frame frame = (Frame)getRootPane().getParent();
				MakelangeloSettingsDialog m = new MakelangeloSettingsDialog(frame, robot);
				m.run();
				// we can only get here if the robot is connected and not running.
				// Save the gcode so that updates to settings are applied immediately + automatically.
				robot.saveCurrentTurtleToDrawing();
			}
		});
		buttonOpenSettings.setPreferredSize(buttonOpenSettings.getPreferredSize());
		machineNumberPanel.add(buttonOpenSettings,cMachine);
		cMachine.gridx++;
	}
	
	// The user has done something. respond to it.
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if (subject == setHome ) {
			robot.setHome();
			updateButtonAccess();
		}
		else {
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
	

	// the moment a robot is confirmed to have connected
	public void onConnect() {
		updateMachineNumberPanel();
		updateButtonAccess();
		robot.engageMotors();
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

		buttonSaveFile.setEnabled(robot!=null);
		
		this.validate();
	}
	

	private void newFile() {
		Turtle t = makelangeloApp.getSelectedTurtle();
		t.reset();
		robot.setTurtles( new ArrayList<Turtle>() );
		updateButtonAccess();
	}
	
	private void generateImage() {
		// set up a card layout (https://docs.oracle.com/javase/tutorial/uiswing/layout/card.html)
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel cards = new JPanel(new CardLayout());
		ServiceLoader<Node> imageGenerators = ServiceLoader.load(Node.class);
		int i=0;
		for( Node ici : imageGenerators ) {
			cards.add(ici.getPanel().getInteriorPanel(),ici.getName());
			i++;
		}
		
		String[] imageGeneratorNames = new String[i];
		
		i=0;
		for( Node f : imageGenerators ) {
			imageGeneratorNames[i++] = f.getName();
		}

		final JComboBox<String> options = new JComboBox<String>(imageGeneratorNames);
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
		options.setSelectedIndex(generatorChoice);
		
		
		JDialog dialog = new JDialog(makelangeloApp.getMainFrame(),Translator.get("MenuGenerate"));
		dialog.add(panel);
		dialog.pack();
		dialog.setVisible(true);
		// other app buttons are still accessible.
	}

	private void changeGeneratorPanel(int index) {
		NodePanel.makelangeloRobotPanel = this;
		
		Turtle t = makelangeloApp.getSelectedTurtle();
		
		Node chosenGenerator = getGenerator(index);
		NodePanel chosenGeneratorPanel = chosenGenerator.getPanel();
		if(chosenGeneratorPanel!=null) {
			Log.message("Generator="+chosenGenerator.getName());
			JPanel p = chosenGeneratorPanel.getInteriorPanel();
			p.setBorder(BorderFactory.createLineBorder(Color.RED));
			try {
				regenerate(chosenGenerator,t);
			} catch(Exception e){}
		}
	}
	
	
	/**
	 * restart the Generator
	 * @param chosenGenerator
	 * @param t
	 */
	public void regenerate(Node chosenGenerator,Turtle t) {
		robot.setDecorator(chosenGenerator);
		
		// do the work
		t.reset();
		chosenGenerator.iterate();
		
		robot.setDecorator(null);
		
		Log.message(Translator.get("Finished"));
		SoundSystem.playConversionFinishedSound();
		updateButtonAccess();
	}

	private Node getGenerator(int arg0) throws IndexOutOfBoundsException {
		ServiceLoader<Node> imageGenerators = ServiceLoader.load(Node.class);
		int i=0;
		for( Node chosenGenerator : imageGenerators ) {
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
