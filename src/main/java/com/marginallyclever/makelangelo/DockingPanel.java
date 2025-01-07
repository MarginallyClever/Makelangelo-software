package com.marginallyclever.makelangelo;

import ModernDocking.Dockable;
import ModernDocking.app.Docking;

import javax.swing.*;
import java.awt.*;

/**
 * {@link DockingPanel} is a {@link JPanel} that implements {@link Dockable}.
 */
public class DockingPanel extends JPanel implements Dockable {
    private final String tabText;
    private final String persistentID;

    public DockingPanel(String persistentID, String tabText) {
        super(new BorderLayout());
        this.persistentID = persistentID;
        this.tabText = tabText;
        Docking.registerDockable(this);
    }

    @Override
    public String getPersistentID() {
        return persistentID;
    }

    @Override
    public String getTabText() {
        return tabText;
    }

    /**
     * Refuse to wrap this {@link DockingPanel} in a {@link JScrollPane}.  The panel is responsibile for scrolling,
     * not the docking system.
     * @return false
     */
    @Override
    public boolean isWrappableInScrollpane() {
        return false;
    }
}