package com.marginallyclever.makelangelo;

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
import com.marginallyclever.core.CommandLineOptions;
import com.marginallyclever.core.Translator;
import com.marginallyclever.makelangelo.DrivePlotterGUI;
import com.marginallyclever.makelangelo.nodes.LoadFile;
import com.marginallyclever.makelangelo.plotter.Makelangelo5;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterListener;

/**
 * Control panel for connecting to and controlling a {@Plotter}.
 * @author Dan Royer
 * @author Peter Colapietro
 * @since 7.1.4
 */
public class DrivePlotterGUI implements ActionListener, PlotterListener {
	// the robot being controlled
	private Plotter myPlotter;
	
	// the top-most UX element
	private Frame parentFrame;
	private JPanel myPanel;

	// connect menu
	private SelectPanel connectionPanel;
	private SelectButton buttonConnect;
	
	// jog buttons
	private JButton buttonLeftIn;
	private JButton buttonLeftOut;
	private JButton buttonRightIn;
	private JButton buttonRightOut;

	// driving controls
	private JButton down100,down10,down1,up1,up10,up100;
	private JButton left100,left10,left1,right1,right10,right100;
	private JButton setHome,goHome,findHome;
	private JButton penUp,penDown;
	private JButton toggleEngageMotor;
	private JButton toggleDisengageMotor;

	// whole-drawing controls
    private JButton buttonStart, buttonStartAt, buttonPause, buttonHalt;

	private boolean isConnected;
	
	// progress bar, line count, time estimate 
	public StatusBar statusBar;

	/**
	 * @param plotter
	 */
	public DrivePlotterGUI(Plotter plotter) {
		myPlotter = plotter;
	}

	public void run(Frame parent) {
		parentFrame = parent;
		
		JDialog dialog = new JDialog(parent,Translator.get("Makelangelo.menuRobot"), true);
        dialog.setLocation(parent.getLocation());
	
		myPlotter.addListener(this);
		
		myPanel = new JPanel(new GridBagLayout());

		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx = 0;
		con1.gridy = 0;
		con1.weightx = 1;
		con1.weighty = 0;
		con1.fill = GridBagConstraints.HORIZONTAL;
		con1.anchor = GridBagConstraints.NORTHWEST;

		myPanel.add(createConnectSubPanel(), con1);
		con1.gridy++;
		
		myPanel.add(createJogMotorsPanel(myPlotter),con1);	con1.gridy++;
		myPanel.add(createUtilitiesPanel(myPlotter),con1);	con1.gridy++;
		myPanel.add(createAxisDrivingControls(),con1);		con1.gridy++;
		myPanel.add(createDrawImagePanel(),con1);			con1.gridy++;

		// always have one extra empty at the end to push everything up.
		con1.weighty = 1;
		myPanel.add(new JLabel(), con1);
		
		// lastly, set the button states
		updateButtonAccess();

		myPlotter.addListener(this);
		
		dialog.setContentPane(myPanel);
		dialog.pack();
		dialog.setVisible(true);
		
		// wait while user does stuff...
		
		myPlotter.closeConnection();
		myPlotter.removeListener(this);
	}


