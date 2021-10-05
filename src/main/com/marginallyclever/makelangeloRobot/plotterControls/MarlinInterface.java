package com.marginallyclever.makelangeloRobot.plotterControls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.DialogBadFirmwareVersion;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.Plotter;
import com.marginallyclever.makelangeloRobot.PlotterEvent;
import com.marginallyclever.makelangeloRobot.marlin.MarlinEvent;

public class MarlinInterface extends JPanel {
	private static final long serialVersionUID = 979851120390943303L;
	private Plotter myPlotter;
	
	private int lineNumber;

	private TextInterfaceToNetworkSession chatInterface = new TextInterfaceToNetworkSession();

	private JButton bESTOP = new JButton("EMERGENCY STOP");
	private JButton bGetPosition = new JButton("M114");
	private JButton bSetHome = new JButton("Set Home");
	private JButton bGoHome = new JButton("Go Home");

	public MarlinInterface(Plotter plotter) {
		super();

		myPlotter = plotter;

		this.setLayout(new BorderLayout());
		this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(chatInterface, BorderLayout.CENTER);

		plotter.addListener((e) -> onPlotterEvent(e));

		chatInterface.addActionListener((e) -> {
			switch (e.getID()) {
			case ChooseConnectionPanel.CONNECTION_OPENED:
				onConnect();
				break;
			case ChooseConnectionPanel.CONNECTION_CLOSED:
				updateButtonAccess();
				break;
			}
		});
		
		chatInterface.addNetworkSessionListener((e)->{
			if(e.flag == MarlinEvent.TRANSPORT_ERROR) {
				setLineNumber((int)e.data);
				sendFileCommand();
			}
			if(e.flag == MarlinEvent.SEND_BUFFER_EMPTY) {
				sendFileCommand();
			}
		});
	}

	private void whenBadFirmwareDetected(String versionFound) {
		(new DialogBadFirmwareVersion()).display(this, versionFound);
	}

	private void whenBadHardwareDetected(String versionFound) {
		JOptionPane.showMessageDialog(this, Translator.get("hardwareVersionBadMessage", new String[]{versionFound}));
	}
	
	private Object onPlotterEvent(PlotterEvent e) {
		switch(e.type) {
		case PlotterEvent.HOME_FOUND: sendFindHome();  break;
		case PlotterEvent.POSITION: sendGoto(); break;
		case PlotterEvent.PEN_UP: sendPenUp(); break;
		case PlotterEvent.MOTORS_ENGAGED: sendEngage(); break;
		}
		return null;
	}

	private void sendFindHome() {
		// TODO Auto-generated method stub
		
	}

	private void sendPenUp() {
		sendCommand( myPlotter.getPenIsUp() 
				? myPlotter.getSettings().getPenUpString()
				: myPlotter.getSettings().getPenDownString() );
	}

	private void sendEngage() {
		sendCommand( myPlotter.getAreMotorsEngaged() ? "M17" : "M18" );
	}

	private void onConnect() {
		setupListener();
		
		lineNumber=0;
		
		// you are at the position I say you are at.
		new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				updateButtonAccess();
				sendSetHome();
			}
		}, 1000 // 1s delay
		);
	}

	private void setupListener() {
		chatInterface.addNetworkSessionListener((evt) -> {
			if(evt.flag == NetworkSessionEvent.DATA_RECEIVED) {
				String message = ((String)evt.data).trim();
				if (message.startsWith("X:") && message.contains("Count")) {
					//System.out.println("FOUND " + message);
					processM114Reply(message);
				}
			}
		});
	}
	
	private void sendCommand(String str) {
		lineNumber++;
		str = "N"+lineNumber+" "+str;
		str += generateChecksum(str);
		
		chatInterface.sendCommand(str);
	}

	private String generateChecksum(String line) {
		byte checksum = 0;

		for (int i = 0; i < line.length(); ++i) {
			checksum ^= line.charAt(i);
		}

		return "*" + Integer.toString(checksum);
	}

	// format is normally X:0.00 Y:270.00 Z:0.00 Count X:0 Y:0 Z:0 U:0 V:0 W:0
	// trim everything after and including "Count", then read the state data.
	private void processM114Reply(String message) {
		try {
			message = message.substring(0, message.indexOf("Count"));
			String[] majorParts = message.split("\b");
			Point2D pos = myPlotter.getPos();
			
			for (String s : majorParts) {
				String[] minorParts = s.split(":");
				Double v = Double.valueOf(minorParts[1]);
				if(minorParts[0].equalsIgnoreCase("X")) pos.x=v;
				if(minorParts[0].equalsIgnoreCase("Y")) pos.y=v;
			}
			
			myPlotter.setPos(pos);
		} catch (NumberFormatException e) {
			Log.error("M114 error: "+e.getMessage());
		}
	}

	private void sendGoto() {
		sendCommand(
				(myPlotter.getPenIsUp() ? "G0" : "G1")
				+ getPosition(myPlotter)
		);
	}

	private String getPosition(Plotter plotter) {
		String action="";
		Point2D p = plotter.getPos();
		action += " X" + StringHelper.formatDouble(p.x);
		action += " Y" + StringHelper.formatDouble(p.y);
		return action;
	}

	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setRollover(true);

		bESTOP.setFont(getFont().deriveFont(Font.BOLD));
		bESTOP.setForeground(Color.RED);

		bESTOP.addActionListener((e) -> sendESTOP() );
		bGetPosition.addActionListener((e) -> sendGetPosition() );
		bSetHome.addActionListener((e) -> sendSetHome() );
		bGoHome.addActionListener((e) -> sendGoHome() );

		bar.add(bESTOP);
		bar.addSeparator();
		bar.add(bGetPosition);
		bar.add(bSetHome);
		bar.add(bGoHome);
		
		updateButtonAccess();

		return bar;
	}

	private void sendESTOP() {
		sendCommand("M112");
	}

	private void sendGetPosition() {
		sendCommand("M114");
	}

	private void updateButtonAccess() {
		boolean isConnected = chatInterface.getIsConnected();

		bESTOP.setEnabled(isConnected);
		bGetPosition.setEnabled(isConnected);
		bSetHome.setEnabled(isConnected);
		bGoHome.setEnabled(isConnected);
	}

	private void sendSetHome() {
		Plotter temp = new Plotter();
		sendCommand("G92"+getPosition(temp));
		myPlotter.setPos(temp.getPos());
	}

	private void sendGoHome() {
		Plotter temp = new Plotter();
		myPlotter.setPos(temp.getPos());
	}

	public void setNetworkSession(NetworkSession session) {
		chatInterface.setNetworkSession(session);
	}

	// TEST

	public static void main(String[] args) {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		JFrame frame = new JFrame(MarlinInterface.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MarlinInterface(new Plotter()));
		frame.pack();
		frame.setVisible(true);
	}
}
