package com.marginallyclever.makelangelo;

import ModernDocking.DockingRegion;
import ModernDocking.app.AppState;
import ModernDocking.app.Docking;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.ext.ui.DockingUI;
import com.marginallyclever.makelangelo.apps.AboutPanel;
import com.marginallyclever.makelangelo.apps.LogPanel;
import com.marginallyclever.makelangelo.apps.PaperSettingsPanel;
import com.marginallyclever.makelangelo.apps.PlotterSettingsManagerPanel;
import com.marginallyclever.makelangelo.apps.firmwareuploader.FirmwareUploaderPanel;
import com.marginallyclever.makelangelo.apps.previewpanel.PreviewPanel;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.Plotter;
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
    private final LogPanel logPanel;
    private final PreviewPanel previewPanel;
    private final PaperSettingsPanel paperSettingsPanel;
    private final PlotterSettingsManagerPanel plotterSettingsPanel;
    private final FirmwareUploaderPanel firmwareUploaderPanel;

    public MainFrame(Paper myPaper, Plotter myPlotter) {
        super();

        initDocking();

        logPanel = new LogPanel();
        previewPanel = new PreviewPanel(myPaper,myPlotter);
        paperSettingsPanel = new PaperSettingsPanel(myPaper);
        plotterSettingsPanel = new PlotterSettingsManagerPanel();
        firmwareUploaderPanel = new FirmwareUploaderPanel();
        plotterSettingsPanel.addListener(previewPanel::updatePlotterSettings);

        createDefaultLayout();
        saveAndRestoreLayout();
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

    /**
     * Persistent IDs were generated using <code>UUID.randomUUID().toString()</code>
     * or <a href="https://www.uuidgenerator.net/">one of many websites</a>.
     */
    private void createDefaultLayout() {
        DockingPanel renderView = new DockingPanel("8e50154c-a149-4e95-9db5-4611d24cc0cc", "Preview");
        renderView.add(previewPanel, BorderLayout.CENTER);
        windows.add(renderView);

        DockingPanel paperSettingsView = new DockingPanel("31ffe343-c085-44b1-9c61-28a049942287", Translator.get("PaperSettings.Title"));
        paperSettingsView.add(paperSettingsPanel, BorderLayout.CENTER);
        windows.add(paperSettingsView);

        DockingPanel machineSettingsView = new DockingPanel("67e525e8-111a-47c9-a490-127f94a99d1a", Translator.get("PlotterSettingsPanel.Title"));
        machineSettingsView.add(plotterSettingsPanel, BorderLayout.CENTER);
        windows.add(machineSettingsView);

        // TODO use getClass().getResource("icons8-install-16.png") as the icon in the window view
        DockingPanel firmwareUploaderView = new DockingPanel("f9e9b7b7-8ced-4edc-b032-d6bf960b0694", Translator.get("FirmwareUpdate"));
        firmwareUploaderView.add(firmwareUploaderPanel, BorderLayout.CENTER);
        windows.add(firmwareUploaderView);

        DockingPanel logView = new DockingPanel("5e565f83-9734-4281-9828-92cd711939df", Translator.get("LogPanel.Title"));
        logView.add(logPanel, BorderLayout.CENTER);
        windows.add(logView);

        DockingPanel aboutView = new DockingPanel("976af87b-90f3-42ce-a5d6-e4ab663fbb15", Translator.get("MenuAbout"));
        aboutView.add(new AboutPanel(), BorderLayout.CENTER);
        windows.add(aboutView);
/*
        DockingPanel webcamView = new DockingPanel("1331fbb0-ceda-4c67-b343-6539d4f939a1", "USB Camera");
        webcamView.add(webCamPanel, BorderLayout.CENTER);
        windows.add(webcamView);

        DockingPanel textInterfaceView = new DockingPanel("7796a733-8e33-417a-b363-b28174901e40", "Serial Interface");
        textInterfaceView.add(textInterface, BorderLayout.CENTER);
        windows.add(textInterfaceView);
*/
    }

    /**
     * Reset the default layout.  These depend on the order of creation in createDefaultLayout().
     */
    public void resetDefaultLayout() {
        logger.info("Resetting layout to default.");
        setSize(600, 800);

        // remove all windows
        for(DockingPanel w : windows) {
            Docking.undock(w);
        }

        // reattach starting windows
        var renderView = windows.get(0);
/*
        var treeView = windows.get(1);
        var detailView = windows.get(2);
        */

        Docking.dock(renderView, this, DockingRegion.CENTER);
        // Docking.dock(aboutView, renderView, DockingRegion.WEST);
        logger.info("done.");
    }

    private void saveAndRestoreLayout() {
        // now that the main frame is set up with the defaults, we can restore the layout
        var path = System.getProperty("user.home") + File.separator + ".makelangelo" + File.separator + "makelangelo.layout";
        logger.debug(path);
        AppState.setPersistFile(new File(path));
        AppState.setAutoPersist(true);

        try {
            AppState.restore();
        } catch (DockingLayoutException e) {
            // something happened trying to load the layout file, record it here
            logger.error("Failed to restore docking layout.", e);
        }
    }

    public List<DockingPanel> getDockingPanels() {
        return windows;
    }

    public PreviewPanel getPreviewPanel() {
        return previewPanel;
    }
}
