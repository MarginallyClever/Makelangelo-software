package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionItem;
import com.marginallyclever.communications.NetworkSessionUIManager;
import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.makelangelo.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ConnectionButton extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionButton.class);
	
	private static final long serialVersionUID = 4773092967249064165L;
	public static final int CONNECTION_OPENED = 1;
	public static final int CONNECTION_CLOSED = 2;
	
	private ButtonIcon bConnect = new ButtonIcon("ButtonConnect", "/images/connect.png");
	private NetworkSession mySession;
	private final JComboBox<NetworkSessionItem> connectionComboBox;
	
	public ConnectionButton(JComboBox<NetworkSessionItem> connectionComboBox) {
		super();

		this.connectionComboBox = connectionComboBox;

		bConnect.setForeground(Color.GREEN);
		bConnect.addActionListener((e)-> onConnectAction() );
		
		this.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.add(bConnect);
	}

	private void onConnectAction() {
		logger.debug("onConnectAction()");
		if (mySession != null) {
			onClose();
		} else {
			NetworkSession s = NetworkSessionUIManager.requestNewSession(connectionComboBox.getItemAt(connectionComboBox.getSelectedIndex()));
			if (s!=null) {
				onOpen(s);
				notifyListeners(new ActionEvent(this, ConnectionButton.CONNECTION_OPENED,""));
			}
		}
	}

	private void onClose() {
		logger.debug("closed");
		if (mySession != null) {
			mySession.closeConnection();
			mySession = null;
			notifyListeners(new ActionEvent(this, ConnectionButton.CONNECTION_CLOSED,""));
		}
		connectionComboBox.setEnabled(true);
		bConnect.setText(Translator.get("ButtonConnect"));
		bConnect.replaceIcon("/images/connect.png");
		bConnect.setForeground(Color.GREEN);
	}

	private void onOpen(NetworkSession s) {
		logger.debug("open to {}", s.getName());

		mySession = s;
		mySession.addListener((e)->{
			if (e.flag == NetworkSessionEvent.CONNECTION_CLOSED) {
				onClose(); 
			}
		});
		connectionComboBox.setEnabled(false);
		bConnect.setText(Translator.get("ButtonDisconnect"));
		bConnect.replaceIcon("/images/disconnect.png");
		bConnect.setForeground(Color.RED);
	}

	public NetworkSession getNetworkSession() {
		return mySession;
	}
	
	public void setNetworkSession(NetworkSession s) {
		if (s!=null && s!=mySession) {
			onClose();
			onOpen(s);
		}
	}

	public void closeConnection() {
		onClose();
	}

	// OBSERVER PATTERN
	
	private List<ActionListener> listeners = new ArrayList<ActionListener>();
	
	public void addActionListener(ActionListener a) {
		listeners.add(a);
	}
	
	public void removeActionListener(ActionListener a) {
		listeners.remove(a);
	}
	
	private void notifyListeners(ActionEvent e) {
		for( ActionListener a : listeners ) {
			a.actionPerformed(e);
		}
	}

	// TEST 
	
	public static void main(String[] args) {
		JFrame frame = new JFrame(ConnectionButton.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JComboBox<NetworkSessionItem> connectionComboBox = new JComboBox<>();
		connectionComboBox.addItem(new NetworkSessionItem(null, "/dev/cu.144"));
		frame.add(new ConnectionButton(connectionComboBox));
		frame.pack();
		frame.setVisible(true);
	}
}
