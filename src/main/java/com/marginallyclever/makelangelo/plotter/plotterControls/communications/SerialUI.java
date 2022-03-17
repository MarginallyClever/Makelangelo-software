package com.marginallyclever.makelangelo.plotter.plotterControls.communications;

import com.marginallyClever.communications.Configuration;
import com.marginallyClever.communications.TransportLayer;
import com.marginallyClever.communications.serial.SerialTransportLayer;

import javax.swing.*;

public class SerialUI implements TransportLayerUI {

    private final JComboBox<Integer> baudRateComboBox = new JComboBox<>(new Integer[] {250000, 115200, 57600, 38400, 19200});

    public SerialUI() {}

    @Override
    public void addToPanel(JPanel panel) {
        panel.add(new JLabel("@"));
        panel.add(baudRateComboBox);
    }

    public TransportLayer getTransportLayer() {
        return new SerialTransportLayer();
    }

    public void setSelectedValue(Configuration configuration) {
        configuration.addConfiguration("speed", baudRateComboBox.getSelectedItem());
    }

    public void onClose() {
        baudRateComboBox.setEnabled(true);
    }

    public void onOpen() {
        baudRateComboBox.setEnabled(false);
    }

}
