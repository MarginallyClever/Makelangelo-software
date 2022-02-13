package com.marginallyclever.communications.tcp;

import com.marginallyclever.communications.Configuration;
import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.TransportLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Lists available TCP connections and opens a connection of that type to a robot
 *
 * @author Dan
 * @since v7.1.0.0
 */
public class TCPTransportLayer implements TransportLayer {
	private static final Logger logger = LoggerFactory.getLogger(TCPTransportLayer.class);

	public TCPTransportLayer() {}

	/**
	 * @return <code>serialConnection</code> if connection successful.  <code>null</code> on failure.
	 */
	public NetworkSession openConnection(Configuration configuration) {
		/*
		// check it
		Log.message("Validating "+connectionName);
		InetAddressValidator validator = new InetAddressValidator();
		if(!validator.isValid(connectionName)) {
			Log.error("Not a valid IP Address.");
			return null;
		}
		*/
		logger.info("Connecting to {}", configuration.getConnectionName());
		TCPConnection connection = new TCPConnection();

		try {
			connection.openConnection(configuration.getConnectionName());
			logger.info("Connect OK");
		} catch (Exception e) {
			logger.error("Connection FAILED to {}", connection, e);
			return null;
		}

		return connection;
	}

	@Override
	public List<String> listConnections() {
		logger.debug("Listing available tcp connection");
		return List.of("192.168.1.183:9999");
	}
}
