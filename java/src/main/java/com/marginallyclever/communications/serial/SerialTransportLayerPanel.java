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
	
	public SerialTransportLayerPanel(SerialTransportLayer serialTransportLayer) {
		this.layer = serialTransportLayer;
		
		this.setLayout(new GridLayout(0, 1));
		add(connectionComboBox = new JComboBox<String>());
	    
	    String [] portsDetected = layer.listConnections();
		int i;
	    for(i=0;i<portsDetected.length;++i) {
	    	connectionComboBox.addItem(portsDetected[i]);
	    }
    	//connectionComboBox.setSelectedIndex(i+1);
	}

	@Override
	public NetworkConnection openConnection() {
		return layer.openConnection(connectionComboBox.getItemAt(connectionComboBox.getSelectedIndex()));
	}
}
