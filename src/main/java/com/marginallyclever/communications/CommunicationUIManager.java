package com.marginallyclever.communications;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * handles requests between the UI and the various transport layers 
 * @author dan royer
 *
 */
public class CommunicationUIManager {

	private static final Logger logger = LoggerFactory.getLogger(CommunicationUIManager.class);
	
	private static List<TransportLayer> transportLayers = new ArrayList<>( Arrays.asList(
			new SerialTransportLayer()
			//new TCPTransportLayer()
			));

	public static List<CommunicationItem> getConnectionsItems() {
		logger.debug("Fetching connections");
		List<CommunicationItem> items = new ArrayList<>();
		for (TransportLayer transportLayer : transportLayers) {
			logger.debug("  {}" ,transportLayer.getName());
			for (String connection: transportLayer.listConnections()) {
				logger.debug("    {}", connection);
				CommunicationItem nsi = new CommunicationItem(transportLayer, connection);
				items.add(nsi);
			}
		}
		return items;
	}
}
