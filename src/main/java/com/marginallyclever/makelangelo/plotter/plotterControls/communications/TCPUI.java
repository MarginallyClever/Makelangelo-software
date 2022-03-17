package com.marginallyclever.makelangelo.plotter.plotterControls.communications;

import com.marginallyClever.communications.Configuration;
import com.marginallyClever.communications.TransportLayer;
import com.marginallyClever.communications.tcp.TCPTransportLayer;

import javax.swing.*;

public class TCPUI implements TransportLayerUI {

    public TCPUI() {}

    @Override
    public void addToPanel(JPanel panel) {
    }

    public TransportLayer getTransportLayer() {
        return new TCPTransportLayer();
    }

    public void setSelectedValue(Configuration configuration) {

    }

    public void onClose() {}

    public void onOpen() {}
}
