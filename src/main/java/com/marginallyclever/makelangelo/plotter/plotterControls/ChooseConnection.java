package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.communications.*;
import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.convenience.ToggleButtonIcon;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.plotterControls.communications.DummyUI;
import com.marginallyclever.makelangelo.plotter.plotterControls.communications.SerialUI;
import com.marginallyclever.makelangelo.plotter.plotterControls.communications.TransportLayerUI;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
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

	private final ToggleButtonIcon bConnect = new ToggleButtonIcon(
			new ToggleButtonIcon.Item("ButtonConnect", "/images/connect.png", Color.GREEN),
			new ToggleButtonIcon.Item("ButtonDisconnect", "/images/disconnect.png", Color.RED)
		);
	private final ButtonIcon refresh = new ButtonIcon("", "/images/arrow_refresh.png");
	private final JComboBox<ComboItem> connectionComboBox = new JComboBox<>();
	private final JPanel configurationPanel = new JPanel();
	private TransportLayerUI previousTransportLayerUI;
	private final List<TransportLayerUI> availableTransportsUI = new ArrayList<>();
	private NetworkSession mySession;

	public ChooseConnection() {
		availableTransportsUI.add(new SerialUI());
		if ("true".equalsIgnoreCase(System.getenv("DEV"))) {
			availableTransportsUI.add(new DummyUI());
		}

		this.add(connectionComboBox);
		connectionComboBox.addItemListener(this::updateConfigurationPanel);
		addConnectionsItems(connectionComboBox);

		configurationPanel.setLayout(new BoxLayout(configurationPanel, BoxLayout.LINE_AXIS));
		this.add(configurationPanel);

		refresh.addActionListener(e -> addConnectionsItems(connectionComboBox));
		this.add(refresh);

		bConnect.addActionListener(e-> onConnectAction());
		this.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.add(bConnect);
	}

	private void updateConfigurationPanel(ItemEvent itemEvent) {
		ComboItem comboItem = (ComboItem) itemEvent.getItem();
		TransportLayerUI transportLayerUI = comboItem.transportLayerUi;
		if (previousTransportLayerUI != transportLayerUI) {
			configurationPanel.removeAll();
			comboItem.transportLayerUi.addToPanel(configurationPanel);
			configurationPanel.revalidate();
		}
		previousTransportLayerUI = transportLayerUI;
	}

	private void addConnectionsItems(JComboBox<ComboItem> comboBox) {
		comboBox.removeAllItems();
		logger.debug("Fetching connections");
		for (TransportLayerUI transportLayerUi : availableTransportsUI) {
			TransportLayer transportLayer = transportLayerUi.getTransportLayer();
			for (String connection: transportLayer.listConnections()) {
				Configuration configuration = new Configuration(transportLayer, connection);
				comboBox.addItem(new ComboItem(configuration, transportLayerUi));
			}
		}

		bConnect.setEnabled(comboBox.getItemCount() > 0);
	}

	private void onConnectAction() {
		if (mySession != null) {
			onClose();
		} else {
			ComboItem comboItem = (ComboItem) connectionComboBox.getSelectedItem();
			if (comboItem==null) return;  // no connections selected, can't happened

			Configuration configuration = comboItem.configuration;
			comboItem.transportLayerUi.setSelectedValue(configuration);
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
			notifyListeners(new NetworkSessionEvent(this, NetworkSessionEvent.CONNECTION_CLOSED, null));
		}

		connectionComboBox.setEnabled(true);
		refresh.setEnabled(true);
		bConnect.updateButton(0);
		availableTransportsUI.forEach(TransportLayerUI::onClose);
	}

	private void onOpen(NetworkSession s) {
		mySession = s;
		mySession.addListener(e -> {
			if (e.flag == NetworkSessionEvent.CONNECTION_CLOSED) {
				onClose();
			}
		});

		connectionComboBox.setEnabled(false);
		refresh.setEnabled(false);
		bConnect.updateButton(1);
		availableTransportsUI.forEach(TransportLayerUI::onOpen);
	}

	public void closeConnection() {
		onClose();
	}

	private class ComboItem {
		private final Configuration configuration;
		private final TransportLayerUI transportLayerUi;

		private ComboItem(Configuration configuration, TransportLayerUI transportLayerUi) {
			this.configuration = configuration;
			this.transportLayerUi = transportLayerUi;
		}

		public String toString() {
			return configuration.getConnectionName();
		}
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
