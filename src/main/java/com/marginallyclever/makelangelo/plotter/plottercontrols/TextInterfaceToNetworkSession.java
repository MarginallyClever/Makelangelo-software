package com.marginallyclever.makelangelo.plotter.plottercontrols;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link TextInterfaceToNetworkSession} provides a method to open and close a 
 * {@link NetworkSession} connection through a {@link ChooseConnection}
 * interface and allow two way communication through a {@link TextInterfaceWithHistory} interface. 
 * @author Dan Royer
 * @since 7.28.0
 */
public class TextInterfaceToNetworkSession extends JPanel implements NetworkSessionListener {
	private final TextInterfaceWithHistory myInterface = new TextInterfaceWithHistory();
	private NetworkSession mySession;

	public TextInterfaceToNetworkSession(ChooseConnection chooseConnection) {
		super();

		setLayout(new BorderLayout());
		
		add(myInterface,BorderLayout.CENTER);
		
		myInterface.setEnabled(false);
		myInterface.addActionListener( (evt) -> {
			if(mySession==null) return;
			
			String str = evt.getActionCommand();
			if(!str.endsWith("\n")) str+="\n";
			
			try {
				mySession.sendMessage(str);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this,e1.getLocalizedMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			}
		});
		chooseConnection.addListener((e)->{
			switch (e.flag) {
				case NetworkSessionEvent.CONNECTION_OPENED -> setNetworkSession((NetworkSession) e.data);
				case NetworkSessionEvent.CONNECTION_CLOSED -> setNetworkSession(null);
			}

			notifyListeners(e);
		});
	}

	public void setNetworkSession(NetworkSession session) {
		if(mySession!=null) mySession.removeListener(this);
		mySession = session;
		if(mySession!=null) mySession.addListener(this);
		
		myInterface.setEnabled(mySession!=null);
	}

	public void sendCommand(String str) {
		myInterface.sendCommand(str);
	}

	public void displayError(String str) {
		myInterface.addToHistory("Error", str);
	}
	
	public String getCommand() {
		return myInterface.getCommand();
	}

	public void setCommand(String str) {
		myInterface.setCommand(str);
	}
	
	@Override
	public void networkSessionEvent(NetworkSessionEvent evt) {
		if(evt.flag == NetworkSessionEvent.DATA_RECEIVED) {
			myInterface.addToHistory(mySession.getName(),((String)evt.data).trim());
		}
	}

	public boolean getIsConnected() { 
		return (mySession!=null && mySession.isOpen());
	}

	public void closeConnection() {
		if (mySession != null) {
			mySession.closeConnection();
		}
	}

	// OBSERVER PATTERN

	private final List<NetworkSessionListener> listeners = new ArrayList<>();

	public void addListener(NetworkSessionListener listener) {
		listeners.add(listener);
	}

	public void removeListener(NetworkSessionListener listener) {
		listeners.remove(listener);
	}

	private void notifyListeners(NetworkSessionEvent evt) {
		for( NetworkSessionListener a : listeners ) {
			a.networkSessionEvent(evt);
		}
	}

	public void addNetworkSessionListener(NetworkSessionListener a) {
		mySession.addListener(a);
	}
	
	public void removeNetworkSessionListener(NetworkSessionListener a) {
		mySession.removeListener(a);
	}

}
