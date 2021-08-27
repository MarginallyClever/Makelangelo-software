package com.marginallyclever.communications;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import com.marginallyclever.makelangelo.Translator;

/**
 * handles requests between the UI and the various transport layers 
 * @author dan royer
 *
 */
public class ConnectionManager {
	private static ArrayList<TransportLayer> transportLayers = new ArrayList<TransportLayer>( Arrays.asList(
			new SerialTransportLayer()
			//, transportLayers.add(new TCPTransportLayer()
			));
	
	/**
	 * create a GUI to give the user transport layer options.
	 * @param parent the root gui component
	 * @return a new connection or null.
	 */
	public static NetworkConnection requestNewConnection(Component parent) {
		JPanel top = new JPanel();
		top.setLayout(new GridLayout(0,1));
		JTabbedPane tabs = new JTabbedPane();
		top.add(tabs);
		for( TransportLayer t : transportLayers ) {
			tabs.addTab(t.getName(), t.getTransportLayerPanel());
		}

		int result = JOptionPane.showConfirmDialog(parent, top, Translator.get("MenuConnect"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			Component c = tabs.getSelectedComponent();
			if(c instanceof TransportLayerPanel) {
				return ((TransportLayerPanel)c).openConnection();
			}
		}
		// cancelled connect
		return null;
	}
}
