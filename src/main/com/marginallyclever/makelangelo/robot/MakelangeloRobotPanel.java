package com.marginallyclever.makelangelo.robot;

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

import javax.swing.*;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.core.select.SelectButton;
import com.marginallyclever.core.select.SelectPanel;
import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.nodes.LoadAndSaveFile;
import com.marginallyclever.makelangelo.robot.settings.MakelangeloSettingsDialog;

/**
 * Control panel for a Makelangelo robot
 * @author Dan Royer
 * @author Peter Colapietro
 * @since 7.1.4
 */
public class MakelangeloRobotPanel extends JPanel implements ActionListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -4703402918904039337L;

	// the robot being controlled
	private MakelangeloRobot robot;
	
	// the parent
	private Makelangelo makelangeloApp;

	// connect menu
	private SelectPanel connectionPanel;
	private SelectButton buttonConnect;
	
	// machine options
	private String[] machineConfigurations;
	private JComboBox<String> machineChoices;
	private JButton buttonOpenSettings;
	private JPanel machineNumberPanel;

    // live controls
    protected SelectButton buttonStart, buttonStartAt, buttonPause, buttonHalt;

	// driving controls
	private JButton down100,down10,down1,up1,up10,up100;
	private JButton left100,left10,left1,right1,right10,right100;
	private JButton setHome;
	private SelectButton goHome,findHome;
	private SelectButton goPaperBorder,penUp,penDown;
	private SelectButton toggleEngagedMotor;

	private boolean isConnected;
	
	// progress bar, line count, time estimate 
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

		add(createAxisDrivingControls(),con1);		con1.gridy++;
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
		
		goPaperBorder = new SelectButton(new PaperBorderAction(robot,Translator.get("GoPaperBorder")));
		animationInterior.add(goPaperBorder);
		
		penUp    = new SelectButton(Translator.get("PenUp"));
		animationInterior.add(penUp);
		penUp.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.raisePen();
			}
		});
		
		penDown  = new SelectButton(Translator.get("PenDown"));
		animationInterior.add(penDown);
		penDown.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.lowerPen();
			}
		});
		
		
		goHome   = new SelectButton(Translator.get("GoHome"));
		animationInterior.add(goHome);
		goHome.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.goHome();	
			}
		});
		
		findHome = new SelectButton(Translator.get("FindHome"));
		animationInterior.add(findHome);
		findHome.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.findHome();
			}
		});
		
		toggleEngagedMotor = new SelectButton(Translator.get("DisengageMotors"));
		animationInterior.add(toggleEngagedMotor);
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
		
		buttonStart = new SelectButton(Translator.get("Start"));
		animationInterior.add(buttonStart);
		buttonStart.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.startAt(0);
			}
		});
		
		buttonStartAt = new SelectButton(Translator.get("StartAtLine"));
		animationInterior.add(buttonStartAt);
		buttonStartAt.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				startAt();
			}
		});
		
		buttonPause = new SelectButton(Translator.get("Pause"));
		animationInterior.add(buttonPause);
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
		
		buttonHalt = new SelectButton(Translator.get("Halt"));
		animationInterior.add(buttonHalt);
		buttonHalt.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				robot.halt();	
			}
		});

		
		return animationPanel;
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
		
		return drivePanel;
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
				MakelangeloSettingsDialog m = new MakelangeloSettingsDialog(robot);
				m.run(makelangeloApp.getMainFrame());
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
		if(p.run(makelangeloApp.getMainFrame())) {
			// user hit ok
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
		
		buttonOpenSettings.setEnabled(!isRunning);

		buttonStart.setEnabled(isConfirmed && didSetHome && !isRunning);
		buttonStartAt.setEnabled(isConfirmed && didSetHome && !isRunning);
		buttonPause.setEnabled(isConfirmed && isRunning);
		buttonHalt.setEnabled(isConfirmed && isRunning);

		if (!isConfirmed) {
			buttonPause.setText(Translator.get("Pause"));
		}

		toggleEngagedMotor.setEnabled(isConfirmed && !isRunning);

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
		
		this.validate();
	}
}
