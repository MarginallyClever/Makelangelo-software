package com.marginallyclever.makelangelo;

import ModernDocking.DockingRegion;
import ModernDocking.app.AppState;
import ModernDocking.app.Docking;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.ext.ui.DockingUI;
import com.marginallyclever.convenience.FileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A JFrame that remembers its size and position.
 */
public class MainFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private final List<DockingPanel> windows = new ArrayList<>();

    public MainFrame() {
        super();
        setLocationByPlatform(true);
        initDocking();
    }

    private void initDocking() {
        Docking.initialize(this);
        DockingUI.initialize();
        ModernDocking.settings.Settings.setAlwaysDisplayTabMode(true);
        ModernDocking.settings.Settings.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // create root panel
        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);
    }

    public void addDockingPanel(String persistentID,String tabText,Component component) {
        DockingPanel panel = new DockingPanel(persistentID,tabText);
        panel.add(component);
        windows.add(panel);
    }

    /**
     * Reset the default layout.  These depend on the order of creation in createDefaultLayout().
     */
    public void resetDefaultLayout() {
        logger.info("Resetting layout to default.");
        setSize(1000, 750);

        for (DockingPanel w : windows) {
            Docking.undock(w);
        }
        var previewPanel = windows.get(0);
        var donatelloPanel = windows.get(1);
        Docking.dock(previewPanel, this, DockingRegion.CENTER);
        Docking.dock(donatelloPanel, previewPanel, DockingRegion.SOUTH);
        logger.debug("done.");
    }

    public void saveAndRestoreLayout() {
        // now that the main frame is set up with the defaults, we can restore the layout
        var layoutPath = FileAccess.getHomeDirectory()+ File.separator+".makelangelo"+File.separator+"makelangelo.layout";
        logger.debug("layout file={}",layoutPath);
        AppState.setPersistFile(new File(layoutPath));
        AppState.setAutoPersist(true);

        try {
            AppState.restore();
        } catch (DockingLayoutException e) {
            logger.error("Failed to restore docking layout.", e);
        }
    }

    public List<DockingPanel> getDockingPanels() {
        return windows;
    }
}
