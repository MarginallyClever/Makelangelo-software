package com.marginallyclever.makelangelo.apps.plottercontrols.communications;

import com.marginallyclever.communications.Configuration;
import com.marginallyclever.communications.TransportLayer;
import com.marginallyclever.communications.tcp.TCPTransportLayer;

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
