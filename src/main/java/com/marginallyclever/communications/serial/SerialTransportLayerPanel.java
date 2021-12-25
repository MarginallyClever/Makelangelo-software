package com.marginallyclever.communications.serial;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.TransportLayerPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class SerialTransportLayerPanel extends TransportLayerPanel {
	private static final Logger logger = LoggerFactory.getLogger(SerialTransportLayerPanel.class);
	private static final long serialVersionUID = -5048852192781164326L;
	private SerialTransportLayer layer;
	private JComboBox<String> connectionComboBox;
	private static String favorite="";
	
	public SerialTransportLayerPanel(SerialTransportLayer serialTransportLayer) {
		super();
		
		this.layer = serialTransportLayer;

		logger.debug("SerialTransportLayerPanel start");
		this.setLayout(new GridLayout(0, 1));
		add(connectionComboBox = new JComboBox<String>());

	    String [] portsDetected = SerialTransportLayer.listConnections();
		int i;
	    for(i=0;i<portsDetected.length;++i) {
			logger.debug("  found {} ", portsDetected[i]);
	    	connectionComboBox.addItem(portsDetected[i]);
	    }
		connectionComboBox.setSelectedItem(favorite);
		logger.debug("SerialTransportLayerPanel ready");
	}

	@Override
	public NetworkSession openConnection() {
		favorite = connectionComboBox.getItemAt(connectionComboBox.getSelectedIndex());
		return layer.openConnection(favorite);
	}
}
