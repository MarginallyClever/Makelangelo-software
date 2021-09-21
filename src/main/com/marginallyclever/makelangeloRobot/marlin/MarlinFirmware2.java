package com.marginallyclever.makelangeloRobot.marlin;

import java.util.ArrayList;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;
import com.marginallyclever.convenience.log.Log;

public class MarlinFirmware2 implements NetworkSessionListener {
	public static final String CUE = "ok";
	public static final String NOCHECKSUM = "NOCHECKSUM ";
	public static final String BADCHECKSUM = "BADCHECKSUM ";
	public static final String BADLINENUM = "BADLINENUM ";
	public static final String NEWLINE = "\n";
	public static final String COMMENT_START = ";";

	private ArrayList<MarlinEventListener> listeners = new ArrayList<MarlinEventListener>();
	
	private NetworkSession mySession = null;
	private RobotIdentityConfirmationAfterMarlin ric = new RobotIdentityConfirmationAfterMarlin();
	private boolean waitingForCue = false;

	// parsing input from Makelangelo
	private ArrayList<String> commandQueue = new ArrayList<String>();


	public MarlinFirmware2() {}
	
	public void setNetworkSession(NetworkSession session) {
		if(mySession!=null) {
			mySession.removeListener(this);
			mySession.removeListener(ric);
		}
		mySession = session;
		if(mySession!=null) {
			mySession.addListener(this);
			mySession.addListener(ric);
		}
		
		ric.start();
	}

	@Override
	public void networkSessionEvent(NetworkSessionEvent evt) {
		switch(evt.flag) {
		case NetworkSessionEvent.DATA_RECEIVED:
			onReceived((String)evt.data);
			break;
		case NetworkSessionEvent.CONNECTION_CLOSED:
			ric.reset();
			break;
		}
	}
	
	private void onReceived(String oneLine) {
		// check for error
		int error_line = errorReported(oneLine);
		if(error_line != -1) {
			notifyLineError(error_line);
		} else {
			notifyDataReceived(oneLine);
		}
		
		// wait for the cue to send another command
		if(oneLine.indexOf(CUE)==0) {
			waitingForCue=false;
			sendQueuedCommand();
		}
	}
	/**
	 * Check if the robot reports an error and if so what line number.
	 *
	 * @return -1 if there was no error, otherwise the line number containing the error.
	 */
	protected int errorReported(String line) {
		if (line.lastIndexOf(NOCHECKSUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(NOCHECKSUM) + NOCHECKSUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("NOCHECKSUM "+err);
			} catch (Exception e) {}

			return err;
		}
		if (line.lastIndexOf(BADCHECKSUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADCHECKSUM) + BADCHECKSUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("BADCHECKSUM "+err);
			} catch (Exception e) {}

			return err;
		}
		if (line.lastIndexOf(BADLINENUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADLINENUM) + BADLINENUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("BADLINENUM "+err);
			} catch (Exception e) {}

			return err;
		}

		return -1;
	}

	/**
	 * Java string to int is very picky.  this method is slightly less picky.  Only works with positive whole numbers.
	 	 * @param src
	 * @return the portion of the string that is actually a number
	 */
	private String getNumberPortion(String src) {
		src = src.trim();
		int length = src.length();
		String result = "";
		for(int i = 0; i < length; i++) {
			Character character = src.charAt(i);
			if (Character.isDigit(character)) {
				result += character;
			}
		}
		return result;
	}

	public void send(String line) {
		if(line==null || line.length()==0) return;
		
		commandQueue.add(line);
		sendQueuedCommand();
	}
	
	protected void sendQueuedCommand() {
		if( !ric.getPortConfirmed() || waitingForCue ) return;

		if(commandQueue.isEmpty()) {
			notifySendBufferEmpty();
			return;
		}

		try {
			String command=commandQueue.remove(0);
			
			if(command.contains(COMMENT_START)) {
				command = command.substring(0,command.indexOf(COMMENT_START));
				if(command.length()==0) return;
			}

			if(!command.endsWith(NEWLINE)) command+=NEWLINE;

			mySession.sendMessage(command);
			
			waitingForCue=true;
		}
		catch(Exception e1) {
			Log.error("Marlin: "+e1.getLocalizedMessage());
		}
	}
		
	public void sendNewUID(long newUID) {
		send("UID " + newUID);
	}
	
	public String generateChecksum(String line) {
		byte checksum = 0;

		for (int i = 0; i < line.length(); ++i) {
			checksum ^= line.charAt(i);
		}

		return "*" + Integer.toString(checksum);
	}

	/**
	 * Removes comments, processes commands robot doesn't handle, add checksum information.
	 *
	 * @param line command to send
	 */
	public void sendLineWithNumberAndChecksum(String line, int lineNumber) {
		if(!ric.getIdentityConfirmed()) return;

		line = "N" + (lineNumber+1) + " " + line;

		int n = line.indexOf(";");
		if(n>=0) {
			String a = line.substring(0,n);
			String b = line.substring(n);
			line = a + generateChecksum(a) + b;
		} else {
			line += generateChecksum(line);
		}
		
		send(line);
	}

	public void addRobotIdentityEventListener(RobotIdentityEventListener arg0) {
		ric.addRobotIdentityEventListener(arg0);
	}
	
	public String getVersion() {
		return ric.getVersion();
	}

	public boolean getIdentityConfirmed() {
		return ric.getIdentityConfirmed();
	}

	public void deleteAllQueuedCommands() {
		commandQueue.clear();
	}
	
	// OBSERVER PATTERN
	
	public void addListener(MarlinEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(MarlinEventListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners(MarlinEvent evt) {
		for( MarlinEventListener a : listeners ) {
			a.marlinEvent(evt);
		}
	}

	// OBSERVER CONVENIENCE METHODS
	
	protected void notifyDataReceived(String message) {
		notifyListeners(new MarlinEvent(this,MarlinEvent.DATA_RECEIVED,message));	
	}
	
	protected void notifyLineError(int lineNumber) {
		notifyListeners(new MarlinEvent(this,MarlinEvent.TRANSPORT_ERROR,lineNumber));	
	}

	protected void notifySendBufferEmpty() {
		notifyListeners(new MarlinEvent(this,MarlinEvent.SEND_BUFFER_EMPTY,null));	
	}
}
