package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicReference;

public class MarlinInterfaceTest {

    @BeforeAll
    public static void init() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void onHearHomeXYFirst() {
        MarlinInterface mi = new MarlinInterface(new ChooseConnection());

        AtomicReference<ActionEvent> ae = new AtomicReference<>();

        mi.addListener(ae::set);
        String message = "echo:Home XY First";
        mi.onDataReceived(new NetworkSessionEvent(this, NetworkSessionEvent.DATA_RECEIVED, message));

        Assertions.assertNotNull(ae.get());
        Assertions.assertEquals(ActionEvent.ACTION_PERFORMED, ae.get().getID());
        Assertions.assertEquals(MarlinInterface.HOME_XY_FIRST, ae.get().getActionCommand());
    }

    @Test
    public void onHearError() {
        MarlinInterface mi = new MarlinInterface(new ChooseConnection());

        AtomicReference<ActionEvent> ae = new AtomicReference<>();

        mi.addListener(ae::set);
        String message = "Error: Printer halted";
        mi.onDataReceived(new NetworkSessionEvent(this, NetworkSessionEvent.DATA_RECEIVED, message));

        Assertions.assertNotNull(ae.get());
        Assertions.assertEquals(ActionEvent.ACTION_PERFORMED, ae.get().getID());
        Assertions.assertEquals(MarlinInterface.ERROR, ae.get().getActionCommand());
    }

    @Test
    public void didNotFind() {
        MarlinInterface mi = new MarlinInterface(new ChooseConnection());
        AtomicReference<ActionEvent> ae = new AtomicReference<>();
        mi.addListener(ae::set);

        mi.queueAndSendCommand("M400");

        Assertions.assertNotNull(ae.get());
        Assertions.assertEquals(ActionEvent.ACTION_PERFORMED, ae.get().getID());
        Assertions.assertEquals(MarlinInterface.DID_NOT_FIND, ae.get().getActionCommand());
    }
}
