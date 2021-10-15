package com.marginallyclever.makelangelo.plotter.plotterControls;

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
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
//import com.marginallyclever.makelangelo.DialogBadFirmwareVersion;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterEvent;
import com.marginallyclever.util.PreferencesHelper;

public class MarlinInterface extends JPanel {
	private static final long serialVersionUID = 979851120390943303L;
	// number of commands we'll hold on to in case there's a resend.
	private static final int HISTORY_BUFFER_LIMIT = 50;
	// Marlin can buffer this many commands from serial, before processing.
	private static final int MARLIN_SEND_LIMIT = 20;
	// If nothing is heard for this many ms then send a ping to check if the connection is still live. 
	private static final int TIMEOUT_DELAY = 2000;
	// Marlin says this when a resend is needed, followed by the last well-received line number.
	private static final String STR_RESEND = "Resend: ";
	// Marlin sends this event when the robot is ready to receive more.
	private static final String STR_OK = "ok";
	// MarlinInterface sends this as an ActionEvent to let listeners know it can handle more input.
	public static final String IDLE = "idle";
	private static final double MARLIN_DRAW_FEEDRATE = 7500.0;  // mm/min

	private static final String STR_FEEDRATE = "echo:  M203";
	private static final String STR_ACCELERATION = "echo:  M201";
	
	//private static final String STR_ECHO = "echo:";
	//private static final String STR_ERROR = "Error:";

	private Plotter myPlotter;
	private TextInterfaceToNetworkSession chatInterface = new TextInterfaceToNetworkSession();
	private ArrayList<MarlinCommand> myHistory = new ArrayList<MarlinCommand>();

	private JButton bESTOP = new JButton("EMERGENCY STOP");
	private JButton bGetPosition = new JButton("M114");
	private JButton bGoHome = new JButton("Go Home");

	// the next line number I should send.  Marlin may say "please resend previous line x", which would change this.
	private int lineNumberToSend;
	// the last line number added to the queue.
	private int lineNumberAdded;
	// don't send more than this many at a time without acknowledgement.
	private int busyCount=MARLIN_SEND_LIMIT;
	
	private Timer timeoutChecker = new Timer(10000,(e)->onTimeoutCheck());
	private long lastReceivedTime;
	
	public MarlinInterface(Plotter plotter) {
		super();

		myPlotter = plotter;

		this.setLayout(new BorderLayout());
		this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(chatInterface, BorderLayout.CENTER);

		plotter.addListener((e) -> onPlotterEvent(e));

		chatInterface.addActionListener((e) -> {
			switch (e.getID()) {
			case ChooseConnection.CONNECTION_OPENED:
				onConnect();
				notifyListeners(e);
				break;
			case ChooseConnection.CONNECTION_CLOSED:
				onClose();
				updateButtonAccess();
				notifyListeners(e);
				break;
			}
		});
	}

	public void addNetworkSessionListener(NetworkSessionListener a) {
		chatInterface.addNetworkSessionListener(a);
	}

	/*@Deprecated
	private void whenBadFirmwareDetected(String versionFound) {
		(new DialogBadFirmwareVersion()).display(this, versionFound);
	}

	@Deprecated
	private void whenBadHardwareDetected(String versionFound) {
		JOptionPane.showMessageDialog(this, Translator.get("hardwareVersionBadMessage", new String[]{versionFound}));
	}*/
	
	private void onPlotterEvent(PlotterEvent e) {
		switch(e.type) {
		case PlotterEvent.HOME_FOUND:
			//System.out.println("MarlinInterface heard plotter home.");
			sendFindHome();
			break;
		case PlotterEvent.POSITION:
			//System.out.println("MarlinInterface heard plotter move.");
			sendGoto();
			break;
		case PlotterEvent.PEN_UPDOWN:
			//System.out.println("MarlinInterface heard plotter up/down.");
			sendPenUpDown();
			break;
		case PlotterEvent.MOTORS_ENGAGED:
			//System.out.println("MarlinInterface heard plotter engage.");
			sendEngage();
			break;
		case PlotterEvent.TOOL_CHANGE:
			//System.out.println("MarlinInterface heard plotter tool change.");
			sendToolChange((int)e.extra);
			break;
		default: break;
		}
	}

