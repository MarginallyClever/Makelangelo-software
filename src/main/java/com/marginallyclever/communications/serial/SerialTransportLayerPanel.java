package com.marginallyclever.communications.serial;

import java.awt.GridLayout;

import javax.swing.JComboBox;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.TransportLayerPanel;
import com.marginallyclever.convenience.log.Log;

public class SerialTransportLayerPanel extends TransportLayerPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5048852192781164326L;
	private SerialTransportLayer layer;
	private JComboBox<String> connectionComboBox;
	private static String favorite="";
	
	public SerialTransportLayerPanel(SerialTransportLayer serialTransportLayer) {
		super();
		
		this.layer = serialTransportLayer;

		Log.message("SerialTransportLayerPanel start");
		this.setLayout(new GridLayout(0, 1));
		add(connectionComboBox = new JComboBox<String>());

	    String [] portsDetected = SerialTransportLayer.listConnections();
		int i;
	    for(i=0;i<portsDetected.length;++i) {
			Log.message("  found: "+portsDetected[i]);
	    	connectionComboBox.addItem(portsDetected[i]);
	    }
		connectionComboBox.setSelectedItem(favorite);
		Log.message("SerialTransportLayerPanel ready");
	}

	@Override
	public NetworkSession openConnection() {
		favorite = connectionComboBox.getItemAt(connectionComboBox.getSelectedIndex());
		return layer.openConnection(favorite);
	}
}
