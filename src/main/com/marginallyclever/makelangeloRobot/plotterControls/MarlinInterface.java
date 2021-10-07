package com.marginallyclever.makelangeloRobot.plotterControls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.DialogBadFirmwareVersion;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.Plotter;
import com.marginallyclever.makelangeloRobot.PlotterEvent;

// TODO add a timeout check in case "ok" is not received somehow.

public class MarlinInterface extends JPanel {
	private static final long serialVersionUID = 979851120390943303L;
	// number of commands we'll hold on to in case there's a resend.
	private static final int HISTORY_BUFFER_LIMIT = 50;
	// Marlin can buffer this many commands from serial, before processing.
	private static final int MARLIN_SEND_LIMIT = 20;
	// Marlin says this when a resend is needed, followed by the last well-received line number.
	private static final String STR_RESEND = "Resend: ";
	// Marlin sends this event when the robot is ready to receive more.
	public static final String IDLE = "idle";

	private Plotter myPlotter;

	private int lineNumberToSend;
	private int lineNumberAdded;
	
	// don't send more than 20 at a time.
	private int busyCount=MARLIN_SEND_LIMIT;

	private TextInterfaceToNetworkSession chatInterface = new TextInterfaceToNetworkSession();

	private JButton bESTOP = new JButton("EMERGENCY STOP");
	private JButton bGetPosition = new JButton("M114");
	private JButton bGoHome = new JButton("Go Home");
	
	private ArrayList<MarlinCommand> myHistory = new ArrayList<MarlinCommand>();

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
				notifyListeners(e);
				break;
			case ChooseConnectionPanel.CONNECTION_CLOSED:
				updateButtonAccess();
				notifyListeners(e);
				break;
			}
		});
	}
	
	public void addNetworkSessionListener(NetworkSessionListener a) {
		chatInterface.addNetworkSessionListener(a);
	}

	@SuppressWarnings("unused")
	private void whenBadFirmwareDetected(String versionFound) {
		(new DialogBadFirmwareVersion()).display(this, versionFound);
	}

	@SuppressWarnings("unused")
	private void whenBadHardwareDetected(String versionFound) {
		JOptionPane.showMessageDialog(this, Translator.get("hardwareVersionBadMessage", new String[]{versionFound}));
	}
	
	private void onPlotterEvent(PlotterEvent e) {
		switch(e.type) {
		case PlotterEvent.HOME_FOUND: sendFindHome();  break;
		case PlotterEvent.POSITION: sendGoto(); break;
		case PlotterEvent.PEN_UPDOWN: sendPenUpDown(); break;
		case PlotterEvent.MOTORS_ENGAGED: sendEngage(); break;
		default: break;
		}
	}

	private void sendFindHome() {
		queueAndSendCommand("G28 XY");
	}

	private void sendPenUpDown() {
		queueAndSendCommand( myPlotter.getPenIsUp() 
				? myPlotter.getSettings().getPenUpString()
				: myPlotter.getSettings().getPenDownString() );
	}

	private void sendEngage() {
		queueAndSendCommand( myPlotter.getAreMotorsEngaged() ? "M17" : "M18" );
	}

	private void onConnect() {
		setupListener();
		lineNumberToSend=0;
		lineNumberAdded=0;
		myHistory.clear();
		updateButtonAccess();
	}
	

	private void setupListener() {
		chatInterface.addNetworkSessionListener((evt) -> {
			if(evt.flag == NetworkSessionEvent.DATA_RECEIVED) {
				String message = ((String)evt.data).trim();
				if(message.startsWith("X:") && message.contains("Count")) {
					//System.out.println("FOUND " + message);
					processM114Reply(message);
				} else if(message.startsWith("ok")) {
					busyCount++;
					sendQueuedCommand();
					if(busyCount>0) {
						clearOldHistory();
						fireIdleNotice();
					}
				} else if(message.contains(STR_RESEND)) {
					String numberPart = message.substring(message.indexOf(STR_RESEND) + STR_RESEND.length());
					int n = Integer.valueOf(numberPart);
					if(n>lineNumberAdded-MarlinInterface.HISTORY_BUFFER_LIMIT) {
						// no problem.
						lineNumberToSend=n;
					}
				} 
			}
		});
	}
	
	private void fireIdleNotice() {
		notifyListeners(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,MarlinInterface.IDLE));
	}

	private void clearOldHistory() {
		while(myHistory.get(0).lineNumber<lineNumberAdded-HISTORY_BUFFER_LIMIT) {
			myHistory.remove(0);
		}
	}

	public void queueAndSendCommand(String str) {
		lineNumberAdded++;
		str = "N"+lineNumberAdded+" "+str;
		str += generateChecksum(str);
		myHistory.add(new MarlinCommand(lineNumberAdded,str));
		
		if(busyCount>0) sendQueuedCommand();
	}
	
	private void sendQueuedCommand() {
		for( MarlinCommand mc : myHistory ) {
			if(mc.lineNumber == lineNumberToSend) {
				busyCount--;
				lineNumberToSend++;
				chatInterface.sendCommand(mc.command);
				return;
			}
		}
		// got here without hitting a line?  history no longer contains line?!  
	}

	private String generateChecksum(String line) {
		byte checksum = 0;

		for (int i = 0; i < line.length(); ++i) {
			checksum ^= line.charAt(i);
		}

		return "*" + Integer.toString(checksum);
	}

	public boolean getIsBusy() {
		return busyCount<=0;
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
		queueAndSendCommand(
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
		bGoHome.addActionListener((e) -> sendGoHome() );

		bar.add(bESTOP);
		bar.addSeparator();
		bar.add(bGetPosition);
		bar.add(bGoHome);
		
		updateButtonAccess();

		return bar;
	}

	private void sendESTOP() {
		queueAndSendCommand("M112");
	}

	private void sendGetPosition() {
		queueAndSendCommand("M114");
	}

	private void updateButtonAccess() {
		boolean isConnected = chatInterface.getIsConnected();

		bESTOP.setEnabled(isConnected);
		bGetPosition.setEnabled(isConnected);
		bGoHome.setEnabled(isConnected);
	}

	private void sendGoHome() {
		Plotter temp = new Plotter();
		myPlotter.setPos(temp.getPos());
	}

	public void setNetworkSession(NetworkSession session) {
		chatInterface.setNetworkSession(session);
	}
	
	// OBSERVER PATTERN
	
	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	public void addListener(ActionListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ActionListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners(ActionEvent e) {
		for (ActionListener listener : listeners) listener.actionPerformed(e);
	}

	// OBSERVER PATTERN ENDS

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
