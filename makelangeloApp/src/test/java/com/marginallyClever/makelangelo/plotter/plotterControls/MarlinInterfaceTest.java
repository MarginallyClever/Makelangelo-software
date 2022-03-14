package com.marginallyClever.makelangelo.plotter.plotterControls;

import com.marginallyClever.communications.NetworkSessionEvent;
import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.util.PreferencesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

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

        assertNotNull(ae.get());
        assertEquals(ActionEvent.ACTION_PERFORMED, ae.get().getID());
        assertEquals(MarlinInterface.HOME_XY_FIRST, ae.get().getActionCommand());
    }

    @Test
    public void onHearError() {
        MarlinInterface mi = new MarlinInterface(new ChooseConnection());

        AtomicReference<ActionEvent> ae = new AtomicReference<>();

        mi.addListener(ae::set);
        String message = "Error: Printer halted";
        mi.onDataReceived(new NetworkSessionEvent(this, NetworkSessionEvent.DATA_RECEIVED, message));

        assertNotNull(ae.get());
        assertEquals(ActionEvent.ACTION_PERFORMED, ae.get().getID());
        assertEquals(MarlinInterface.ERROR, ae.get().getActionCommand());
    }

    @Test
    public void didNotFind() {
        MarlinInterface mi = new MarlinInterface(new ChooseConnection());
        AtomicReference<ActionEvent> ae = new AtomicReference<>();
        mi.addListener(ae::set);

        mi.queueAndSendCommand("M400");

        assertNotNull(ae.get());
        assertEquals(ActionEvent.ACTION_PERFORMED, ae.get().getID());
        assertEquals(MarlinInterface.DID_NOT_FIND, ae.get().getActionCommand());
    }
}
