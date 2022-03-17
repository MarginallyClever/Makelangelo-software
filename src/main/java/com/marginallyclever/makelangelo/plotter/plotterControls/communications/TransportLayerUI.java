package com.marginallyclever.makelangelo.plotter.plotterControls.communications;

import com.marginallyClever.communications.Configuration;
import com.marginallyClever.communications.TransportLayer;

import javax.swing.*;

public interface TransportLayerUI {
    void addToPanel(JPanel panel);

    TransportLayer getTransportLayer();

    void setSelectedValue(Configuration configuration);

    void onClose();

    void onOpen();
}