	protected List<LoadFile> loadFileSavers() {
		return new ArrayList<LoadFile>();
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


	private JPanel createJogMotorsPanel(final Plotter plotter) {
		CollapsiblePanel jogPanel = new CollapsiblePanel(Translator.get("RobotPanel.JogMotors"));
		JPanel jogInterior = jogPanel.getContentPane().getInteriorPanel();
		jogInterior.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.NORTH;
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.weightx=1;
			
		jogInterior.add(buttonLeftIn = new JButton(Translator.get("JogLeftIn")),gbc);
		buttonLeftIn.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				plotter.jogLeftMotorIn();
			}
		});

		gbc.gridx=1;
		jogInterior.add(new JLabel(""),gbc);
		
		gbc.gridx=2;
		jogInterior.add(buttonRightIn = new JButton(Translator.get("JogRightIn")),gbc);
		buttonRightIn.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				plotter.jogRightMotorIn();
			}
		});
		
		gbc.gridy++;
		gbc.gridx=0;
		jogInterior.add(buttonLeftOut = new JButton(Translator.get("JogLeftOut")),gbc);
		buttonLeftOut.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				plotter.jogLeftMotorOut();
			}
		});
		
		gbc.gridx=1;
		jogInterior.add(new JLabel(""),gbc);
		
		gbc.gridx=2;
		jogInterior.add(buttonRightOut = new JButton(Translator.get("JogRightOut")),gbc);
		buttonRightOut.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				plotter.jogRightMotorOut();
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
					myPlotter.halt();
					myPlotter.closeConnection();
					buttonConnect.setText(Translator.get("ButtonConnect"));
					buttonConnect.setForeground(Color.GREEN);
					isConnected=false;
					updateButtonAccess();
				} else {
					// network connections
					ConnectionManager connectionManager = new ConnectionManager();
					NetworkConnection s = connectionManager.requestNewConnection(parentFrame);
					if(s!=null) {
						Log.message("Connected.");
						buttonConnect.setText(Translator.get("ButtonDisconnect"));
						buttonConnect.setForeground(Color.RED);
						myPlotter.openConnection( s );
					}
					isConnected=true;
				}
			}
		});
        buttonConnect.setForeground(Color.GREEN);

        connectionPanel.add(buttonConnect);

	    return connectionPanel;
	}


	private JPanel createUtilitiesPanel(final Plotter plotter) {
		CollapsiblePanel utilitiesPanel = new CollapsiblePanel(Translator.get("RobotPanel.Animate"));
		JPanel panelInterior = utilitiesPanel.getContentPane().getInteriorPanel();

		panelInterior.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.NORTH;
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.weightx=1;
		gbc.gridwidth=1;
		
		toggleEngageMotor = new JButton(Translator.get("EngageMotors"));
		panelInterior.add(toggleEngageMotor,gbc);
		toggleEngageMotor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plotter.engageMotors();
			}
		});

		toggleDisengageMotor = new JButton(Translator.get("DisengageMotors"));
		panelInterior.add(toggleDisengageMotor,gbc);
		toggleDisengageMotor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plotter.disengageMotors();
			}
		});
		
		
		gbc.gridy++;
		panelInterior.add(new JSeparator(),gbc);
		
		gbc.gridy++;
		penUp = new JButton(Translator.get("PenUp"));
		panelInterior.add(penUp,gbc);
		penUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plotter.raisePen();
			}
		});
		
		gbc.gridy++;
		penDown = new JButton(Translator.get("PenDown"));
		panelInterior.add(penDown,gbc);
		penDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myPlotter.lowerPen();
			}
		});
		
		gbc.gridy++;
		panelInterior.add(new JSeparator(),gbc);

		gbc.gridy++;
		setHome = new JButton(Translator.get("SetHome"));
	    panelInterior.add(setHome,gbc);
	    setHome.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myPlotter.setHome();
				updateButtonAccess();
			}
		});

		gbc.gridy++;
		findHome = new JButton(Translator.get("FindHome"));
		panelInterior.add(findHome,gbc);
		findHome.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myPlotter.findHome();
			}
		});

		gbc.gridy++;
		goHome = new JButton(Translator.get("GoHome"));
		panelInterior.add(goHome,gbc);
		goHome.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myPlotter.goHome();	
			}
		});
		
		return utilitiesPanel;
	}
	
	private JPanel createDrawImagePanel() {
		CollapsiblePanel drawImagePanel = new CollapsiblePanel(Translator.get("RobotPanel.drawImagePanel"));
		JPanel panelInterior = drawImagePanel.getContentPane().getInteriorPanel();

		panelInterior.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.NORTH;
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.weightx=1;
		gbc.gridwidth=1;
		
		gbc.gridy++;
		buttonStart = new JButton(Translator.get("Start"));
		panelInterior.add(buttonStart,gbc);
		buttonStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				robotController.startAt(0);
			}
		});

		gbc.gridy++;
		buttonStartAt = new JButton(Translator.get("StartAtLine"));
		panelInterior.add(buttonStartAt,gbc);
		buttonStartAt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startAt();
			}
		});

		gbc.gridy++;
		buttonPause = new JButton(Translator.get("Pause"));
		panelInterior.add(buttonPause,gbc);
		buttonPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// toggle pause
				if (myPlotter.isPaused() == true) {
					buttonPause.setText(Translator.get("Pause"));
					robotController.unPause();
					robotController.sendFileCommand();
				} else {
					buttonPause.setText(Translator.get("Unpause"));
					robotController.pause();
				}
			}
		});

		
		gbc.gridy++;
		buttonHalt = new JButton(Translator.get("Halt"));
		panelInterior.add(buttonHalt,gbc);
		buttonHalt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myPlotter.halt();	
			}
		});

		gbc.gridy++;
		statusBar = new StatusBar();
		panelInterior.add(statusBar, gbc);
		
		return drawImagePanel;
	}
	
	// manual cartesian driving
	private CollapsiblePanel createAxisDrivingControls() {
		CollapsiblePanel drivePanel = new CollapsiblePanel(Translator.get("RobotPanel.AxisDriveControls"));
		JPanel panelInterior = drivePanel.getContentPane().getInteriorPanel();
		panelInterior.setLayout(new GridBagLayout());
		
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

		gbc.gridx=3;  gbc.gridy=6;  panelInterior.add(down100,gbc);
		gbc.gridx=3;  gbc.gridy=5;  panelInterior.add(down10,gbc);
		gbc.gridx=3;  gbc.gridy=4;  panelInterior.add(down1,gbc);

		gbc.gridx=0;  gbc.gridy=3;  panelInterior.add(left100,gbc);
		gbc.gridx=1;  gbc.gridy=3;  panelInterior.add(left10,gbc);
		gbc.gridx=2;  gbc.gridy=3;  panelInterior.add(left1,gbc);
		
		gbc.gridx=4;  gbc.gridy=3;  panelInterior.add(right1,gbc);
		gbc.gridx=5;  gbc.gridy=3;  panelInterior.add(right10,gbc);
		gbc.gridx=6;  gbc.gridy=3;  panelInterior.add(right100,gbc);
		
		gbc.gridx=3;  gbc.gridy=2;  panelInterior.add(up1,gbc);
		gbc.gridx=3;  gbc.gridy=1;  panelInterior.add(up10,gbc);
		gbc.gridx=3;  gbc.gridy=0;  panelInterior.add(up100,gbc);

		return drivePanel;
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

		if(dx!=0 || dy!=0) {
			myPlotter.movePenRelative(dx,dy);
		}
	}
	
	protected void startAt() {
		PanelStartAt p = new PanelStartAt();
		if(p.run(parentFrame)) {
			// user hit ok
			int lineNumber = p.lineNumber;
			if (lineNumber != -1) {
				if(p.findPreviousPenDown==false) {
					if(p.addPenDownCommand==true) {
						myPlotter.sendLineToRobot(myPlotter.getPenDownString());
					}
					robotController.startAt(lineNumber);
				} else {
					int lineBefore = robotController.findLastPenUpBefore(lineNumber);
					robotController.startAt(lineBefore);
				}
			}
		}
	}
	

	// the moment a robot is confirmed to have connected
	public void onConnect() {
		updateButtonAccess();
		myPlotter.engageMotors();
	}
	
	public void updateButtonAccess() {
		boolean isConfirmed=false;
		boolean isRunning=false;
		boolean didSetHome=false;
				
		if(myPlotter!=null) {
			isConfirmed = myPlotter.isPortConfirmed();
			isRunning = myPlotter.isRunning();
			didSetHome = myPlotter.didSetHome();
		}
		
		buttonRightIn.setEnabled(isConfirmed && !isRunning);
		buttonLeftIn.setEnabled(isConfirmed && !isRunning);
		buttonRightOut.setEnabled(isConfirmed && !isRunning);
		buttonLeftOut.setEnabled(isConfirmed && !isRunning);
		
		if(buttonHalt!=null) buttonHalt.setEnabled(isConfirmed && isRunning);
		if(buttonStart!=null) buttonStart.setEnabled(isConfirmed && didSetHome && !isRunning);
		if(buttonStartAt!=null) buttonStartAt.setEnabled(isConfirmed && didSetHome && !isRunning);
		if(buttonPause!=null) {
			buttonPause.setEnabled(isConfirmed && isRunning);
			if(!isConfirmed) {
				buttonPause.setText(Translator.get("Pause"));
			}
		}
		
		toggleEngageMotor.setEnabled(isConfirmed && !isRunning);
		toggleDisengageMotor.setEnabled(isConfirmed && !isRunning);

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

		setHome .setEnabled( isConfirmed && !isRunning && !myPlotter.canAutoHome() );
		findHome.setEnabled( isConfirmed && !isRunning &&  myPlotter.canAutoHome() );
		goHome.setEnabled(isConfirmed && !isRunning && didSetHome);
		
		penUp.setEnabled(isConfirmed && !isRunning);
		penDown.setEnabled(isConfirmed && !isRunning);
		
		myPanel.validate();
	}
	
	
	public static void main(String[] argv) {
		if(GraphicsEnvironment.isHeadless()) {
			System.out.println("Test can only be run on a machine with a head (monitor, HID)");
			return;
		}	
		
		Log.start();
		CommandLineOptions.setFromMain(argv);
		Translator.start();
		
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame mainFrame = new JFrame(Translator.get("Makelangelo.menuRobot"));
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				Plotter robot = new Makelangelo5();
				DrivePlotterGUI gui = new DrivePlotterGUI(robot);
				gui.run(mainFrame);
			}
		});
	}

	@Override
	public void connectionConfirmed(Plotter r) {		
		onConnect();
		
		if (parentFrame != null) {
			parentFrame.invalidate();
		}
		updateButtonAccess();
	}

	@Override
	public void firmwareVersionBad(Plotter r, long versionFound) {
		(new DialogBadFirmwareVersion()).display(parentFrame, Long.toString(versionFound));
	}

	@Override
	public void dataAvailable(Plotter r, String data) {
		if (data.endsWith("\n"))
			data = data.substring(0, data.length() - 1);
		Log.message(data); // #ffa500 = orange
	}

	@Override
	public void sendBufferEmpty(Plotter r) {}

	@Override
	public void lineError(Plotter r, int lineNumber) {}

	@Override
	public void disconnected(Plotter r) {
		if (parentFrame != null) {
			parentFrame.invalidate();
		}
		SoundSystem.playDisconnectSound();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getSource() == myPlotter) {
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
					toggleEngageMotor.setText(Translator.get("DisengageMotors"));
				} else {
					toggleEngageMotor.setText(Translator.get("EngageMotors"));
				}
				break;
			}
		}
	}
}
