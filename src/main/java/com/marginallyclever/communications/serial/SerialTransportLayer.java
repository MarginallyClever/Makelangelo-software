package com.marginallyclever.communications.serial;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.TransportLayer;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import jssc.SerialPortList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Lists available serial connections and opens a connection of that type
 *
 * @author Dan
 * @since v7.1.0.0
 */
public class SerialTransportLayer implements TransportLayer {

	private static final Logger logger = LoggerFactory.getLogger(SerialTransportLayer.class);

	public SerialTransportLayer() {}

	public String getName() {
		return "USB Serial";
	}
	
	/**
	 * find all available serial ports.
	 * @return a list of port names
	 */
	public List<String> listConnections() {
		String [] portsDetected;

		String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		if ((os.contains("mac")) || (os.contains("darwin"))) {
			// Also list Bluetooth serial connections
			portsDetected = SerialPortList.getPortNames(Pattern.compile("cu"));

			Arrays.sort(portsDetected, (o1, o2) -> {
				// Bluetooth-Incoming-Port is not useful, so put it at the end of the list
				if (o2.contains("Bluetooth-Incoming-Port")) {
					return -1;
				} else if (o1.contains("Bluetooth-Incoming-Port")) {
					return 1;
				}
				return o1.compareTo(o2);
			});
		} else {
			portsDetected = SerialPortList.getPortNames();
		}

		return Arrays.asList(portsDetected);
	}

	/**
	 * @return {@code NetworkSession} if connection successful.  return null on failure.
	 */
	@Override
	public NetworkSession openConnection(String connectionName) {
		SerialConnection serialConnection = new SerialConnection();

		try {
			serialConnection.openConnection(connectionName);
		} catch (Exception e) {
			logger.error("Failed to open the serial {}; Ignoring", connectionName, e);
			return null;
		}

		return serialConnection;
	}
}
