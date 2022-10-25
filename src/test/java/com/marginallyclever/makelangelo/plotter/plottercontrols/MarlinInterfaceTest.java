package com.marginallyclever.makelangelo.plotter.plottercontrols;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

        AtomicReference<MarlinInterfaceEvent> ae = new AtomicReference<>();

        mi.addListener(ae::set);
        String message = "echo:Home XY First";
        mi.onDataReceived(new NetworkSessionEvent(this, NetworkSessionEvent.DATA_RECEIVED, message));

        Assertions.assertNotNull(ae.get());
        Assertions.assertEquals(MarlinInterfaceEvent.HOME_XY_FIRST, ae.get().getID());
    }

    @Test
    public void onHearActionCommand() {
        MarlinInterface mi = new MarlinInterface(new ChooseConnection());

        AtomicReference<MarlinInterfaceEvent> ae = new AtomicReference<>();

        mi.addListener(ae::set);
        String message = "//action:notification Ready black and click";
        mi.onDataReceived(new NetworkSessionEvent(this, NetworkSessionEvent.DATA_RECEIVED, message));

        Assertions.assertNotNull(ae.get());
        Assertions.assertEquals(MarlinInterfaceEvent.ACTION_COMMAND, ae.get().getID());
        Assertions.assertEquals("notification Ready black and click", ae.get().getActionCommand());
    }

    @Test
    public void onHearError() {
        MarlinInterface mi = new MarlinInterface(new ChooseConnection());

        AtomicReference<MarlinInterfaceEvent> ae = new AtomicReference<>();

        mi.addListener(ae::set);
        String message = "Error: Printer halted";
        mi.onDataReceived(new NetworkSessionEvent(this, NetworkSessionEvent.DATA_RECEIVED, message));

        Assertions.assertNotNull(ae.get());
        Assertions.assertEquals(MarlinInterfaceEvent.ERROR, ae.get().getID());
        Assertions.assertEquals("Printer halted", ae.get().getActionCommand());
    }

    @Test
    public void didNotFind() {
        MarlinInterface mi = new MarlinInterface(new ChooseConnection());
        AtomicReference<MarlinInterfaceEvent> ae = new AtomicReference<>();
        mi.addListener(ae::set);

        mi.queueAndSendCommand("M400");

        Assertions.assertNotNull(ae.get());
        Assertions.assertEquals(MarlinInterfaceEvent.DID_NOT_FIND, ae.get().getID());
    }
}
