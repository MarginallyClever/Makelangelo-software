package com.marginallyclever.communications.serial;

import com.marginallyclever.communications.NetworkSession;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encapsulate all serial receive/transmit implementation
 *
 * @author Peter Colapietro
 * @since v7 (4/12/15)
 */
public final class SerialConnection extends NetworkSession implements SerialPortEventListener {
	public static final int BAUD_RATE = 250000;
	private static final Logger logger = LoggerFactory.getLogger(SerialConnection.class);

	private SerialPort serialPort;
	private boolean portOpened = false;
	private String inputBuffer = "";

	public SerialConnection() {}

	@Override
	public void closeConnection() {
		if (portOpened) {
			if (serialPort != null) {
				try {
					serialPort.removeEventListener();
					serialPort.closePort();
				} catch (SerialPortException e) {
					logger.error("Close failed", e);
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
			notifyDataReceived(oneLine);
		}
	}

	@Override
	public void sendMessage(String msg) throws Exception {
		if(!portOpened) return;
		if(msg==null || msg.length()==0) return;
		
		serialPort.writeBytes(msg.getBytes());
		notifyDataSent(msg);
	}

	/**
	 * @return the port open for this serial connection.
	 */
	@Override
	public boolean isOpen() {
		return portOpened;
	}
}
