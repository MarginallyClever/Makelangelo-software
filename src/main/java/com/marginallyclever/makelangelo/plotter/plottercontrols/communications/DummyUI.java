package com.marginallyclever.makelangelo.plotter.plottercontrols.communications;

import com.marginallyclever.communications.Configuration;
import com.marginallyclever.communications.TransportLayer;
import com.marginallyclever.communications.dummy.DummyTransportLayer;

import javax.swing.*;

public class DummyUI implements TransportLayerUI {
    @Override
    public void addToPanel(JPanel panel) {

    }

    @Override
    public TransportLayer getTransportLayer() {
        return new DummyTransportLayer();
    }

    @Override
    public void setSelectedValue(Configuration configuration) {

    }

    @Override
    public void onClose() {

    }

    @Override
    public void onOpen() {

    }
}
