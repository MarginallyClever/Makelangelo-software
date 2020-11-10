package com.marginallyclever.communications.tcp;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.TransportLayer;
import com.marginallyclever.communications.TransportLayerPanel;
import com.marginallyclever.convenience.log.Log;

/**
 * Lists available TCP connections and opens a connection of that type to a robot
 *
 * @author Dan
 * @since v7.1.0.0
 */
public class TCPTransportLayer implements TransportLayer {
	public TCPTransportLayer() {}

	public String getName() {
		return "TCP/IP";
	}

	/**
	 * @return <code>serialConnection</code> if connection successful.  <code>null</code> on failure.
	 */
	public NetworkConnection openConnection(String connectionName) {
		/*
		// check it
		Log.message("Validating "+connectionName);
		InetAddressValidator validator = new InetAddressValidator();
		if(!validator.isValid(connectionName)) {
			Log.error("Not a valid IP Address.");
			return null;
		}
		*/
		Log.message("Connecting to "+connectionName);
		//if(connectionName.equals(recentPort)) return null;
		TCPConnection connection = new TCPConnection(this);

		try {
			connection.openConnection(connectionName);
			Log.message("Connect OK");
		} catch (Exception e) {
			Log.message("Connect FAILED");
			e.printStackTrace();
			return null;
		}

		return connection;
	}

	@Override
	public TransportLayerPanel getTransportLayerPanel() {
		return new TCPTransportLayerPanel(this);
	}
}
