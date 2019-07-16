package com.marginallyclever.communications.tcp;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.TransportLayerPanel;

public class TCPTransportLayerPanel extends TransportLayerPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5157947915933861665L;
	private TCPTransportLayer layer;
	private JTextField connectionField;
	private JTextField portField;
	private static String portNumber = "9999";
	private static String connectionName = "192.168.1.183";
	
	TCPTransportLayerPanel(TCPTransportLayer tcpLayer) {
		this.layer=tcpLayer;
		
		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel("IP (a.b.c.d)"));  // TODO translate me?
		this.add(connectionField = new JTextField());
		this.add(new JLabel("Port"));  // TODO translate me?
		this.add(portField = new JTextField());
		
		connectionField.setText(connectionName);
		portField.setText(portNumber);
	}
	
	public NetworkConnection openConnection() {
		connectionName = connectionField.getText();
		portNumber = portField.getText();
		return layer.openConnection(connectionField.getText()+":"+portNumber);
	}
}
