package com.marginallyclever.communications.serial;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.util.ArrayList;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.convenience.log.Log;


/**
 * Encapsulate all serial receive/transmit implementation
 *
 * @author Peter Colapietro
 * @since v7 (4/12/15)
 */
public final class SerialConnection extends NetworkSession implements SerialPortEventListener {
	public static final int BAUD_RATE = 250000;
	public static final String CUE = "ok";
	public static final String NOCHECKSUM = "NOCHECKSUM ";
	public static final String BADCHECKSUM = "BADCHECKSUM ";
	public static final String BADLINENUM = "BADLINENUM ";
	public static final String NEWLINE = "\n";
	public static final String COMMENT_START = ";";

	private SerialPort serialPort;
	private boolean portOpened = false;
	private boolean waitingForCue = false;

	// parsing input from Makelangelo
	private String inputBuffer = "";
	private ArrayList<String> commandQueue = new ArrayList<String>();


	public SerialConnection() {}

	@Override
	public void sendMessage(String msg) throws Exception {
		commandQueue.add(msg);
		sendQueuedCommand();
	}


	@Override
	public void closeConnection() {
		if (portOpened) {
			if (serialPort != null) {
				try {
					serialPort.removeEventListener();
					serialPort.closePort();
				} catch (SerialPortException e) {
					Log.error(e.getLocalizedMessage());
				}
			}
			portOpened = false;
		}
	}

	// open a serial connection to a device.  We won't know it's the robot until
	@Override
	public void openConnection(String portName) throws Exception {
		if (portOpened) return;

		closeConnection();
		
		// open the port
		serialPort = new SerialPort(portName);
		serialPort.openPort();// Open serial port
		serialPort.setParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		serialPort.addEventListener(this);

		setName(portName);
		portOpened = true;
		waitingForCue = false;
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

	// Deal with something robot has sent.
	@Override
	public void serialEvent(SerialPortEvent events) {
		String rawInput, oneLine;
		int x;

		if(!events.isRXCHAR()) return;
		if(!portOpened) return;
		int len =0 ;
		byte [] buffer;
		try {
			len = events.getEventValue();
			buffer = serialPort.readBytes(len);
		} catch (SerialPortException e) {
			// uh oh
			return;
		}
		
		if( len<=0 ) return;
		rawInput = new String(buffer,0,len);
		inputBuffer+=rawInput;
		// each line ends with a \n.
		for( x=inputBuffer.indexOf("\n"); x!=-1; x=inputBuffer.indexOf("\n") ) {
			x=x+1;
			oneLine = inputBuffer.substring(0,x);
			inputBuffer = inputBuffer.substring(x);

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
			}
		}
		if(waitingForCue==false) {
			sendQueuedCommand();
		}
	}

	protected void sendQueuedCommand() {
		if(!portOpened || waitingForCue) return;

		if(commandQueue.isEmpty()==true) {
			notifySendBufferEmpty();
			return;
		}

		try {
			String command=commandQueue.remove(0);
			if(command==null || command.length()==0) return;
			/*
			// remove any comments in the gcode
			// TODO don't put this in serialConnection, it's the wrong level of abstraction.
			if(command.contains(COMMENT_START)) {
				command = command.substring(0,line.indexOf(COMMENT_START));
			}*/
			if(!command.endsWith("\n")) command+=NEWLINE;

			serialPort.writeBytes(command.getBytes());
			
			waitingForCue=true;

			notifyDataSent(command);
		}
		catch(Exception e1) {
			Log.error(e1.getLocalizedMessage());
		}
	}

	public void deleteAllQueuedCommands() {
		commandQueue.clear();
	}

	/**
	 * Java string to int is very picky.  this method is slightly less picky.  Only works with positive whole numbers.
	 *
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

	/**
	 * @return the port open for this serial connection.
	 */
	@Override
	public boolean isOpen() {
		return portOpened;
	}
}
