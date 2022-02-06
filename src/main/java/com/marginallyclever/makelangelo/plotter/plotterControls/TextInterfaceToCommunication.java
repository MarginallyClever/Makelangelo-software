package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.communications.Communication;
import com.marginallyclever.communications.CommunicationEvent;
import com.marginallyclever.communications.CommunicationListener;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link TextInterfaceToCommunication} provides a method to open and close a
 * {@link Communication} connection through a {@link ChooseConnection}
 * interface and allow two way communication through a {@link TextInterfaceWithHistory} interface. 
 * @author Dan Royer
 * @since 7.28.0
 */
public class TextInterfaceToCommunication extends JPanel implements CommunicationListener {
	private static final long serialVersionUID = 1032123255711692874L;
	private TextInterfaceWithHistory myInterface = new TextInterfaceWithHistory();
	private Communication mySession;

	public TextInterfaceToCommunication(ChooseConnection chooseConnection) {
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
				case CommunicationEvent.CONNECTION_OPENED -> setNetworkSession((Communication) e.data);
				case CommunicationEvent.CONNECTION_CLOSED -> setNetworkSession(null);
			}

			notifyListeners(e);
		});
	}

	public void setNetworkSession(Communication session) {
		if(mySession!=null) mySession.removeListener(this);
		mySession = session;
		if(mySession!=null) mySession.addListener(this);
		
		myInterface.setEnabled(mySession!=null);
	}

	public void sendCommand(String str) {
		myInterface.sendCommand(str);
	}
	
	public String getCommand() {
		return myInterface.getCommand();
	}

	public void setCommand(String str) {
		myInterface.setCommand(str);
	}
	
	@Override
	public void networkSessionEvent(CommunicationEvent evt) {
		if(evt.flag == CommunicationEvent.DATA_RECEIVED) {
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

	private List<CommunicationListener> listeners = new ArrayList<>();

	public void addListener(CommunicationListener listener) {
		listeners.add(listener);
	}

	public void removeListener(CommunicationListener listener) {
		listeners.remove(listener);
	}

	private void notifyListeners(CommunicationEvent evt) {
		for( CommunicationListener a : listeners ) {
			a.networkSessionEvent(evt);
		}
	}

	public void addNetworkSessionListener(CommunicationListener a) {
		mySession.addListener(a);
	}
	
	public void removeNetworkSessionListener(CommunicationListener a) {
		mySession.removeListener(a);
	}

	// TEST 
	
	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		
		JFrame frame = new JFrame(TextInterfaceToCommunication.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setPreferredSize(new Dimension(600, 400));
		frame.add(new TextInterfaceToCommunication(new ChooseConnection()));
		frame.pack();
		frame.setVisible(true);
	}
}
