package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.communications.TransportLayer;
import com.marginallyclever.communications.serial.SerialTransportLayer;
import com.marginallyclever.communications.tcp.TCPTransportLayer;
import com.marginallyclever.makelangelo.plotter.plotterControls.communications.SerialUI;
import com.marginallyclever.makelangelo.plotter.plotterControls.communications.TCPUI;
import com.marginallyclever.makelangelo.plotter.plotterControls.communications.TransportLayerUI;

/**
 * Link between communications stuff and UI
 */
public enum CommunicationsManager {
    SERIAL(new SerialTransportLayer(), new SerialUI()),
    TCP(new TCPTransportLayer(), new TCPUI());

    CommunicationsManager(TransportLayer transportLayer, TransportLayerUI transportLayerUI) {
        this.transportLayer = transportLayer;
        this.transportLayerUI = transportLayerUI;
    }

    private TransportLayer transportLayer;
    private TransportLayerUI transportLayerUI;

    public TransportLayer getTransportLayer() {
        return transportLayer;
    }

    public TransportLayerUI getTransportLayerUI() {
        return transportLayerUI;
    }
}
