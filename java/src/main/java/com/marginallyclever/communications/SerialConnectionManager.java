package com.marginallyclever.communications;

import jssc.SerialPortList;


/**
 * Lists available serial connections and opens a connection of that type
 *
 * @author Dan
 * @since v7.1.0.0
 */
public class SerialConnectionManager implements MarginallyCleverConnectionManager {
	private String[] portsDetected;

	public SerialConnectionManager() {}

	/**
	 * find all available serial ports
	 *
	 * @return a list of port names
	 */
	@Override
	public String[] listConnections() {
		String OS = System.getProperty("os.name").toLowerCase();

		if (OS.indexOf("mac") >= 0) {
			portsDetected = SerialPortList.getPortNames("/dev/");
			//System.out.println("OS X");
		} else if (OS.indexOf("win") >= 0) {
			portsDetected = SerialPortList.getPortNames("COM");
			//System.out.println("Windows");
		} else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
			portsDetected = SerialPortList.getPortNames("/dev/");
			//System.out.println("Linux/Unix");
		} else {
			System.out.println("OS ERROR");
			System.out.println("OS NAME=" + System.getProperty("os.name"));
		}
		return portsDetected;
	}

	/**
	 * @return <code>serialConnection</code> if connection successful.  <code>null</code> on failure.
	 */
	public MarginallyCleverConnection openConnection(String connectionName) {
		//if(connectionName.equals(recentPort)) return null;

		SerialConnection serialConnection = new SerialConnection();
		serialConnection.setManager(this);

		try {
			serialConnection.openConnection(connectionName);
		} catch (Exception e) {
			return null;
		}

		return serialConnection;
	}
}
