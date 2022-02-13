package com.marginallyclever.makelangelo.plotter.plotterControls.communications;

import com.marginallyclever.communications.Configuration;
import com.marginallyclever.communications.TransportLayer;

import javax.swing.*;

public interface TransportLayerUI {
    void addToPanel(JPanel panel);

    TransportLayer getTransportLayer();

    void setSelectedValue(Configuration configuration);
}
