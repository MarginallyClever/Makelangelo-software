package com.marginallyclever.communications;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import com.marginallyclever.makelangelo.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * handles requests between the UI and the various transport layers 
 * @author dan royer
 *
 */
public class NetworkSessionUIManager {

	private static final Logger logger = LoggerFactory.getLogger(NetworkSessionUIManager.class);
	
	private static List<TransportLayer> transportLayers = new ArrayList<>( Arrays.asList(
			new SerialTransportLayer()
			//new TCPTransportLayer()
			));


	public static List<NetworkSessionItem> getConnectionsItems() {
		List<NetworkSessionItem> items = new ArrayList<>();
		for (TransportLayer transportLayer : transportLayers) {
			logger.debug("  {}" ,transportLayer.getName());
			for (String connection: transportLayer.listConnections()) {
				NetworkSessionItem nsi = new NetworkSessionItem(transportLayer, connection);
				items.add(nsi);
			}
		}
		return items;
	}

	/**
	 * create a GUI to give the user transport layer options.
	 * @param parent the root gui component
	 * @return a new connection or null.
	 */
	public static NetworkSession requestNewSession(NetworkSessionItem networkSessionItem) {
		logger.debug("requestNewSession {}", networkSessionItem);
		return networkSessionItem.getTransportLayer().openConnection(networkSessionItem.getConnectionName());
	}
}
