package com.marginallyclever.communications.serial;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.TransportLayer;
import com.marginallyclever.communications.TransportLayerPanel;
import com.marginallyclever.convenience.log.Log;

import java.util.regex.Pattern;
import jssc.SerialPortList;


/**
 * Lists available serial connections and opens a connection of that type
 *
 * @author Dan
 * @since v7.1.0.0
 */
public class SerialTransportLayer implements TransportLayer {
	public SerialTransportLayer() {}

	public String getName() {
		return "USB Serial";
	}
	
	/**
	 * find all available serial ports.
	 * @return a list of port names
	 */
	public static String[] listConnections() {
		String OS = System.getProperty("os.name").toLowerCase();

		String [] portsDetected;
		if (OS.indexOf("mac") >= 0) {
			// Also list Bluetooth serial connections
			portsDetected = SerialPortList.getPortNames(Pattern.compile("cu"));
		} else {
			portsDetected = SerialPortList.getPortNames();
		}

		/*
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.indexOf("mac") >= 0) {
			SerialPortList.
			"/dev/");
			//Log.message("OS X");
		} else if (OS.indexOf("win") >= 0) {
			portsDetected = SerialPortList.getPortNames("COM");
			//Log.message("Windows");
		} else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
			portsDetected = SerialPortList.getPortNames("/dev/");
			//Log.message("Linux/Unix");
		} else {
			Log.message("OS ERROR");
			Log.message("OS NAME=" + System.getProperty("os.name"));
		}*/
		
		return portsDetected;
	}

	/**
	 * @return {@code NetworkSession} if connection successful.  return null on failure.
	 */
	@Override
	public NetworkSession openConnection(String connectionName) {
		//if(connectionName.equals(recentPort)) return null;

		SerialConnection serialConnection = new SerialConnection();

		try {
			serialConnection.openConnection(connectionName);
		} catch (Exception e) {
			return null;
		}

		return serialConnection;
	}

	/**
	 * @return a panel with the gui options for this transport layer
	 */
	public TransportLayerPanel getTransportLayerPanel() {
		return new SerialTransportLayerPanel(this);
	}
	
	public static void main(String[] args) {
		Log.start();
		Log.message("connections:");
		String [] list = SerialTransportLayer.listConnections();
		for(String s : list ) {
			Log.message(s);
		}
		Log.end();
	}
}
