package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link MarlinInterface} manages communication with a remote device running Marlin firmware.
 * 
 * In the OSI model of network interfaces this is the Presentation/Syntax layer which
 * "ensures that data is in a usable format and is where data encryption occurs".
 * That means checksum verification and resend control.
 * 
 * @author Dan Royer
 * @since 7.28.0
 */
public class MarlinInterface extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(MarlinInterface.class);
	
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
	// Marlin sends this message when an error happened.
	private static final String STR_ERROR = "Error:";
	// Marlin sends this message when a fatal error occured.
	private static final String STR_PRINTER_HALTED = "Printer halted";
	// Marlin sends this event when the robot must be homed first
	private static final String STR_HOME_XY_FIRST = "echo:Home XY First";

	// MarlinInterface sends this as an ActionEvent to let listeners know it can handle more input.
	public static final String IDLE = "idle";
	// MarlinInterface sends this as an ActionEvent to let listeners know it can handle an error.
	public static final String ERROR = "error";
	// MarlinInterface sends this as an ActionEvent to let listeners know it must home first.
	public static final String HOME_XY_FIRST = "homexyfirst";
	// MarlinInterface sends this as an ActionEvent to let listeners know there is an error in the transmission.
	public static final String DID_NOT_FIND = "didnotfind";

	private TextInterfaceToNetworkSession chatInterface;
	private List<MarlinCommand> myHistory = new ArrayList<>();

	// the next line number I should send.  Marlin may say "please resend previous line x", which would change this.
	private int lineNumberToSend;
	// the last line number added to the queue.
	private int lineNumberAdded;
	// don't send more than this many at a time without acknowledgement.
	private int busyCount=MARLIN_SEND_LIMIT;
	
	private Timer timeoutChecker = new Timer(10000,(e)->onTimeoutCheck());
	private long lastReceivedTime;
	
	public MarlinInterface(ChooseConnection chooseConnection) {
		super();

		this.setLayout(new BorderLayout());
		chatInterface = new TextInterfaceToNetworkSession(chooseConnection);
		this.add(chatInterface, BorderLayout.CENTER);

		chatInterface.addListener((e) -> {
			switch (e.flag) {
				case NetworkSessionEvent.CONNECTION_OPENED -> onConnect();
				case NetworkSessionEvent.CONNECTION_CLOSED -> onClose();
			}
			// TODO notifyListeners(e);
		});
	}

	public void addNetworkSessionListener(NetworkSessionListener a) {
		chatInterface.addNetworkSessionListener(a);
	}

	private void onConnect() {
		logger.debug("MarlinInterface connected.");
		setupNetworkListener();
		lineNumberToSend=1;
		lineNumberAdded=0;
		myHistory.clear();
		timeoutChecker.start();
	}
	
	private void onClose() {
		logger.debug("MarlinInterface disconnected.");
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
		if (evt.flag == NetworkSessionEvent.DATA_RECEIVED) {
			lastReceivedTime = System.currentTimeMillis();
			String message = ((String)evt.data).trim();

			logger.trace("received '{}'", message.trim());
			if (message.startsWith(STR_OK)) {
				onHearOK();
			} else if (message.contains(STR_RESEND)) {
				onHearResend(message);
			} else if (message.startsWith(STR_ERROR)) {
				onHearError(message);
			} else if (message.startsWith(STR_HOME_XY_FIRST)) {
				onHearHomeXYFirst();
			} else if (message.startsWith(STR_PRINTER_HALTED)) {
				onHearError(message);
			}
		}
	}

	private void onHearResend(String message) {
		String numberPart = message.substring(message.indexOf(STR_RESEND) + STR_RESEND.length());
		try {
			int n = Integer.parseInt(numberPart);
			if (n>lineNumberAdded-MarlinInterface.HISTORY_BUFFER_LIMIT) {
				// no problem.
				lineNumberToSend=n;
			} else {
				// line is no longer in the buffer.  should not be possible!
			}
		} catch(NumberFormatException e) {
			logger.debug("Resend request for '{}' failed: {}", message, e.getMessage());
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

	private void onHearError(String message) {
		logger.error("Error from printer '{}'", message);
		notifyListeners(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, MarlinInterface.ERROR));
	}

	private void onHearHomeXYFirst() {
		notifyListeners(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, MarlinInterface.HOME_XY_FIRST));
	}

	private void onDidNotFindCommandInHistory() {
		notifyListeners(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, MarlinInterface.DID_NOT_FIND));
	}

	private void fireIdleNotice() {
		notifyListeners(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, MarlinInterface.IDLE));
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
		//logger.debug("MarlinInterface queued '{}'. busyCount={}", assembled, busyCount);
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
				logger.trace("sending '{}'", mc.command);
				chatInterface.sendCommand(mc.command);
				return;
			}
		}
		
		if(smallest>lineNumberToSend) {
			// history no longer contains the line?!
			logger.warn("Did not find {}", lineNumberToSend);
			onDidNotFindCommandInHistory();
			if (logger.isDebugEnabled()) {
				for (MarlinCommand mc : myHistory) {
					logger.debug("...{}: {}", mc.lineNumber, mc.command);
				}
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

	public void sendESTOP() {
		logger.warn("Emergency stop");
		chatInterface.sendCommand("M112");
		chatInterface.sendCommand("M112");
		chatInterface.sendCommand("M112");
	}

	// OBSERVER PATTERN
	
	private List<ActionListener> listeners = new ArrayList<>();

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
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(MarlinInterface.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MarlinInterface(new ChooseConnection()));
		frame.pack();
		frame.setVisible(true);
	}
}
