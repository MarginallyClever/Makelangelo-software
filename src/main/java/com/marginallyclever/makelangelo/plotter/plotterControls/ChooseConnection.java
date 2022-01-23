package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.communications.*;
import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ChooseConnection provides a human interface to open or close a
 * connection to a remote device available through a {@link NetworkSession}.
 *
 * @author Dan Royer
 * @since 7.28.0
 */
public class ChooseConnection extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(ChooseConnection.class);
	
	private static final long serialVersionUID = 4773092967249064165L;
	@Deprecated
	public static final int CONNECTION_OPENED = 1;
	@Deprecated
	public static final int CONNECTION_CLOSED = 2;
	
	private ButtonIcon bConnect = new ButtonIcon("ButtonConnect", "/images/connect.png");
	private ButtonIcon refresh = new ButtonIcon("", "/images/arrow_refresh.png");
	private final JComboBox<NetworkSessionItem> connectionComboBox = new JComboBox<>();
	private NetworkSession mySession;

	public ChooseConnection() {
		super();

		this.add(connectionComboBox);

		refresh.addActionListener(e -> addConnectionsItems(connectionComboBox));
		this.add(refresh);
		addConnectionsItems(connectionComboBox);

		bConnect.setForeground(Color.GREEN);
		bConnect.addActionListener((e)-> onConnectAction() );
		
		this.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.add(bConnect);
	}

	private void addConnectionsItems(JComboBox<NetworkSessionItem> comboBox) {
		comboBox.removeAllItems();
		for (NetworkSessionItem connection: NetworkSessionUIManager.getConnectionsItems()) {
			comboBox.addItem(connection);
		}
	}

	private void onConnectAction() {
		logger.debug("onConnectAction()");
		if (mySession != null) {
			onClose();
		} else {
			NetworkSessionItem networkSessionItem = connectionComboBox.getItemAt(connectionComboBox.getSelectedIndex());
			if(networkSessionItem==null) return;  // no connections at all
			NetworkSession networkSession = networkSessionItem.getTransportLayer().openConnection(networkSessionItem.getConnectionName());
			if (networkSession != null) {
				onOpen(networkSession);
				notifyListeners(new NetworkSessionEvent(this, NetworkSessionEvent.CONNECTION_OPENED, networkSession));
			}
		}
	}

	private void onClose() {
		logger.debug("closed");
		if (mySession != null) {
			mySession.closeConnection();
			mySession = null;
			notifyListeners(new NetworkSessionEvent(this, NetworkSessionEvent.CONNECTION_CLOSED,null));
		}
		connectionComboBox.setEnabled(true);
		refresh.setEnabled(true);
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
		refresh.setEnabled(false);
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

	private List<NetworkSessionListener> listeners = new ArrayList<>();

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

	// TEST 
	
	public static void main(String[] args) {
		PreferencesHelper.start();
		Translator.start();

		JFrame frame = new JFrame(ChooseConnection.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ChooseConnection());
		frame.pack();
		frame.setVisible(true);
	}
}
