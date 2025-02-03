package com.marginallyclever.makelangelo;

import ModernDocking.DockingRegion;
import ModernDocking.app.AppState;
import ModernDocking.app.Docking;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.ext.ui.DockingUI;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.donatello.AddNodePanel;
import com.marginallyclever.donatello.Donatello;
import com.marginallyclever.donatello.actions.undoable.NodeAddAction;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;
import com.marginallyclever.makelangelo.applicationsettings.MetricsPreferences;
import com.marginallyclever.makelangelo.donatelloimpl.DonatelloDropTarget;
import com.marginallyclever.makelangelo.makeart.io.LoadFilePanel;
import com.marginallyclever.makelangelo.makeart.io.OpenFileChooser;
import com.marginallyclever.makelangelo.makeart.io.SaveGCode;
import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRenderer;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRendererFactory;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.preview.Camera;
import com.marginallyclever.makelangelo.preview.PreviewDropTarget;
import com.marginallyclever.makelangelo.preview.PreviewPanel;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.MarlinSimulationVisualizer;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFacade;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFactory;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderer;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * A JFrame that remembers its size and position.
 */
public class MainFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private final List<DockingPanel> windows = new ArrayList<>();

    private final Donatello donatello = new Donatello();
    private final AddNodePanel addNodePanel = new AddNodePanel();
    private final AboutPanel aboutPanel = new AboutPanel(MakelangeloVersion.VERSION, MakelangeloVersion.DETAILED_VERSION);

    private static final String PREFERENCE_SAVE_PATH = "savePath";

    private final Camera camera;
    private final PlotterSettingsManager plotterSettingsManager = new PlotterSettingsManager();
    private final Plotter myPlotter = new Plotter();
    private final Paper myPaper = new Paper();
    private Turtle myTurtle = new Turtle();
    private final TurtleRenderFacade myTurtleRenderer = new TurtleRenderFacade();
    private PlotterRenderer myPlotterRenderer;

    private final MainMenu mainMenuBar;
    private final PreviewPanel previewPanel = new PreviewPanel();
    private final MakeleangeloRangeSlider rangeSlider = new MakeleangeloRangeSlider();

    public MainFrame() {
        super();

        myPlotter.setSettings(plotterSettingsManager.getLastSelectedProfile());
        myPaper.loadConfig();

        createContentPane();

        previewPanel.addListener(myPlotter);
        addPlotterRendererToPreviewPanel();

        rangeSlider.addChangeListener(e->{
            myTurtleRenderer.setFirst(rangeSlider.getBottom());
            myTurtleRenderer.setLast(rangeSlider.getTop());
        });

        onPlotterSettingsUpdate(myPlotter.getSettings());

        logger.debug("Starting virtual camera...");
        camera = new Camera();

        createAppWindow();
        //checkSharingPermission();

        Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
        if (preferences.getBoolean("Check for updates", false)) checkForUpdate(true);

        setLocationByPlatform(true);

        initDocking();
        createDefaultLayout();
        resetDefaultLayout();
        saveAndRestoreLayout();

        mainMenuBar = new MainMenu(this);
        this.setJMenuBar(mainMenuBar);

        setupDropTarget();

        addNodePanel.addAddNodeListener(e->{
            NodeAddAction action = new NodeAddAction("Add",donatello);
            action.commitAdd(e,donatello.getPaintArea().getCameraPosition());
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

    private void updatePlotterRenderer() {
        try {
            myPlotterRenderer = PlotterRendererFactory.valueOf(myPlotter.getSettings().getString(PlotterSettings.STYLE)).getPlotterRenderer();
        } catch (Exception e) {
            logger.error("Failed to find plotter style {}", myPlotter.getSettings().getString(PlotterSettings.STYLE));
            myPlotterRenderer = PlotterRendererFactory.MAKELANGELO_5.getPlotterRenderer();
        }
    }

    public void onPlotterSettingsUpdate(PlotterSettings settings) {
        myPlotter.setSettings(settings);

        TurtleRenderer turtleRenderer = TurtleRenderFactory.MARLIN_SIM.getTurtleRenderer();
        if(turtleRenderer instanceof MarlinSimulationVisualizer msv) {
            msv.setSettings(settings);
            msv.reset();
        }
        myTurtleRenderer.setUpColor(settings.getColor(PlotterSettings.PEN_UP_COLOR));
        myTurtleRenderer.setPenDiameter(settings.getDouble(PlotterSettings.DIAMETER));
        // myTurtleRenderer.setDownColor() would be meaningless, the down color is stored in each Turtle.

        updatePlotterRenderer();

        if(previewPanel != null) previewPanel.repaint();
    }

    private void addPlotterRendererToPreviewPanel() {
        previewPanel.addListener((gl2)->{
            if(myPlotterRenderer!=null) {
                myPlotterRenderer.render(gl2, myPlotter);
            }
        });
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

    public void openFile(String filename) {
        try {
            LoadFilePanel loader = new LoadFilePanel(myPaper,filename);
            loader.addActionListener((e)-> setTurtle((Turtle)(e).getSource()));

            if(filename == null || filename.trim().isEmpty()) throw new InvalidParameterException("filename cannot be empty");

            if (loader.onNewFilenameChosen(filename)) {
                previewPanel.addListener(loader);
                JDialog dialog = new JDialog(this, Translator.get("LoadFilePanel.title"));
                dialog.add(loader);
                dialog.setMinimumSize(new Dimension(500,500));
                dialog.pack();
                dialog.setLocationRelativeTo(this);
                loader.setParent(dialog);

                enableMenuBar(false);
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        loader.loadingFinished();
                        enableMenuBar(true);
                        previewPanel.removeListener(loader);
                        mainMenuBar.getRecentFiles().addFilename(filename);
                    }
                });

                dialog.setVisible(true);
            } else {
                mainMenuBar.getRecentFiles().addFilename(filename);
            }

            setMainTitle(new File(filename).getName());
        } catch(Exception e) {
            logger.error("Error while loading the file {}", filename, e);
            JOptionPane.showMessageDialog(this, Translator.get("LoadError") + e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            mainMenuBar.getRecentFiles().removeFilename(filename);
        }
    }

    /**
     * Load a vector and add it to the existing {@link Turtle}.
     */
    public void importFile() {
        JFileChooser fileChooser = TurtleFactory.getFileChooser();
        // load the last path from preferences
        Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
        String lastPath = preferences.get(OpenFileChooser.KEY_PREFERENCE_LOAD_PATH, FileAccess.getWorkingDirectory());
        fileChooser.setCurrentDirectory(new File(lastPath));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getAbsolutePath();
            logger.debug("File selected by user: {}", filename);

            // save the path to preferences
            preferences.put(OpenFileChooser.KEY_PREFERENCE_LOAD_PATH, fileChooser.getCurrentDirectory().toString());

            // commit the load
            try {
                Turtle t = TurtleFactory.load(filename);
                myTurtle.add(t);
                setTurtle(myTurtle);
            } catch(Exception e) {
                logger.error("Failed to load {}", filename, e);
                JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
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

    private void createContentPane() {
        logger.debug("create content pane...");

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);

        logger.debug("  create PreviewPanel...");
        previewPanel.setCamera(camera);
        previewPanel.addListener(myPaper);
        previewPanel.addListener(myPlotter);
        previewPanel.addListener(myTurtleRenderer);
        addPlotterRendererToPreviewPanel();

        contentPane.add(previewPanel, BorderLayout.CENTER);
        contentPane.add(rangeSlider, BorderLayout.SOUTH);

        JToolBar toolBar = createToolBar();
        contentPane.add(toolBar, BorderLayout.NORTH);
    }

    private JToolBar createToolBar() {
        var bar = new JToolBar();

        var buttonZoomOut = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoom(1);
            }
        };
        buttonZoomOut.putValue(Action.SHORT_DESCRIPTION,Translator.get("MenuView.zoomOut"));
        buttonZoomOut.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
        buttonZoomOut.putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-zoom-out-16.png"))));
        bar.add(buttonZoomOut);

        var buttonZoomIn = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoom(-1);
            }
        };
        buttonZoomIn.putValue(Action.SHORT_DESCRIPTION,Translator.get("MenuView.zoomIn"));
        buttonZoomIn.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));
        buttonZoomIn.putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-zoom-in-16.png"))));
        bar.add(buttonZoomIn);

        var buttonZoomToFit = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomToFit(myPaper.getPaperWidth(),myPaper.getPaperHeight());
            }
        };
        buttonZoomToFit.putValue(Action.SHORT_DESCRIPTION,Translator.get("MenuView.zoomFit"));
        buttonZoomToFit.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
        buttonZoomToFit.putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-zoom-to-fit-16.png"))));
        bar.add(buttonZoomToFit);

        Action toggleAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean b = GFXPreferences.getShowPenUp();
                GFXPreferences.setShowPenUp(!b);
            }
        };
        var checkboxShowPenUpMoves = new JToggleButton(toggleAction);
        toggleAction.putValue(Action.SHORT_DESCRIPTION,Translator.get("GFXPreferences.showPenUp"));
        toggleAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));//"ctrl M"
        toggleAction.putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-plane-16.png"))));
        checkboxShowPenUpMoves.setSelected(GFXPreferences.getShowPenUp());
        GFXPreferences.addListener((e)->checkboxShowPenUpMoves.setSelected ((boolean)e.getNewValue()));
        bar.add(checkboxShowPenUpMoves);

        return bar;
    }


    //  For thread safety this method should be invoked from the event-dispatching thread.
    private void createAppWindow() {
        logger.debug("Creating GUI...");

        setMainTitle("");
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            // when someone tries to close the app, confirm it.
            @Override
            public void windowClosing(WindowEvent e) {
                confirmClose();
                super.windowClosing(e);
            }
        });

        try {
            this.setIconImage(ImageIO.read(Objects.requireNonNull(Makelangelo.class.getResource("/logo-icon.png"))));
        } catch (IOException e) {
            logger.warn("Can't load icon", e);
        }

        camera.zoomToFit( Paper.DEFAULT_WIDTH, Paper.DEFAULT_HEIGHT);

        loadPaths();
    }

    public boolean confirmClose() {
        int result = JOptionPane.showConfirmDialog(this, Translator.get("ConfirmQuitQuestion"),
                Translator.get("ConfirmQuitTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            previewPanel.removeListener(myPlotter);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            myPlotter.getSettings().save();
            plotterSettingsManager.setLastSelectedProfile(myPlotter.getSettings().getUID());
            savePaths();

            // Run this on another thread than the AWT event queue to
            // make sure the call to Animator.stop() completes before
            // exiting
            new Thread(()->{
                previewPanel.stop();
                this.dispose();
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
        preferences.put(PREFERENCE_SAVE_PATH, SaveDialog.getLastPath() );
    }

    /**
     * Use Preferences to recall the last "save" dialog path.
     */
    private void loadPaths() {
        Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
        SaveDialog.setLastPath( preferences.get(PREFERENCE_SAVE_PATH, FileAccess.getWorkingDirectory() ) );
    }

    public void setMainTitle(String title) {
        String finalTitle = MakelangeloVersion.getFullOrLiteVersionStringRelativeToSysEnvDevValue();
        if (!title.trim().isEmpty()) {
            finalTitle = title + " - " + finalTitle;
        }
        this.setTitle(finalTitle);
    }

    public void setTurtle(Turtle turtle) {
        myTurtle = turtle;
        myTurtleRenderer.setTurtle(turtle);
        int top = turtle.history.size();
        rangeSlider.setLimits(0,top);
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
        addDockingPanel("Preview","Preview",previewPanel);
        addDockingPanel("Donatello","Donatello",donatello);
        addDockingPanel("AddNode","Add Node",addNodePanel);
        addDockingPanel("About","About",aboutPanel);
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

        SaveGCode save = new SaveGCode();
        try {
            int head = rangeSlider.getValue();
            int tail = rangeSlider.getUpperValue();
            save.run(getTurtle(), getPlotter(), this, head, tail);
        } catch(Exception e) {
            logger.error("Error while exporting the gcode", e);
            JOptionPane.showMessageDialog(this, Translator.get("SaveError") + e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public Paper getPaper() {
        return myPaper;
    }

    public Plotter getPlotter() {
        return myPlotter;
    }

    public TurtleRenderer getTurtleRenderer() {
        return myTurtleRenderer.getRenderer();
    }

    public Camera getCamera() {
        return camera;
    }

    public void setTurtleRenderer(TurtleRenderer renderer) {
        myTurtleRenderer.setRenderer(renderer);
    }

    public PlotterSettingsManager getPlotterSettingsManager() {
        return plotterSettingsManager;
    }
}
