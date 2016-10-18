package com.marginallyclever.communications.tcp;

import java.awt.GridLayout;

import javax.swing.JTextField;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.TransportLayerPanel;

public class TCPTransportLayerPanel extends TransportLayerPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5157947915933861665L;
	private TCPTransportLayer layer;
	private JTextField connectionName;
	
	TCPTransportLayerPanel(TCPTransportLayer tcpLayer) {
		this.layer=tcpLayer;
		
		this.setLayout(new GridLayout(0, 1));
		this.add(connectionName = new JTextField());
	}
	
	public NetworkConnection openConnection() {
		return layer.openConnection(connectionName.getText());
	}
}
