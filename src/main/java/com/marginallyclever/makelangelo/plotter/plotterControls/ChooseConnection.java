package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.communications.*;
import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.convenience.ToggleButtonIcon;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
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

	private final ToggleButtonIcon bConnect = new ToggleButtonIcon(
			Arrays.asList("ButtonConnect", "ButtonDisconnect"),
			Arrays.asList("/images/connect.png", "/images/disconnect.png"),
			Arrays.asList(Color.GREEN, Color.RED));
	private final ButtonIcon refresh = new ButtonIcon("", "/images/arrow_refresh.png");
	private final JComboBox<Configuration> connectionComboBox = new JComboBox<>();
	private final JPanel configurationPanel = new JPanel();
	private TransportLayer previousTransportLayer;

	private NetworkSession mySession;

	public ChooseConnection() {
		this.add(connectionComboBox);
		connectionComboBox.addActionListener(this::updateConfigurationPanel);
		addConnectionsItems(connectionComboBox);

		configurationPanel.setLayout(new BoxLayout(configurationPanel, BoxLayout.LINE_AXIS));
		this.add(configurationPanel);

		refresh.addActionListener(e -> addConnectionsItems(connectionComboBox));
		this.add(refresh);

		bConnect.addActionListener(e-> onConnectAction());
		this.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.add(bConnect);
	}

	private void updateConfigurationPanel(ActionEvent actionEvent) {
		Configuration configuration = (Configuration) ((JComboBox )actionEvent.getSource()).getSelectedItem();
		TransportLayer transportLayer = configuration.getTransportLayer();
		if (previousTransportLayer != configuration.getTransportLayer()) {
			configurationPanel.removeAll();
			configuration.getTransportLayerUI().addToPanel(configurationPanel);
			configurationPanel.revalidate();
		}
		previousTransportLayer = transportLayer;
	}

	private void addConnectionsItems(JComboBox<Configuration> comboBox) {
		comboBox.removeAllItems();
		logger.debug("Fetching connections");
		for (TransportLayer transportLayer : TransportLayers.transportLayers) {
			logger.debug("  {}" ,transportLayer.getName());
			for (String connection: transportLayer.listConnections()) {
				logger.debug("    {}", connection);
				Configuration configuration = new Configuration(transportLayer, connection);
				comboBox.addItem(configuration);
			}
		}

		bConnect.setEnabled(comboBox.getItemCount() > 0);
	}

	private void onConnectAction() {
		if (mySession != null) {
			onClose();
		} else {
			int speed = 42; // (int) baudRateComboBox.getSelectedItem();
			Configuration configuration = (Configuration) connectionComboBox.getSelectedItem();
			if (configuration==null) return;  // no connections at all
			configuration.addConfiguration("speed", speed);
			NetworkSession networkSession = configuration.getTransportLayer().openConnection(configuration);
			if (networkSession != null) {
				onOpen(networkSession);
				notifyListeners(new NetworkSessionEvent(this, NetworkSessionEvent.CONNECTION_OPENED, networkSession));
			} else {
				notifyListeners(new NetworkSessionEvent(this, NetworkSessionEvent.CONNECTION_ERROR, null));
			}
		}
	}

	private void onClose() {
		if (mySession != null) {
			mySession.closeConnection();
			mySession = null;
			notifyListeners(new NetworkSessionEvent(this, NetworkSessionEvent.CONNECTION_CLOSED,null));
		}
		connectionComboBox.setEnabled(true);
		refresh.setEnabled(true);
		bConnect.updateButton(0);
	}

	private void onOpen(NetworkSession s) {
		mySession = s;
		mySession.addListener(e->{
			if (e.flag == NetworkSessionEvent.CONNECTION_CLOSED) {
				onClose();
			}
		});
		connectionComboBox.setEnabled(false);
		refresh.setEnabled(false);
		bConnect.updateButton(1);
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
		frame.setMinimumSize(new Dimension(800, 70));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ChooseConnection());
		frame.pack();
		frame.setVisible(true);
	}
}