	private void sendToolChange(int toolNumber) {
		queueAndSendCommand(getToolChangeString(toolNumber));
	}

	private void sendFindHome() {
		queueAndSendCommand("G28 XY");
	}
	
	private static String getColorName(int toolNumber) {
		String name = "";
		switch (toolNumber) {
		case 0xff0000:  name = "red";		break;
		case 0x00ff00:  name = "green";		break;
		case 0x0000ff:  name = "blue";		break;
		case 0x000000:  name = "black";		break;
		case 0x00ffff:  name = "cyan";		break;
		case 0xff00ff:  name = "magenta";	break;
		case 0xffff00:  name = "yellow";	break;
		case 0xffffff:  name = "white";		break;
		default:
			name = "0x" + Integer.toHexString(toolNumber);
			break; // display unknown RGB value as hex
		}
		return name;
	}
	
	private void sendPenUpDown() {
		String str = myPlotter.getPenIsUp() 
				? MarlinInterface.getPenUpString(myPlotter)
				: MarlinInterface.getPenDownString(myPlotter);
		queueAndSendCommand(str);
	}

	private void sendEngage() {
		queueAndSendCommand( myPlotter.getAreMotorsEngaged() ? "M17" : "M18" );
	}

	private void onConnect() {
		Log.message("MarlinInterface connected.");
		setupListener();
		lineNumberToSend=1;
		lineNumberAdded=0;
		myHistory.clear();
		updateButtonAccess();
		timeoutChecker.start();
	}
	
	private void onClose() {
		Log.message("MarlinInterface disconnected.");
		timeoutChecker.stop();
	}
	
	private void onTimeoutCheck() {
		if(System.currentTimeMillis()-lastReceivedTime>TIMEOUT_DELAY) {
			chatInterface.sendCommand("M400");
		}
	}

	private void setupListener() {
		chatInterface.addNetworkSessionListener((evt) -> onDataReceived(evt));
	}
	
	// This does not fire on the Swing EVT thread.  Be careful!  Concurrency problems may happen.
	private void onDataReceived(NetworkSessionEvent evt){
		if(evt.flag == NetworkSessionEvent.DATA_RECEIVED) {
			lastReceivedTime=System.currentTimeMillis();
			String message = ((String)evt.data).trim();
			//Log.message("MarlinInterface received '"+message.trim()+"'.");
			if(message.startsWith("X:") && message.contains("Count")) {
				onHearM114(message);
			} else if(message.startsWith(STR_FEEDRATE)) {
				onHearFeedrate(message);
			} else if(message.startsWith(STR_ACCELERATION)) {
				onHearAcceleration(message);
			} else if(message.startsWith(STR_OK)) {
				onHearOK();
			} else if(message.contains(STR_RESEND)) {
				onHearResend(message);
			}
		}
	}
	
	// format is "echo:  M201 X5400.00 Y5400.00 Z5400.00"
	// I only care about the x value when reading.
	private void onHearAcceleration(String message) {
		message = message.substring(STR_ACCELERATION.length());
		String [] parts = message.split("\s");
		if(parts.length!=4) return;  // TODO exception when M201 is broken?
		double v=Double.valueOf(parts[1].substring(1));
		Log.message("MarlinInterface found acceleration "+v);
		myPlotter.getSettings().setAcceleration(v);
	}

	// format is "echo:  M203 X5400.00 Y5400.00 Z5400.00"
	// I only care about the x value when reading.
	private void onHearFeedrate(String message) {
		message = message.substring(STR_FEEDRATE.length());
		String [] parts = message.split("\s");
		if(parts.length!=4) return;  // TODO exception when M201 is broken?
		double v=Double.valueOf(parts[1].substring(1));
		Log.message("MarlinInterface found feedrate "+v);
		myPlotter.getSettings().setDrawFeedRate(v);
	}

	private void onHearResend(String message) {
		String numberPart = message.substring(message.indexOf(STR_RESEND) + STR_RESEND.length());
		try {
			int n = Integer.valueOf(numberPart);
			if(n>lineNumberAdded-MarlinInterface.HISTORY_BUFFER_LIMIT) {
				// no problem.
				lineNumberToSend=n;
			} else {
				// line is no longer in the buffer.  should not be possible!
			}
		} catch(NumberFormatException e) {
			Log.message("Resend request for '"+message+"' failed: "+e.getMessage());
		}
	}

