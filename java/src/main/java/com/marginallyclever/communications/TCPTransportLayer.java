package com.marginallyclever.communications;

import java.awt.Component;
import org.apache.commons.validator.routines.InetAddressValidator;

import com.marginallyclever.makelangelo.Log;

/**
 * Lists available TCP connections and opens a connection of that type to a robot
 *
 * @author Dan
 * @since v7.1.0.0
 */
public class TCPTransportLayer implements TransportLayer {
	public TCPTransportLayer() {}

	/**
	 * @return <code>serialConnection</code> if connection successful.  <code>null</code> on failure.
	 */
	public NetworkConnection openConnection(String connectionName) {
		// check it
		InetAddressValidator validator = new InetAddressValidator();
		if(!validator.isValid(connectionName)) {
			Log.error("Not a valid IP Address.");
			return null;
		}
		//if(connectionName.equals(recentPort)) return null;
		TCPConnection connection = new TCPConnection(this);

		try {
			connection.openConnection(connectionName);
		} catch (Exception e) {
			return null;
		}

		return connection;
	}

	@Override
	public NetworkConnection requestNewConnection(Component parent) {
		// TODO Auto-generated method stub
		//new Socket("192.168.4.1", 9999);
		String inetAddress=null;
		
		// TODO request an address from the user


		// connect to it 
		return openConnection(inetAddress);
	}
}
