package com.marginallyclever.communications.serial;

import com.marginallyclever.communications.Configuration;
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
	public static final String CU_USBSERIAL = "cu.usbserial";

	public SerialTransportLayer() {}

	/**
	 * find all available serial ports.
	 * @return a list of port names
	 */
	public List<String> listConnections() {
		logger.debug("Listing available serial port");
		String[] portsDetected;

		String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

		if ((os.contains("mac")) || (os.contains("darwin"))) {
			portsDetected = SerialPortList.getPortNames(Pattern.compile("(ttys[0-9]{1,3}|tty\\..*|cu\\..*)"));

			// Also list Bluetooth serial connections
			Arrays.sort(portsDetected, (o1, o2) -> {
				// cu.usbserial* are most used, so put it on the top of the list
				if (o1.contains(CU_USBSERIAL) && o2.contains(CU_USBSERIAL)) {
					return o1.compareTo(o2);
				}
				if (o2.contains(CU_USBSERIAL)) {
					return 1;
				} else if (o1.contains(CU_USBSERIAL)) {
					return -1;
				}
				return o1.compareTo(o2);
			});
		} else  {
			portsDetected = SerialPortList.getPortNames();
		}

		List<String> connections = Arrays.asList(portsDetected);

		if (logger.isDebugEnabled()) {
			connections.forEach(connection -> logger.debug("  {}", connection));
		}

		return connections;
	}

	/**
	 * @return {@code NetworkSession} if connection successful.  return null on failure.
	 */
	@Override
	public NetworkSession openConnection(Configuration configuration) {
		SerialConnection serialConnection = new SerialConnection();

		try {
			String connectionName = configuration.getConnectionName();
			if (configuration.getConfigurations().containsKey("speed")) {
				serialConnection.openConnection(connectionName, (int) configuration.getConfigurations().get("speed"));
			} else {
				serialConnection.openConnection(connectionName);
			}
		} catch (Exception e) {
			logger.error("Failed to open the serial {}; Ignoring", configuration.getConnectionName(), e);
			return null;
		}

		return serialConnection;
	}
}
