package com.marginallyclever.makelangelo.plotter.plottercontrols;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link MarlinPanel} manages communication with a remote device running Marlin firmware.
 * In the OSI model of network interfaces this is the Presentation/Syntax layer which
 * "ensures that data is in a usable format and is where data encryption occurs".
 * That means checksum verification and resend control.
 * 
 * @author Dan Royer
 * @since 7.28.0
 */
public class MarlinPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(MarlinPanel.class);

	// number of commands we'll hold on to in case there's a resend.
	private static final int HISTORY_BUFFER_LIMIT = 250;
	// Marlin can buffer this many commands from serial, before processing.
	private static final int MARLIN_SEND_LIMIT = 20;
	// If nothing is heard for this many ms then send a ping to check if the connection is still live. 
	private static final int TIMEOUT_DELAY = 5000;
	// Max duration before alerting the user something is wrong
	private static final int FATAL_TIMEOUT_DELAY = TIMEOUT_DELAY * 5;
	// Marlin says this when a resend is needed, followed by the last well-received line number.
	private static final String STR_RESEND = "Resend: ";
	// Marlin sends this event when the robot is ready to receive more.
	private static final String STR_OK = "ok";
	// Marlin sends this message when an error happened.
	public static final String STR_ERROR = "Error:";
	// Marlin sends this message when a fatal error occured.
	public static final String STR_PRINTER_HALTED = "Printer halted";
	// Marlin sends this event when the robot must be homed first
	private static final String STR_HOME_XY_FIRST = "echo:Home XY First";

	// Marlin sends this message when the robot is sending an Action Command.
	public static final String STR_ACTION_COMMAND = "//action:";
	public static final String PROMPT_BEGIN = "prompt_begin";
	public static final String PROMPT_CHOICE = "prompt_choice";
	public static final String PROMPT_BUTTON = "prompt_button";
	public static final String PROMPT_SHOW = "prompt_show";
	public static final String PROMPT_END = "prompt_end";
	public static final String STR_I_HANDLE_DIALOGS = "M876 P1";

	private final TextInterfaceToNetworkSession chatInterface;

	private final List<MarlinCommand> myHistory = new ArrayList<>();

	// the next line number I should send.  Marlin may say "please resend previous line x", which would change this.
	private int lineNumberToSend;
	// the last line number added to the queue.
	private int lineNumberAdded;
	// don't send more than this many at a time without acknowledgement.
	private int busyCount = MARLIN_SEND_LIMIT;
	
	private final Timer timeoutChecker = new Timer(TIMEOUT_DELAY,(e)->onTimeoutCheck());
	private long lastReceivedTime;

	private final ActionCommandDialog promptDialog = new ActionCommandDialog();
	private boolean waitingForResponse = false;
	
	public MarlinPanel(ChooseConnection chooseConnection) {
		super(new BorderLayout());

		chatInterface = new TextInterfaceToNetworkSession(chooseConnection);
		this.add(chatInterface, BorderLayout.CENTER);

		chatInterface.addListener((e) -> {
			switch (e.flag) {
				case NetworkSessionEvent.CONNECTION_OPENED -> onConnect();
				case NetworkSessionEvent.CONNECTION_CLOSED -> onClose();
			}
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
		lastReceivedTime = System.currentTimeMillis();
		queueAndSendCommand(STR_I_HANDLE_DIALOGS);
		// timeoutChecker uses lastReceivedTime to check if the connection is still live.
		// so start it after setting the lastReceived time or the first check will fail.
		timeoutChecker.start();
	}
	
	private void onClose() {
		logger.debug("MarlinInterface disconnected.");
		timeoutChecker.stop();
	}
	
	private void onTimeoutCheck() {
		long delay = System.currentTimeMillis() - lastReceivedTime;
		if (delay > TIMEOUT_DELAY) {
			if (delay > FATAL_TIMEOUT_DELAY) {
				logger.error("No answer from the robot");
				notifyListeners( MarlinPanelEvent.COMMUNICATION_FAILURE, "communicationFailure");
				chatInterface.displayError("No answer from the robot, retrying...");
			} else {
				logger.trace("Heartbeat: M400");
				chatInterface.sendCommand("M400");
			}
		}
	}

	private void setupNetworkListener() {
		chatInterface.addNetworkSessionListener(this::onDataReceived);
	}
	
	// This does not fire on the Swing EVT thread.  Be careful!  Concurrency problems may happen.
	protected void onDataReceived(NetworkSessionEvent evt) {
		if (evt.flag == NetworkSessionEvent.DATA_RECEIVED) {
			lastReceivedTime = System.currentTimeMillis();
			String message = ((String)evt.data).trim();

			logger.trace("received '{}'", message.trim());
			if(message.startsWith(STR_OK)) {
				onHearOK();
			} else if(message.contains(STR_RESEND)) {
				onHearResend(message);
			} else if(message.startsWith(STR_ERROR)) {
				onHearError(message.substring(STR_ERROR.length()).trim());
			} else if(message.startsWith(STR_HOME_XY_FIRST)) {
				onHearHomeXYFirst();
			} else if(message.startsWith(STR_ACTION_COMMAND)) {
				onHearActionCommand(message.substring(STR_ACTION_COMMAND.length()).trim());
			}
		}
	}

	private void onHearResend(String message) {
		String numberPart = message.substring(message.indexOf(STR_RESEND) + STR_RESEND.length());
		try {
			int n = Integer.parseInt(numberPart);
			if (n > lineNumberAdded) {
				logger.warn("Resend line {} asked but never sent", n);
			}
			if (n > lineNumberAdded - MarlinPanel.HISTORY_BUFFER_LIMIT) {
				// no problem.
				lineNumberToSend = n;
			} else {
				// line is no longer in the buffer.  should not be possible!
				logger.warn("Resend line {} asked but no longer in the buffer", n);
			}
		} catch (NumberFormatException e) {
			logger.debug("Resend request for '{}' failed: {}", message, e.getMessage());
		}
	}

	private void onHearOK() {
		SwingUtilities.invokeLater(() -> {
    		busyCount++;
    		sendQueuedCommand();
        	clearOldHistory();
    		if(lineNumberToSend>=lineNumberAdded && !waitingForResponse) {
    			fireIdleNotice();
    		}
        });
	}

	private void onHearError(String message) {
		logger.error("Error from printer '{}'", message);
		
		// only notify listeners of a fatal error (MarlinInterface.ERROR) if the printer halts.
		if (message.contains(STR_PRINTER_HALTED)) {
			notifyListeners( MarlinPanelEvent.ERROR, STR_PRINTER_HALTED );
		}
	}

	private void onHearHomeXYFirst() {
		logger.warn("Home XY First");
		notifyListeners( MarlinPanelEvent.HOME_XY_FIRST,"homeXYFirst" );
	}

	private void onHearActionCommand(String command) {
		logger.debug("Action command {}", command);

		processActionCommand(command);

		notifyListeners( MarlinPanelEvent.ACTION_COMMAND, command );
	}

	private void onDidNotFindCommandInHistory() {
		notifyListeners( MarlinPanelEvent.DID_NOT_FIND, "didNotFind" );
	}

	private void fireIdleNotice() {
		notifyListeners( MarlinPanelEvent.IDLE, "idle" );
	}

	private void clearOldHistory() {
		while(myHistory.size()>0 && myHistory.get(0).lineNumber < lineNumberAdded-HISTORY_BUFFER_LIMIT) {
			myHistory.remove(0);
		}
	}
	
	public void queueAndSendCommand(String str) {
		str = removeComment(str);
		if(str.length()==0) return;

		lineNumberAdded++;
		String withLineNumber = "N"+lineNumberAdded+" "+str;
		String assembled = withLineNumber + generateChecksum(withLineNumber);
		myHistory.add(new MarlinCommand(lineNumberAdded,assembled));
		if(busyCount>0) sendQueuedCommand();
	}

	public String removeComment(String str) {
		int first = str.indexOf(';');
		if(first>-1) {
			str = str.substring(0,first);
		}
		return str.trim();
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


	private void processActionCommand(String actionCommand) {
		if(actionCommand.startsWith(PROMPT_BEGIN)) {
			promptDialog.setPromptMessage(actionCommand.substring(PROMPT_BEGIN.length()));
			promptDialog.clearPrompts();
		} else if(actionCommand.startsWith(PROMPT_CHOICE)) {
			promptDialog.addOption(actionCommand.substring(PROMPT_CHOICE.length()).trim());
		} else if(actionCommand.startsWith(PROMPT_BUTTON)) {
			promptDialog.addOption(actionCommand.substring(PROMPT_BUTTON.length()).trim());
		} else if(actionCommand.startsWith(PROMPT_SHOW)) {
			promptDialog.run(this, Translator.get("InfoTitle"),(result)-> {
				queueAndSendCommand("M876 S" + Math.max(0,result));
				waitingForResponse = false;
				fireIdleNotice();
			});
		} else if(actionCommand.startsWith(PROMPT_END)) {
			if(promptDialog.isOpen()) {
				// close the dialog because user clicked dial on robot LCD.
				promptDialog.close();
				waitingForResponse = false;
				fireIdleNotice();
			}
		}
	}

	// OBSERVER PATTERN
	
	private final List<MarlinPanelListener> listeners = new ArrayList<>();

	public void addListener(MarlinPanelListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MarlinPanelListener listener) {
		listeners.remove(listener);
	}

	private void notifyListeners(int id,String command) {
		MarlinPanelEvent event = new MarlinPanelEvent(this,id,command);
		for (MarlinPanelListener listener : listeners) listener.actionPerformed(event);
	}

	// OBSERVER PATTERN ENDS

	// TEST

	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(MarlinPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MarlinPanel(new ChooseConnection()));
		frame.pack();
		frame.setVisible(true);
	}
}
