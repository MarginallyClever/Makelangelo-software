package com.marginallyclever.makelangelo.robot.ux;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
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

import com.marginallyclever.communications.ConnectionManager;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.select.SelectButton;
import com.marginallyclever.core.select.SelectPanel;
import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.DialogBadFirmwareVersion;
import com.marginallyclever.makelangelo.SoundSystem;
import com.marginallyclever.core.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.nodes.LoadAndSaveFile;
import com.marginallyclever.makelangelo.robot.RobotController;
import com.marginallyclever.makelangelo.robot.RobotControllerListener;
import com.marginallyclever.makelangelo.robot.ux.PanelRobot;

/**
 * Control panel for a Makelangelo robot
 * @author Dan Royer
 * @author Peter Colapietro
 * @since 7.1.4
 */
public class PanelRobot extends JPanel implements ActionListener, RobotControllerListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -4703402918904039337L;

	// the robot being controlled
	private RobotController myRobotController;
	private ConnectionManager connectionManager;
	
	// the top-most UX element
	private Frame parentFrame;

	// connect menu
	private SelectPanel connectionPanel;
	private SelectButton buttonConnect;
	
	// machine options
	private String[] machineConfigurations;
	private JComboBox<String> machineChoices;
	private JButton buttonOpenSettings;
	private JPanel machineNumberPanel;

	// jog buttons
	private JButton buttonAneg;
	private JButton buttonApos;
	private JButton buttonBneg;
	private JButton buttonBpos;

	// driving controls
	private JButton down100,down10,down1,up1,up10,up100;
	private JButton left100,left10,left1,right1,right10,right100;
	private JButton setHome,goHome,findHome;
	private JButton goPaperBorder;
	private JButton penUp,penDown;
	private JButton toggleEngagedMotor;

	// whole-drawing controls
    private JButton buttonStart, buttonStartAt, buttonPause, buttonHalt;

	private boolean isConnected;
	
	// progress bar, line count, time estimate 
	public StatusBar statusBar;

	/**
	 * @param parent
	 * @param robot
	 */
	public PanelRobot(Frame parent, RobotController robot) {
		this.parentFrame = parent;
		this.myRobotController = robot;
		myRobotController.addListener(this);
		
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

		add(createJogMotorsPanel(),con1);			con1.gridy++;
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
		
		connectionManager = new ConnectionManager();
		robot.addListener(this);
	}


	protected List<LoadAndSaveFile> loadFileSavers() {
		return new ArrayList<LoadAndSaveFile>();
	}


	public JButton createTightJButton(String label) {
		JButton b = new JButton(label);
		//b.setMargin(new Insets(0,0,0,0));
		Dimension d = new Dimension(60,20);
		b.setPreferredSize(d);
		b.setMaximumSize(d);
		b.setMinimumSize(d);
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


	private JPanel createJogMotorsPanel() {
		CollapsiblePanel jogPanel = new CollapsiblePanel(Translator.get("RobotPanel.JogMotors"));
		JPanel jogInterior = jogPanel.getContentPane().getInteriorPanel();
		jogInterior.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.NORTH;
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.weightx=1;
			
		jogInterior.add(buttonAneg = new JButton(Translator.get("JogLeftIn")),gbc);
		buttonAneg.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				myRobotController.jogLeftMotorIn();
			}
		});

		gbc.gridx=1;
		jogInterior.add(new JLabel(""),gbc);
		
		gbc.gridx=2;
		jogInterior.add(buttonBneg = new JButton(Translator.get("JogRightIn")),gbc);
		buttonBneg.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				myRobotController.jogRightMotorIn();
			}
		});
		
		gbc.gridy++;
		gbc.gridx=0;
		jogInterior.add(buttonApos = new JButton(Translator.get("JogLeftOut")),gbc);
		buttonApos.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				myRobotController.jogLeftMotorOut();
			}
		});
		
		gbc.gridx=1;
		jogInterior.add(new JLabel(""),gbc);
		
		gbc.gridx=2;
		jogInterior.add(buttonBpos = new JButton(Translator.get("JogRightOut")),gbc);
		buttonBpos.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				myRobotController.jogRightMotorOut();
			}
		});
		
		return jogPanel;
	}
	
	protected JPanel createConnectSubPanel() {
		connectionPanel = new SelectPanel();
				
        buttonConnect = new SelectButton(Translator.get("ButtonConnect"));
        buttonConnect.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(isConnected) {
					myRobotController.halt();
					myRobotController.closeConnection();
					buttonConnect.setText(Translator.get("ButtonConnect"));
					buttonConnect.setForeground(Color.GREEN);
					isConnected=false;
					updateButtonAccess();
				} else {
					// network connections
					NetworkConnection s = connectionManager.requestNewConnection(parentFrame);
					if(s!=null) {
						Log.message("Connected.");
						buttonConnect.setText(Translator.get("ButtonDisconnect"));
						buttonConnect.setForeground(Color.RED);
						myRobotController.openConnection( s );
					}
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
		myRobotController.getSettings().loadConfig(newUID);
	}


	private JPanel createAnimationPanel() {
		CollapsiblePanel animationPanel = new CollapsiblePanel(Translator.get("RobotPanel.Animate"));
		JPanel animationInterior = animationPanel.getContentPane().getInteriorPanel();

		animationInterior.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.NORTH;
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.weightx=1;
		gbc.gridwidth=1;
		
		
		goPaperBorder = new JButton(new PaperBorderAction(myRobotController,Translator.get("GoPaperBorder")));
		animationInterior.add(goPaperBorder,gbc);
		
		gbc.gridy++;
		animationInterior.add(new JSeparator(),gbc);

		gbc.gridy++;
		toggleEngagedMotor = new JButton(Translator.get("DisengageMotors"));
		animationInterior.add(toggleEngagedMotor,gbc);
		toggleEngagedMotor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(myRobotController.areMotorsEngaged() ) {
					myRobotController.disengageMotors();
				} else {
					myRobotController.engageMotors();
				}
			}
		});
		
		gbc.gridy++;
		animationInterior.add(new JSeparator(),gbc);
		
		gbc.gridy++;
		penUp = new JButton(Translator.get("PenUp"));
		animationInterior.add(penUp,gbc);
		penUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myRobotController.raisePen();
			}
		});
		
		gbc.gridy++;
		penDown = new JButton(Translator.get("PenDown"));
		animationInterior.add(penDown,gbc);
		penDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myRobotController.lowerPen();
			}
		});
		
		gbc.gridy++;
		animationInterior.add(new JSeparator(),gbc);

		gbc.gridy++;
		setHome = new JButton(Translator.get("SetHome"));
	    animationInterior.add(setHome,gbc);
	    setHome.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myRobotController.setHome();
				updateButtonAccess();
			}
		});

		gbc.gridy++;
		findHome = new JButton(Translator.get("FindHome"));
		animationInterior.add(findHome,gbc);
		findHome.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myRobotController.findHome();
			}
		});

		gbc.gridy++;
		goHome = new JButton(Translator.get("GoHome"));
		animationInterior.add(goHome,gbc);
		goHome.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myRobotController.goHome();	
			}
		});
		
		gbc.gridy++;
		animationInterior.add(new JSeparator(),gbc);

		gbc.gridy++;
		buttonStart = new JButton(Translator.get("Start"));
		animationInterior.add(buttonStart,gbc);
		buttonStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myRobotController.startAt(0);
			}
		});

		gbc.gridy++;
		buttonStartAt = new JButton(Translator.get("StartAtLine"));
		animationInterior.add(buttonStartAt,gbc);
		buttonStartAt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startAt();
			}
		});

		gbc.gridy++;
		buttonPause = new JButton(Translator.get("Pause"));
		animationInterior.add(buttonPause,gbc);
		buttonPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// toggle pause
				if (myRobotController.isPaused() == true) {
					buttonPause.setText(Translator.get("Pause"));
					myRobotController.unPause();
					myRobotController.sendFileCommand();
				} else {
					buttonPause.setText(Translator.get("Unpause"));
					myRobotController.pause();
				}
			}
		});

		
		gbc.gridy++;
		buttonHalt = new JButton(Translator.get("Halt"));
		animationInterior.add(buttonHalt,gbc);
		buttonHalt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myRobotController.halt();	
			}
		});

		return animationPanel;
	}
	

	// manual cartesian driving
	private CollapsiblePanel createAxisDrivingControls() {
		CollapsiblePanel drivePanel = new CollapsiblePanel(Translator.get("RobotPanel.AxisDriveControls"));
		JPanel driveInterior = drivePanel.getContentPane().getInteriorPanel();
		driveInterior.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill=GridBagConstraints.BOTH;
		gbc.anchor=GridBagConstraints.CENTER;
		gbc.gridx=0;
		gbc.gridy=0;

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

		gbc.gridx=3;  gbc.gridy=6;  driveInterior.add(down100,gbc);
		gbc.gridx=3;  gbc.gridy=5;  driveInterior.add(down10,gbc);
		gbc.gridx=3;  gbc.gridy=4;  driveInterior.add(down1,gbc);

		gbc.gridx=0;  gbc.gridy=3;  driveInterior.add(left100,gbc);
		gbc.gridx=1;  gbc.gridy=3;  driveInterior.add(left10,gbc);
		gbc.gridx=2;  gbc.gridy=3;  driveInterior.add(left1,gbc);
		
		gbc.gridx=4;  gbc.gridy=3;  driveInterior.add(right1,gbc);
		gbc.gridx=5;  gbc.gridy=3;  driveInterior.add(right10,gbc);
		gbc.gridx=6;  gbc.gridy=3;  driveInterior.add(right100,gbc);
		
		gbc.gridx=3;  gbc.gridy=2;  driveInterior.add(up1,gbc);
		gbc.gridx=3;  gbc.gridy=1;  driveInterior.add(up10,gbc);
		gbc.gridx=3;  gbc.gridy=0;  driveInterior.add(up100,gbc);

		return drivePanel;
	}
	

	/**
	 * Refresh the list of available known machines. 
	 * If we are connected to a machine, select that machine number and disable the ability to change selection.
	 */
	public void updateMachineNumberPanel() {
		machineNumberPanel.removeAll();
		machineConfigurations = myRobotController.getSettings().getKnownMachineNames();
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
			if( myRobotController.getConnection() == null ) state=true;
			else if( myRobotController.getConnection().isOpen() == false ) state=true;
			else if( myRobotController.isPortConfirmed() == false ) state=true;
			
			machineChoices.setEnabled( state );
			machineChoices.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED) {
						updateMachineChoice();
					}
				}
			});

			int index = myRobotController.getSettings().getKnownMachineIndex();
			if( index<0 ) index=0;
			machineChoices.setSelectedIndex(index);

			// force the GUI to load the correct initial choice.
			updateMachineChoice();
		}

		buttonOpenSettings = new JButton(Translator.get("configureMachine"));
		buttonOpenSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SettingsDialog m = new SettingsDialog(myRobotController);
				m.run(parentFrame);
			}
		});
		buttonOpenSettings.setPreferredSize(buttonOpenSettings.getPreferredSize());
		machineNumberPanel.add(buttonOpenSettings,cMachine);
		cMachine.gridx++;
	}
	
	// The user has done something. respond to it.
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
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

		if(dx!=0 || dy!=0) myRobotController.movePenRelative(dx,dy);
	}
	
	protected void startAt() {
		PanelStartAt p = new PanelStartAt();
		if(p.run(parentFrame)) {
			// user hit ok
			int lineNumber = p.lineNumber;
			if (lineNumber != -1) {
				if(p.findPreviousPenDown==false) {
					if(p.addPenDownCommand==true) {
						myRobotController.sendLineToRobot(myRobotController.getSettings().getPenDownString());
					}
					myRobotController.startAt(lineNumber);
				} else {
					int lineBefore = myRobotController.findLastPenUpBefore(lineNumber);
					myRobotController.startAt(lineBefore);
				}
			}
		}
	}
	

	// the moment a robot is confirmed to have connected
	public void onConnect() {
		updateMachineNumberPanel();
		updateButtonAccess();
		myRobotController.engageMotors();
	}
	
	public void updateButtonAccess() {
		boolean isConfirmed=false;
		boolean isRunning=false;
		boolean didSetHome=false;
				
		if(myRobotController!=null) {
			isConfirmed = myRobotController.isPortConfirmed();
			isRunning = myRobotController.isRunning();
			didSetHome = myRobotController.didSetHome();
		}
		
		buttonOpenSettings.setEnabled(!isRunning);
		
		buttonBneg.setEnabled(isConfirmed && !isRunning);
		buttonAneg.setEnabled(isConfirmed && !isRunning);
		buttonBpos.setEnabled(isConfirmed && !isRunning);
		buttonApos.setEnabled(isConfirmed && !isRunning);
		
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
		setHome .setEnabled( isConfirmed && !isRunning && !myRobotController.getSettings().getHardwareProperties().canAutoHome() );
		findHome.setEnabled( isConfirmed && !isRunning &&  myRobotController.getSettings().getHardwareProperties().canAutoHome() );
		goHome.setEnabled(isConfirmed && !isRunning && didSetHome);
		
		penUp.setEnabled(isConfirmed && !isRunning);
		penDown.setEnabled(isConfirmed && !isRunning);
		
		this.validate();
	}
	
	
	public static void main(String[] argv) {
		Log.start();
		CommandLineOptions.setFromMain(argv);
		Translator.start();
		
		if(GraphicsEnvironment.isHeadless()) {
			// TODO a text-only interface?
		} else {			
			// Schedule a job for the event-dispatching thread:
			// creating and showing this application's GUI.
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JFrame mainFrame = new JFrame(Translator.get("Makelangelo.menuRobot"));
					mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

					RobotController robot = new RobotController();
					PanelRobot makelangeloRobotPanel = new PanelRobot(mainFrame, robot);

					mainFrame.setContentPane(makelangeloRobotPanel);
					mainFrame.pack();
					mainFrame.setVisible(true);
				}
			});
		}
	}

	@Override
	public void connectionConfirmed(RobotController r) {
		String hardwareVersion = r.getSettings().getHardwareVersion();
		onConnect();
		r.getSettings().setHardwareVersion(hardwareVersion);
		
		if (parentFrame != null) {
			parentFrame.invalidate();
		}
		updateMachineNumberPanel();
		updateButtonAccess();
	}

	@Override
	public void firmwareVersionBad(RobotController r, long versionFound) {
		(new DialogBadFirmwareVersion()).display(parentFrame, Long.toString(versionFound));
	}

	@Override
	public void dataAvailable(RobotController r, String data) {
		if (data.endsWith("\n"))
			data = data.substring(0, data.length() - 1);
		Log.message(data); // #ffa500 = orange
	}

	@Override
	public void sendBufferEmpty(RobotController r) {}

	@Override
	public void lineError(RobotController r, int lineNumber) {}

	@Override
	public void disconnected(RobotController r) {
		if (parentFrame != null) {
			parentFrame.invalidate();
		}
		SoundSystem.playDisconnectSound();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getSource() == myRobotController) {
			switch(evt.getPropertyName()) {
			case "halt":  updateButtonAccess();  break;
			case "running":
				statusBar.start();
				updateButtonAccess(); // disables all the manual driving buttons
				break;
			case "progress":
				statusBar.setProgress((long)evt.getOldValue(), (long)evt.getNewValue());
				break;
			case "engaged":
				if((boolean)evt.getNewValue()) {
					toggleEngagedMotor.setText(Translator.get("DisengageMotors"));
				} else {
					toggleEngagedMotor.setText(Translator.get("EngageMotors"));
				}
				break;
			}
		}
	}
}
