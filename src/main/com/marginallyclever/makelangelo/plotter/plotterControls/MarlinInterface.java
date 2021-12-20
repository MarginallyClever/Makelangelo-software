package com.marginallyclever.makelangelo.plotter.plotterControls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

/**
 * In the OSI model of network interfaces this is the Presentation/Syntax layer which
 * "ensures that data is in a usable format and is where data encryption occurs".
 * In our case the "encryption" is checksum and resend-control.
 * 
 * @author Dan Royer
 */
public class MarlinInterface extends JPanel {
	private static final long serialVersionUID = 979851120390943303L;
	// number of commands we'll hold on to in case there's a resend.
	private static final int HISTORY_BUFFER_LIMIT = 250;
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

	//private static final String STR_ECHO = "echo:";
	//private static final String STR_ERROR = "Error:";

	private TextInterfaceToNetworkSession chatInterface = new TextInterfaceToNetworkSession();
	private ArrayList<MarlinCommand> myHistory = new ArrayList<MarlinCommand>();

	private JButton bESTOP = new JButton("EMERGENCY STOP");

	// the next line number I should send.  Marlin may say "please resend previous line x", which would change this.
	private int lineNumberToSend;
	// the last line number added to the queue.
	private int lineNumberAdded;
	// don't send more than this many at a time without acknowledgement.
	private int busyCount=MARLIN_SEND_LIMIT;
	
	private Timer timeoutChecker = new Timer(10000,(e)->onTimeoutCheck());
	private long lastReceivedTime;
	
	public MarlinInterface() {
		super();

		this.setLayout(new BorderLayout());
		this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(chatInterface, BorderLayout.CENTER);

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

	private void onConnect() {
		Log.message("MarlinInterface connected.");
		setupNetworkListener();
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

	private void setupNetworkListener() {
		chatInterface.addNetworkSessionListener((evt) -> onDataReceived(evt));
	}
	
	// This does not fire on the Swing EVT thread.  Be careful!  Concurrency problems may happen.
	protected void onDataReceived(NetworkSessionEvent evt) {
		if(evt.flag == NetworkSessionEvent.DATA_RECEIVED) {
			lastReceivedTime=System.currentTimeMillis();
			String message = ((String)evt.data).trim();
			//Log.message("MarlinInterface received '"+message.trim()+"'.");
			if(message.startsWith(STR_OK)) {
				onHearOK();
			} else if(message.contains(STR_RESEND)) {
				onHearResend(message);
			}
		}
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
		SwingUtilities.invokeLater(() -> {
    		busyCount++;
    		sendQueuedCommand();
        	clearOldHistory();
    		if(lineNumberToSend>=lineNumberAdded) {
    			fireIdleNotice();
    		}
        });
	}

	private void fireIdleNotice() {
		notifyListeners(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,MarlinInterface.IDLE));
	}

	private void clearOldHistory() {
		while(myHistory.size()>0 && myHistory.get(0).lineNumber < lineNumberAdded-HISTORY_BUFFER_LIMIT) {
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

	
	protected JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setRollover(true);

		bESTOP.setFont(getFont().deriveFont(Font.BOLD));
		bESTOP.setForeground(Color.RED);

		bESTOP.addActionListener((e) -> sendESTOP() );
		bar.add(bESTOP);
		//bar.addSeparator();
		
		updateButtonAccess();

		return bar;
	}

	private void sendESTOP() {
		chatInterface.sendCommand("M112");
		chatInterface.sendCommand("M112");
		chatInterface.sendCommand("M112");
	}

	private void updateButtonAccess() {
		boolean isConnected = chatInterface.getIsConnected();

		bESTOP.setEnabled(isConnected);
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

		JFrame frame = new JFrame(MarlinInterface.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MarlinInterface());
		frame.pack();
		frame.setVisible(true);
	}
}
