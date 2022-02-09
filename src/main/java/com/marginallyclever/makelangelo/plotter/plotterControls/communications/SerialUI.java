package com.marginallyclever.makelangelo.plotter.plotterControls.communications;

import javax.swing.*;
import java.util.Arrays;

public class SerialUI implements TransportLayerUI {

    @Override
    public void addToPanel(JPanel panel) {
        panel.add(new JLabel("@"));
        JComboBox<Integer> baudRateComboBox = new JComboBox<>();
        Arrays.asList(250000, 115200, 57600, 38400, 19200).forEach(baudRateComboBox::addItem);
        panel.add(baudRateComboBox);
    }
}
