package com.marginallyclever.makelangelo.plotter.plottercontrols.communications;

import com.marginallyclever.communications.Configuration;
import com.marginallyclever.communications.TransportLayer;

import javax.swing.*;

public interface TransportLayerUI {
    void addToPanel(JPanel panel);

    TransportLayer getTransportLayer();

    void setSelectedValue(Configuration configuration);

    void onClose();

    void onOpen();
}
