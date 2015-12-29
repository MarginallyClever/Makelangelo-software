package com.marginallyclever.communications;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.util.ArrayList;

import com.marginallyclever.communications.MarginallyCleverConnectionReadyListener;


/**
 * Created on 4/12/15.  Encapsulate all jssc serial receive/transmit implementation
 *
 * @author Peter Colapietro
 * @since v7
 */
public final class SerialConnection implements SerialPortEventListener, MarginallyCleverConnection {
	private SerialPort serialPort;
	private static final int BAUD_RATE = 57600;

	private String connectionName = "";
	private boolean portOpened = false;
	private boolean waitingForCue = false;


	static final String CUE = "> ";
	static final String NOCHECKSUM = "NOCHECKSUM ";
	static final String BADCHECKSUM = "BADCHECKSUM ";
	static final String BADLINENUM = "BADLINENUM ";
	static final String NEWLINE = "\n";
	static final String COMMENT_START = ";";

	// parsing input from Makelangelo
	private String inputBuffer = "";
    ArrayList<String> commandQueue = new ArrayList<String>();

    // Listeners which should be notified of a change to the percentage.
    private ArrayList<MarginallyCleverConnectionReadyListener> listeners = new ArrayList<MarginallyCleverConnectionReadyListener>();


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

		connectionName = portName;
		portOpened = true;
		waitingForCue = true;
		
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
			} catch (Exception e) {
			}

			return err;
		}
		if (line.lastIndexOf(BADCHECKSUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADCHECKSUM) + BADCHECKSUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
			} catch (Exception e) {
			}

			return err;
		}
		if (line.lastIndexOf(BADLINENUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADLINENUM) + BADLINENUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
			} catch (Exception e) {
			}

			return err;
		}

		return -1;
	}


	// Deal with something robot has sent.
	@Override
	public void serialEvent(SerialPortEvent events) {
		String rawInput, oneLine;
		int x;
		
        if(events.isRXCHAR()) {
        	if(!portOpened) return;
            try {
            	int len = events.getEventValue();
				byte [] buffer = serialPort.readBytes(len);
				if( len>0 ) {
					rawInput = new String(buffer,0,len);
					inputBuffer+=rawInput;
					// each line ends with a \n.
					for( x=inputBuffer.indexOf("\n"); x!=-1; x=inputBuffer.indexOf("\n") ) {
						x=x+1;
						oneLine = inputBuffer.substring(0,x);
						inputBuffer = inputBuffer.substring(x);
						// wait for the cue to send another command
						if(oneLine.indexOf(CUE)==0) {
							if(waitingForCue) {
								notifyDataAvailable(oneLine);
							}
							waitingForCue=false;
						} else {
							int error_line = errorReported(oneLine);
		                    if(error_line != -1) {
		                    	notifyLineError(error_line);
		                    }
		                    notifyDataAvailable(oneLine);
						}
					}
					if(waitingForCue==false) {
						sendQueuedCommand();
					}
				}
            } catch (SerialPortException e) {}
        }
	}


	protected void sendQueuedCommand() {
		if(!portOpened || waitingForCue) return;
		
		if(commandQueue.size()==0) {
		      notifyConnectionReady();
		      return;
		}
		
		String command;
		try {
			command=commandQueue.remove(0);
			String line = command;
			if(line.contains(COMMENT_START)) {
				String [] lines = line.split(COMMENT_START);
				command = lines[0];
			}
			if(line.endsWith("\n") == false) {
				line+=NEWLINE;
			}
			serialPort.writeBytes(line.getBytes());
			waitingForCue=true;
		}
		catch(IndexOutOfBoundsException e1) {}
		catch(SerialPortException e2) {}
	}
	
	public void deleteAllQueuedCommands() {
		commandQueue.clear();
	}
	
	// connect to the last port
	@Override
	public void reconnect() throws Exception {
		openConnection(connectionName);
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
		for (int i = 0; i < length; i++) {
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

	@Override
	public String getRecentConnection() {
		return connectionName;
	}

	@Override
	public void addListener(MarginallyCleverConnectionReadyListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(MarginallyCleverConnectionReadyListener listener) {
		listeners.remove(listener);
	}

	private void notifyLineError(int lineNumber) {
	      for (MarginallyCleverConnectionReadyListener listener : listeners) {
	          listener.lineError(this,lineNumber);
	        }
	}
	
    private void notifyConnectionReady() {
      for (MarginallyCleverConnectionReadyListener listener : listeners) {
        listener.connectionReady(this);
      }
    }
	
	// tell all listeners data has arrived
	private void notifyDataAvailable(String line) {
	      for (MarginallyCleverConnectionReadyListener listener : listeners) {
	        listener.dataAvailable(this,line);
	      }
	}
}
