package com.marginallyclever.communications.tcp;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.TransportLayer;
import com.marginallyclever.communications.TransportLayerPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists available TCP connections and opens a connection of that type to a robot
 *
 * @author Dan
 * @since v7.1.0.0
 */
public class TCPTransportLayer implements TransportLayer {
	private static final Logger logger = LoggerFactory.getLogger(TCPTransportLayer.class);

	public TCPTransportLayer() {}

	public String getName() {
		return "TCP/IP";
	}

	/**
	 * @return <code>serialConnection</code> if connection successful.  <code>null</code> on failure.
	 */
	public NetworkSession openConnection(String connectionName) {
		/*
		// check it
		Log.message("Validating "+connectionName);
		InetAddressValidator validator = new InetAddressValidator();
		if(!validator.isValid(connectionName)) {
			Log.error("Not a valid IP Address.");
			return null;
		}
		*/
		logger.info("Connecting to {}", connectionName);
		TCPConnection connection = new TCPConnection();

		try {
			connection.openConnection(connectionName);
			logger.info("Connect OK");
		} catch (Exception e) {
			logger.error("Connection FAILED to {}", connection, e);
			return null;
		}

		return connection;
	}

	@Override
	public TransportLayerPanel getTransportLayerPanel() {
		return new TCPTransportLayerPanel(this);
	}
}
