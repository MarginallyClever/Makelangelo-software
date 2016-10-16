package com.marginallyclever.communications;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.Translator;

import jssc.SerialPortList;


/**
 * Lists available serial connections and opens a connection of that type
 *
 * @author Dan
 * @since v7.1.0.0
 */
public class SerialTransportLayer implements TransportLayer {
	private String[] portsDetected;

	public SerialTransportLayer() {}

	/**
	 * find all available serial ports
	 *
	 * @return a list of port names
	 */
	private String[] listConnections() {
		String OS = System.getProperty("os.name").toLowerCase();

		if (OS.indexOf("mac") >= 0) {
			portsDetected = SerialPortList.getPortNames("/dev/");
			//System.out.println("OS X");
		} else if (OS.indexOf("win") >= 0) {
			portsDetected = SerialPortList.getPortNames("COM");
			//System.out.println("Windows");
		} else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
			portsDetected = SerialPortList.getPortNames("/dev/");
			//System.out.println("Linux/Unix");
		} else {
			System.out.println("OS ERROR");
			System.out.println("OS NAME=" + System.getProperty("os.name"));
		}
		return portsDetected;
	}

	/**
	 * @return <code>serialConnection</code> if connection successful.  <code>null</code> on failure.
	 */
	public NetworkConnection openConnection(String connectionName) {
		//if(connectionName.equals(recentPort)) return null;

		SerialConnection serialConnection = new SerialConnection(this);

		try {
			serialConnection.openConnection(connectionName);
		} catch (Exception e) {
			return null;
		}

		return serialConnection;
	}

	  /**
	   * Display a list of available serial connections.  Let the user choose one.
	   * @return return an open connection to the selected device, or null.
	   */
	  public NetworkConnection requestNewConnection(Component parent) {
			JPanel connectionList = new JPanel(new GridLayout(0, 1));
			connectionList.add(new JLabel(Translator.get("MenuConnect")));
			
			GridBagConstraints con1 = new GridBagConstraints();
			con1.gridx=0;
			con1.gridy=0;
			con1.weightx=1;
			con1.weighty=1;
			con1.fill=GridBagConstraints.HORIZONTAL;
			con1.anchor=GridBagConstraints.NORTH;

			JComboBox<String> connectionComboBox = new JComboBox<String>();
	        connectionList.removeAll();
	        connectionList.add(connectionComboBox);
		    
	        String [] portsDetected = listConnections();
			int i;
		    for(i=0;i<portsDetected.length;++i) {
		    	connectionComboBox.addItem(portsDetected[i]);
		    	connectionComboBox.setSelectedIndex(i+1);
		    }
	        
			int result = JOptionPane.showConfirmDialog(parent, connectionList, Translator.get("MenuConnect"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				return openConnection(connectionComboBox.getItemAt(connectionComboBox.getSelectedIndex()));
			}
			return null;
	  }
}
