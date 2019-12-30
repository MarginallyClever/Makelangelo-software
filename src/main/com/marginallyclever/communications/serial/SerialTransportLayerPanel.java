package com.marginallyclever.communications.serial;

import java.awt.GridLayout;

import javax.swing.JComboBox;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.TransportLayerPanel;

public class SerialTransportLayerPanel extends TransportLayerPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5048852192781164326L;
	private SerialTransportLayer layer;
	private JComboBox<String> connectionComboBox;
	private static String favorite="";
	
	public SerialTransportLayerPanel(SerialTransportLayer serialTransportLayer) {
		this.layer = serialTransportLayer;
		
		this.setLayout(new GridLayout(0, 1));
		add(connectionComboBox = new JComboBox<String>());
	    
	    String [] portsDetected = layer.listConnections();
		int i;
	    for(i=0;i<portsDetected.length;++i) {
	    	connectionComboBox.addItem(portsDetected[i]);
	    }
		connectionComboBox.setSelectedItem(favorite);
	}

	@Override
	public NetworkConnection openConnection() {
		favorite = connectionComboBox.getItemAt(connectionComboBox.getSelectedIndex());
		return layer.openConnection(favorite);
	}
}
