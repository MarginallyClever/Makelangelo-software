package com.marginallyclever.makelangelo;

import ModernDocking.DockingRegion;
import ModernDocking.app.AppState;
import ModernDocking.app.Docking;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.ext.ui.DockingUI;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.donatello.Donatello;
import com.marginallyclever.donatello.nodefactorypanel.NodeFactoryPanel;
import com.marginallyclever.makelangelo.applicationsettings.MetricsPreferences;
import com.marginallyclever.makelangelo.donatelloimpl.DockableEditNodePanel;
import com.marginallyclever.makelangelo.donatelloimpl.DonatelloDropTarget;
import com.marginallyclever.makelangelo.makeart.io.SaveGCode;
import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.preview.PreviewDropTarget;
import com.marginallyclever.makelangelo.preview.PreviewPanel;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderer;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * A JFrame that remembers its size and position.
 */
public class MainFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);

    private static final String PREFERENCE_SAVE_PATH = "savePath";

    private final List<DockingPanel> windows = new ArrayList<>();

    private final PreviewPanel previewPanel = new PreviewPanel();
    private final Donatello donatello = new Donatello();
    private final AboutPanel aboutPanel = new AboutPanel(MakelangeloVersion.VERSION, MakelangeloVersion.DETAILED_VERSION);
    private final NodeFactoryPanel nodeFactoryPanel = new NodeFactoryPanel();
    private final DockableEditNodePanel editNodePanel = new DockableEditNodePanel();

    private Turtle myTurtle = new Turtle();

    private final MainMenu mainMenuBar;

    public MainFrame() {
        super();

        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        createAppWindow();
        initDocking();
        createDefaultLayout();
        resetDefaultLayout();
        saveAndRestoreLayout();

        //checkSharingPermission();

        Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
        if (preferences.getBoolean("Check for updates", false)) checkForUpdate(true);

        // must appear after docks so that the Windows menu item can be populated.
        mainMenuBar = new MainMenu(this);
        setJMenuBar(mainMenuBar);

        selectionChangesEditNodePanel();
        setupDropTarget();
        donatello.connectNodeFactory(nodeFactoryPanel);
    }

    private void selectionChangesEditNodePanel() {
        donatello.addSelectionListener((selected)->{
            editNodePanel.setNodeAndGraph(selected.getSelectedNodes(),selected.getGraph());
        });
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

    // check if we need to ask about sharing
    @SuppressWarnings("unused")
    private void checkSharingPermission() {
        logger.debug("Checking sharing permissions...");

        final String SHARING_CHECK_STRING = "Last version sharing checked";

        Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
        String v = preferences.get(SHARING_CHECK_STRING,"0");
        int comparison = MakelangeloVersion.VERSION.compareTo(v);
        if(comparison!=0) {
            preferences.put(SHARING_CHECK_STRING, MakelangeloVersion.VERSION);
            int dialogResult = JOptionPane.showConfirmDialog(this, Translator.get("MetricsPreferences.collectAnonymousMetricsOnUpdate"),"Sharing Is Caring",JOptionPane.YES_NO_OPTION);
            MetricsPreferences.setAllowedToShare(dialogResult == JOptionPane.YES_OPTION);
        }
    }

    /**
     * Change the enable state of the menu items inside the {@code mainMenuBar}.
     * Remember that enabling the menuBar does not affect the children.
     * @param b the new state
     */
    public void enableMenuBar(boolean b) {
        int c = mainMenuBar.getMenuCount();
        while(--c>=0) {
            mainMenuBar.getMenu(c).setEnabled(b);
        }
    }



    /**
     * Parse <a href="https://github.com/MarginallyClever/Makelangelo/releases/latest">https://github.com/MarginallyClever/Makelangelo/releases/latest</a>
     * redirect notice to find the latest release tag.
     */
    public void checkForUpdate(boolean announceIfFailure) {
        logger.debug("checking for updates...");
        try {
            URI link = new URI("https://github.com/MarginallyClever/Makelangelo-Software/releases/latest");
            HttpURLConnection conn = (HttpURLConnection)link.toURL().openConnection();
            conn.setInstanceFollowRedirects(false); // you still need to handle redirect manually.
            conn.setConnectTimeout(5000);
            conn.connect();
            int responseCode = conn.getResponseCode();
            String responseMessage = conn.getHeaderField("Location");
            conn.disconnect();

            // parse the last part of the redirect URL, which contains the
            // release tag (which is the VERSION)
            String line2 = responseMessage.substring(responseMessage.lastIndexOf("/") + 1);

            logger.debug("latest release: {}; this version: {}@{}", line2, MakelangeloVersion.VERSION, MakelangeloVersion.DETAILED_VERSION);

            int comp = line2.compareTo(MakelangeloVersion.VERSION);
            String results;
            if (comp > 0) results = Translator.get("UpdateNotice");
            else if (comp < 0) results = "This version is from the future?!";
            else results = Translator.get("UpToDate");

            JOptionPane.showMessageDialog(this, results);
        } catch (Exception e) {
            if (announceIfFailure) {
                JOptionPane.showMessageDialog(null, Translator.get("UpdateCheckFailed") + e.getLocalizedMessage());
            }
            logger.error("Update check failed", e);
        }
    }

    /**
     * See <a href="http://www.dreamincode.net/forums/topic/190944-creating-an-updater-in-java/">creating an updater in java</a>
     *//*
     * private void downloadUpdate() {
     *   String[] run = {"java","-jar","updater/update.jar"};
     *   try {
     *     Runtime.getRuntime().exec(run);
     *   } catch (Exception ex) {
     *     ex.printStackTrace();
     *   }
     *   System.exit(0);
     * }
     */
    //  For thread safety this method should be invoked from the event-dispatching thread.
    private void createAppWindow() {
        setMainTitle("");

        addWindowListener(new WindowAdapter() {
            // when someone tries to close the app, confirm it.
            @Override
            public void windowClosing(WindowEvent e) {
                confirmClose();
                super.windowClosing(e);
            }
        });

        try {
            setIconImage(ImageIO.read(Objects.requireNonNull(Makelangelo.class.getResource("/logo-icon.png"))));
        } catch (IOException e) {
            logger.warn("Can't load icon", e);
        }

        loadPaths();
    }

    public boolean confirmClose() {
        int result = JOptionPane.showConfirmDialog(this, Translator.get("ConfirmQuitQuestion"),
                Translator.get("ConfirmQuitTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            savePaths();

            // Run this on another thread than the AWT event queue to
            // make sure the call to Animator.stop() completes before
            // exiting
            new Thread(()->{
                previewPanel.stop();
                dispose();
            }).start();
            return true;
        }
        return false;
    }

    /**
     * Use Preferences to store the last "save" dialog path.
     */
    private void savePaths() {
        Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
        preferences.put(PREFERENCE_SAVE_PATH, TurtleFactory.getSaveFileChooser().getCurrentDirectory().getAbsolutePath() );
    }

    /**
     * Use Preferences to recall the last "save" dialog path.
     */
    private void loadPaths() {
        Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
        TurtleFactory.getSaveFileChooser().setCurrentDirectory(new File(preferences.get(PREFERENCE_SAVE_PATH, FileAccess.getWorkingDirectory() )));
    }

    public void setMainTitle(String title) {
        String finalTitle = MakelangeloVersion.getFullOrLiteVersionStringRelativeToSysEnvDevValue();
        if (!title.trim().isEmpty()) {
            finalTitle = title + " - " + finalTitle;
        }
        setTitle(finalTitle);
    }

    public void setTurtle(Turtle turtle) {
        myTurtle = turtle;
        previewPanel.setTurtle(turtle);
    }

    public Turtle getTurtle() {
        return myTurtle;
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
        var addPanel = windows.get(2);
        var aboutPanel2 = windows.get(3);
        Docking.dock(previewPanel, this, DockingRegion.CENTER);
        Docking.dock(donatelloPanel, previewPanel, DockingRegion.CENTER);
        Docking.dock(addPanel, previewPanel, DockingRegion.EAST);
        Docking.dock(aboutPanel2, addPanel, DockingRegion.NORTH);
        logger.debug("done.");
    }

    public void createDefaultLayout() {
        addDockingPanel("Preview","Preview", previewPanel);
        addDockingPanel("Donatello","Donatello",donatello);
        addDockingPanel("AddNode","Add Node", nodeFactoryPanel);
        addDockingPanel("About","About",aboutPanel);
        addDockingPanel("EditNode","Edit Node",editNodePanel);
    }

    private void setupDropTarget() {
        logger.debug("adding drag & drop support...");
        new DropTarget(donatello, new DonatelloDropTarget(donatello));
        new DropTarget(previewPanel, new PreviewDropTarget(this));
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

    public void onDialogAbout() {
        Docking.undock(windows.get(3));
        Docking.dock(windows.get(3), this, DockingRegion.CENTER);
    }

    public List<DockingPanel> getDockingPanels() {
        return windows;
    }

    public void saveGCode() {
        logger.debug("Saving to gcode...");

        try {
            int head = previewPanel.getRangeBottom();
            int tail = previewPanel.getRangeTop();
            SaveGCode save = new SaveGCode();
            save.run(getTurtle(), previewPanel.getPlotter(), this, head, tail);
        } catch(Exception e) {
            logger.error("Error while exporting the gcode", e);
            JOptionPane.showMessageDialog(this, Translator.get("SaveError") + e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public PreviewPanel getPreviewPanel() {
        return previewPanel;
    }

    public Plotter getPlotter() {
        return previewPanel.getPlotter();
    }

    public Paper getPaper() {
        return previewPanel.getPaper();
    }

    public TurtleRenderer getTurtleRenderer() {
        return previewPanel.getTurtleRenderer();
    }

    public void setTurtleRenderer(TurtleRenderer turtleRenderer) {
        previewPanel.setTurtleRenderer(turtleRenderer);
    }

    public PlotterSettingsManager getPlotterSettingsManager() {
        return previewPanel.getPlotterSettingsManager();
    }

    public void onPlotterSettingsUpdate(PlotterSettings lastSelectedProfile) {
        previewPanel.onPlotterSettingsUpdate(lastSelectedProfile);
    }
}