	private void onHearOK() {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        		busyCount++;
        		sendQueuedCommand();
            	clearOldHistory();
        		if(lineNumberToSend>=lineNumberAdded) {
        			fireIdleNotice();
        		}
            }
        });
	}

	private void fireIdleNotice() {
		notifyListeners(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,MarlinInterface.IDLE));
	}

	private void clearOldHistory() {
		while(myHistory.size()>0 && myHistory.get(0).lineNumber<lineNumberAdded-HISTORY_BUFFER_LIMIT) {
			myHistory.remove(0);
		}
	}

	public void queueAndSendCommand(String str) {
		if(str.trim().length()==0) return;
		
		lineNumberAdded++;
		String withLineNumber = "N"+lineNumberAdded+" "+str;
		String assembled = withLineNumber + generateChecksum(withLineNumber);
		myHistory.add(new MarlinCommand(lineNumberAdded,assembled));
		//System.out.println("MarlinInterface queued '"+assembled+"'.  busyCount="+busyCount);
		if(busyCount>0) sendQueuedCommand();
	}
	
	private void sendQueuedCommand() {
		clearOldHistory();
		
		if(myHistory.size()==0) return;
		
		int smallest = Integer.MAX_VALUE;
		for( MarlinCommand mc : myHistory ) {
			if(smallest > mc.lineNumber) smallest = mc.lineNumber;
			if(mc.lineNumber == lineNumberToSend) {
				busyCount--;
				lineNumberToSend++;
				//System.out.println("MarlinInterface sending '"+mc.command+"'.");
				chatInterface.sendCommand(mc.command);
				return;
			}
		}
		
		if(smallest>lineNumberToSend) {
			// history no longer contains the line?!
			System.out.println("MarlinInterface did not find "+lineNumberToSend);
			for( MarlinCommand mc : myHistory ) {
				System.out.println("..."+mc.lineNumber+": "+mc.command);
			}
		}
	}

	private String generateChecksum(String line) {
		byte checksum = 0;

		int i=line.length();
		while(i>0) checksum ^= (byte)line.charAt(--i);

		return "*" + Integer.toString(checksum);
	}

	public boolean getIsBusy() {
		return busyCount<=0;
	}
	
	// format is normally X:0.00 Y:270.00 Z:0.00 Count X:0 Y:0 Z:0 U:0 V:0 W:0
	// trim everything after and including "Count", then read the state data.
	private void onHearM114(String message) {
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
		Point2D p = myPlotter.getPos();
		String msg = myPlotter.getPenIsUp() 
				? MarlinInterface.getTravelTo(p.x,p.y)
				: MarlinInterface.getDrawTo(p.x,p.y);
		queueAndSendCommand( msg );
	}
	
	// "By convention, most G-code generators use G0 for non-extrusion movements"
	// https://marlinfw.org/docs/gcode/G000-G001.html
	public static String getTravelTo(double x,double y) {
		return "G0"+getPosition(x,y);
	}
	
	// "By convention, most G-code generators use G0 for non-extrusion movements"
	// https://marlinfw.org/docs/gcode/G000-G001.html
	public static String getDrawTo(double x,double y) {
		return "G1"+getPosition(x,y)+" F"+MARLIN_DRAW_FEEDRATE;
	}

	private static String getPosition(double x,double y) {
		String action=
				" X" + StringHelper.formatDouble(x) +
				" Y" + StringHelper.formatDouble(y);
		return action;
	}

	public static String getPenUpString(Plotter p) {
		return "M280 P0 S"+(int)p.getPenUpAngle()  +" T" + (int)p.getPenLiftTime();
	}

	public static String getPenDownString(Plotter p) {
		return "M280 P0 S"+(int)p.getPenDownAngle()  +" T50";
	}

	public static String getToolChangeString(int toolNumber) {
		String colorName = getColorName(toolNumber & 0xFFFFFF);
		return "M0 Ready "+colorName+" and click";
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

	public void closeConnection() {
		this.chatInterface.closeConnection();
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
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}

		JFrame frame = new JFrame(MarlinInterface.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MarlinInterface(new Plotter()));
		frame.pack();
		frame.setVisible(true);
	}
}
