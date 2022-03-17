package com.marginallyclever.makelangelo.plotter.plotterControls.communications;

import com.marginallyClever.communications.Configuration;
import com.marginallyClever.communications.TransportLayer;
import com.marginallyClever.communications.dummy.DummyTransportLayer;

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
