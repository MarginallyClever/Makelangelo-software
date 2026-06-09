package com.marginallyclever.makelangelo;

import ModernDocking.app.DockableMenuItem;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.log.LogPanel;
import com.marginallyclever.makelangelo.actions.*;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGeneratorFactory;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGeneratorPanel;
import com.marginallyclever.makelangelo.makeart.turtletool.*;
import com.marginallyclever.makelangelo.paper.PaperSettingsPanel;
import com.marginallyclever.makelangelo.plotter.PiCaptureAction;
import com.marginallyclever.makelangelo.plotter.marlinsimulation.MarlinSimulation;
import com.marginallyclever.makelangelo.plotter.plottercontrols.PlotterControls;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManagerPanel;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;

/**
 * The MainMenu class is responsible for managing the application's main menu bar.
 * It extends the JMenuBar class and provides a series of menus and menu items for
 * facilitating user interaction with different application features.
 *
 * The class also handles platform-specific adjustments for MacOS and provides
 * utilities for creating and organizing menus and their respective actions.
 *
 * The main menu shortcuts are controlled by the {@link MenuShortcutManager}, which
 * stores user settings in <code>~/.makelangelo/shortcuts.json</code>.
 */
public class MainMenu extends JMenuBar {
    private static final Logger logger = LoggerFactory.getLogger(MainMenu.class);

    public static int SHORTCUT_CTRL = InputEvent.CTRL_DOWN_MASK;
    public static int SHORTCUT_ALT = InputEvent.ALT_DOWN_MASK;

    private final MainFrame frame;
    private RecentFiles recentFiles;
    private boolean isMacOS = false;

    public MainMenu(MainFrame frame) {
        super();
        this.frame = frame;
        setSystemLookAndFeelForMacos();
        add(createFileMenu());
        add(createSettingsMenu());
        add(createGenerateMenu());
        add(createToolsMenu());
        add(createViewMenu());
        add(createWindowsMenu());
        add(createRobotMenu());
        add(createHelpMenu());
        updateUI();

        new MenuShortcutManager().manage(this);
    }

    private void setSystemLookAndFeelForMacos() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if ((os.contains("mac")) || (os.contains("darwin"))) {
            isMacOS=true;
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            SHORTCUT_ALT = InputEvent.META_DOWN_MASK;
        }
    }

    private JMenu createFileMenu() {
        JMenu menu = new JMenu(Translator.get("MenuMakelangelo"));
        menu.setMnemonic('f');

        recentFiles = new RecentFiles(Translator.get("MenuReopenFile"),frame);

        menu.add(new NewFileAction("MenuNewFile",frame));
        menu.add(new LoadFileAction(null,frame,recentFiles));
        menu.add(recentFiles);
        menu.add(new ImportFileAction("MenuImportFile",frame));
        menu.add(new SaveFileAction("MenuSaveFile",frame));
        menu.addSeparator();
        menu.add(new AdjustPreferencesAction("ApplicationSettings.title",frame));
        menu.add(new UpdateFirmwareAction("FirmwareUpdate",frame));

        addQuit(menu);

        return menu;
    }

    private void addQuit(JMenu menu) {
        boolean added=false;

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
                added = true;
                desktop.setQuitHandler((evt, res) -> {
                    if (frame.confirmClose()) {
                        res.performQuit();
                    } else {
                        res.cancelQuit();
                    }
                });
            }
        }

        if(!added) {
            menu.addSeparator();
            menu.add(new QuitAction("MenuQuit",frame));
        }
    }

    private JMenu createViewMenu() {
        JMenu menu = new JMenu(Translator.get("MenuView"));
        menu.setMnemonic('V');

        menu.add(createRenderStyleMenu());
        menu.addSeparator();

        var app = frame.getPreviewPanel();
        var camera = app.getCamera();
        var paper = frame.getEditorContext().getPaper();

        menu.add(new ZoomOutAction("MenuView.zoomOut",camera));
        menu.add(new ZoomInAction("MenuView.zoomIn",camera));
        menu.add(new ZoomToFitAction("MenuView.zoomFit",camera,paper));
        menu.add(new ZoomToFitMachineAction("MenuView.zoomFitMachine",camera,frame.getEditorContext().getPlotter()));

        JCheckBoxMenuItem checkboxShowPenUpMoves = new JCheckBoxMenuItem(new ActionShowPenUpMoves());
        GFXPreferences.addListener((e)->checkboxShowPenUpMoves.setSelected ((boolean)e.getNewValue()));
        menu.add(checkboxShowPenUpMoves);

        return menu;
    }

    private JMenu createWindowsMenu() {
        JMenu menuWindows = new JMenu(Translator.get("MenuWindows"));
        // add each panel to the windows menu with a checkbox if the current panel is visible.
        int index=0;
        for(DockingPanel w : frame.getDockingPanels()) {
            DockableMenuItem item = new DockableMenuItem(w.getPersistentID(),w.getTabText());
            menuWindows.add(item);
            if(index<12) {
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + index, InputEvent.SHIFT_DOWN_MASK));
            }
            index++;
        }

        menuWindows.add(new JSeparator());
        menuWindows.add(new ResetLayoutAction("MenuWindows.ResetLayout",frame));

        return menuWindows;
    }

    private JMenu createHelpMenu() {
        JMenu menu = new JMenu(Translator.get("Help"));
        menu.setMnemonic('H');

        JMenuItem buttonViewLog = new JMenuItem(new NamedAbstractAction("ShowLog") {
            @Override
            public void actionPerformed(ActionEvent e) {
                runLogPanel();
            }
        });
        buttonViewLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, SHORTCUT_CTRL));//"ctrl L"
        menu.add(buttonViewLog);

        JMenuItem buttonLogFolder = new JMenuItem(new NamedAbstractAction("OpenLogFolder") {
            @Override
            public void actionPerformed(ActionEvent e) {
                openLogDirectory();
            }
        });
        buttonLogFolder.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-folder-16.png"))));
        menu.add(buttonLogFolder);

        JMenuItem buttonReportBug = createMenuItemBrowse("ReportBug","https://github.com/MarginallyClever/Makelangelo-Software/issues");
        buttonReportBug.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-bug-16.png"))));
        menu.add(buttonReportBug);

        JMenuItem buttonManual = createMenuItemBrowse("MenuManual", "https://mcr.dozuki.com/c/Makelangelo_3_and_5_Guide");
        buttonManual.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-open-book-16.png"))));
        menu.add(buttonManual);

        JMenuItem buttonForums = createMenuItemBrowse("MenuForums", "https://discord.gg/Q5TZFmB");
        buttonForums.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-discord-16.png"))));
        menu.add(buttonForums);

        JMenuItem buttonDonation = createMenuItemBrowse("MenuItemPayPalDonation", "https://www.marginallyclever.com/products/makelangelo-software/");
        menu.add(buttonDonation);

        JMenuItem buttonTranslate = createMenuItemBrowse("MenuItemTranslate", "https://crowdin.com/project/makelangelo");
        buttonTranslate.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-translate-16.png"))));
        menu.add(buttonTranslate);

        JMenuItem buttonCheckForUpdate = new JMenuItem(new NamedAbstractAction("MenuUpdate") {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.checkForUpdate(true);
            }
        });
        buttonCheckForUpdate.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-update-16.png"))));
        menu.add(buttonCheckForUpdate);

        menu.addSeparator();

        addAbout(menu);

        return menu;
    }

    private void addAbout(JMenu menu) {
        boolean added=false;
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                added=true;
                desktop.setAboutHandler((e) -> frame.onDialogAbout() );
            }
        }
        if(!added) {
            JMenuItem buttonAbout = new JMenuItem(new NamedAbstractAction("MenuAbout") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.onDialogAbout();
                }
            });
            menu.add(buttonAbout);
        }
    }

    private void openLogDirectory() {
        try {
            Desktop.getDesktop().open(Log.logDir);
        } catch (IOException e1) {
            logger.error("Can't open log folder", e1);
        }
    }

    public JMenuItem createMenuItemBrowse(String menuLabelAlreadyTranslated, String urlAsString) {
        JMenuItem jmi = new JMenuItem(new NamedAbstractAction(menuLabelAlreadyTranslated) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            java.awt.Desktop.getDesktop().browse(URI.create(urlAsString));
                        } else {
                            logger.error("Desktop.Action.BROWSE not supported. Can't open the browser to " + urlAsString);
                        }
                    } else {
                        logger.error("Desktop not supported. Can't open the browser to " + urlAsString);
                    }

                } catch (IOException ioe) {
                    logger.error("Can't open the browser to "+urlAsString, ioe);
                }
            }
        });
        jmi.setToolTipText(urlAsString);
        return jmi;
    }

    private void runLogPanel() {
        LogPanel.runAsDialog(SwingUtilities.getWindowAncestor(this));
    }

    private JMenu createGenerateMenu() {
        return createGeneratorMenuFromTree(TurtleGeneratorFactory.available);
    }

    private JMenu createGeneratorMenuFromTree(TurtleGeneratorFactory.TurtleGeneratorLeaf root) {
        JMenu menu = new JMenu(Translator.get(root.getName()));
        for (TurtleGeneratorFactory.TurtleGeneratorLeaf child : root.getChildren()) {
            if (child.getChildren().isEmpty()) {
                JMenuItem menuItem = new JMenuItem(new NamedAbstractAction(child.getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        runGeneratorDialog(child.getGenerator());
                    }
                });
                menu.add(menuItem);
            } else {
                JMenu subMenu = createGeneratorMenuFromTree(child);
                menu.add(subMenu);
            }
        }
        return menu;
    }

    private void runGeneratorDialog(TurtleGenerator turtleGenerator) {
        turtleGenerator.setPaper(frame.getEditorContext().getPaper());
        turtleGenerator.addListener(t->frame.getEditorContext().setTurtle(t));
        turtleGenerator.setTurtle(frame.getEditorContext().getTurtle());
        turtleGenerator.generate();

        if(turtleGenerator.getPanelElements().isEmpty()) {
            return;
        }

        frame.setMainTitle(turtleGenerator.getName());
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), turtleGenerator.getName());
        TurtleGeneratorPanel panel = new TurtleGeneratorPanel(turtleGenerator);
        dialog.add(panel);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setMinimumSize(new Dimension(300,300));
        dialog.pack();


        frame.enableMenuBar(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.enableMenuBar(true);
                frame.getEditorContext().getPaper().setRotationRef(0);
                logger.debug("Generation finished");
            }
        });

        dialog.setVisible(true);
    }

    private JMenu createToolsMenu() {
        JMenu menu = new JMenu(Translator.get("ArtPipeline"));
        menu.setMnemonic('T');

        try {
            PiCaptureAction pc = new PiCaptureAction();

            JButton bCapture = new JButton(new NamedAbstractAction("MenuCaptureImage") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pc.run(frame,frame.getEditorContext().getPaper());
                }
            });
            bCapture.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-camera-16.png"))));
            menu.add(bCapture);
            menu.addSeparator();
        } catch (FailedToRunRaspistillException e) {
            logger.debug("PiCaptureAction unavailable.");
        }

        menu.add(createActionMenuItem(new ResizeTurtleToPaperAction("ConvertImagePaperFit",frame.getEditorContext().getPaper(),false)));
        menu.add(createActionMenuItem(new ResizeTurtleToPaperAction("ConvertImagePaperFill",frame.getEditorContext().getPaper(),true)));
        menu.add(createActionMenuItem(new CenterTurtleToPaperAction("ConvertImagePaperCenter",frame.getEditorContext().getPaper())));

        menu.add(createMover("Translate","/com/marginallyclever/makelangelo/icons8-move-16.png",(e)->runTranslatePanel()));
        menu.add(createMover("Scale","/com/marginallyclever/makelangelo/icons8-resize-16.png",(e)->runScalePanel()));
        menu.add(createMover("Rotate","/com/marginallyclever/makelangelo/icons8-rotate-16.png",(e)->runRotatePanel()));
        menu.add(createMover("Crop","/com/marginallyclever/makelangelo/icons8-crop-16.png",(e)-> {
            CropTurtleAction act = new CropTurtleAction(frame.getEditorContext().getPaper());
            frame.getEditorContext().mutate(act::run);
        }));
        menu.addSeparator();

        var a4 = createModifier(new FlipTurtleAction("FlipV",1,-1),"/com/marginallyclever/makelangelo/icons8-flip-horizontal-16.png");
        a4.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, SHORTCUT_CTRL));//"ctrl V"
        menu.add(a4);

        var a5 = createModifier(new FlipTurtleAction("FlipH",-1,1),"/com/marginallyclever/makelangelo/icons8-flip-vertical-16.png");
        a5.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, SHORTCUT_CTRL));//"ctrl H"
        menu.add(a5);

        menu.addSeparator();

        var a1 = createModifier(new SimplifyTurtleAction(),"/com/marginallyclever/makelangelo/icons8-sort-16.png");
        a1.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, SHORTCUT_CTRL));//"ctrl Y"
        menu.add(a1);

        var a2 = createModifier(new ReorderTurtleAction(),"/com/marginallyclever/makelangelo/icons8-kangaroo-16.png");
        a2.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, SHORTCUT_CTRL));//"ctrl R"
        menu.add(a2);

        var a3 = createModifier(new InfillTurtleAction(), "/com/marginallyclever/makelangelo/icons8-fill-color-16.png");
        a3.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, SHORTCUT_CTRL));//"ctrl I"
        menu.add(a3);

        return menu;
    }

    private TurtleTool createModifier(TurtleTool action, String resource) {
        if(resource!=null) {
            action.putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource(resource))));
        }
        action.setContext(frame.getEditorContext());

        return action;
    }

    private JMenuItem createMover(String label, String resource, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(new NamedAbstractAction(label) {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.actionPerformed(e);
            }
        });
        menuItem.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(resource))));
        return menuItem;
    }

    private TurtleTool createActionMenuItem(TurtleTool action) {
        action.setContext(frame.getEditorContext());
        return action;
    }

    private void runRotatePanel() {
        RotateTurtlePanel.runAsDialog(SwingUtilities.getWindowAncestor(this), frame.getEditorContext());
    }

    private void runScalePanel() {
        ScaleTurtlePanel.runAsDialog(SwingUtilities.getWindowAncestor(this), frame.getEditorContext());
    }

    private void runTranslatePanel() {
        TranslateTurtlePanel.runAsDialog(SwingUtilities.getWindowAncestor(this), frame.getEditorContext());
    }

    private JMenu createRobotMenu() {
        JMenu menu = new JMenu(Translator.get("Robot"));
        menu.setMnemonic('k');

        JMenuItem bEstimate = new JMenuItem(new NamedAbstractAction("RobotMenu.GetTimeEstimate") {
            @Override
            public void actionPerformed(ActionEvent e) {
                estimateTime();
            }
        });
        bEstimate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, SHORTCUT_CTRL));
        bEstimate.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-stopwatch-16.png"))));
        menu.add(bEstimate);

        JMenuItem bSaveToSD = new JMenuItem(new NamedAbstractAction("RobotMenu.SaveGCode") {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.saveGCode();
            }
        });
        bSaveToSD.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, SHORTCUT_CTRL));
        bSaveToSD.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-export-16.png"))));
        menu.add(bSaveToSD);

        JMenuItem bOpenControls = new JMenuItem(new NamedAbstractAction("RobotMenu.OpenControls") {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPlotterControls();
            }
        });
        bOpenControls.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, SHORTCUT_CTRL));
        bOpenControls.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-joystick-16.png"))));
        menu.add(bOpenControls);

        return menu;
    }

    private JMenuItem createRenderStyleMenu() {
        JMenu menu = new JMenu(Translator.get("RobotMenu.RenderStyle"));

        ButtonGroup group = new ButtonGroup();

        var names = TurtleRenderFactory.getNames();
        for(int i=0;i<names.length;++i) {
            var renderer = TurtleRenderFactory.getTurtleRenderer(i);
            JRadioButtonMenuItem button = new JRadioButtonMenuItem(renderer.getTranslatedName());
            if (frame.getTurtleRenderer() == renderer) button.setSelected(true);
            final var index = i;
            button.addActionListener((e)-> onTurtleRenderChange(index));
            menu.add(button);
            group.add(button);
        }

        return menu;
    }

    /**
     * Called when the user selects a new render style.
     * @param index index into the {@link TurtleRenderFactory} array
     */
    private void onTurtleRenderChange(int index) {
        var renderer = TurtleRenderFactory.getTurtleRenderer(index);
        logger.debug("Switching to render style '{}'", renderer.getTranslatedName());
        frame.setTurtleRenderer(renderer);
    }

    private void estimateTime() {
        MarlinSimulation ms = new MarlinSimulation(frame.getEditorContext().getPlotter().getSettings());
        int estimatedSeconds = (int)Math.ceil(ms.getTimeEstimate(frame.getEditorContext().getTurtle()));
        String timeAsString = StringHelper.getElapsedTime(estimatedSeconds);
        String message = Translator.get("EstimatedTimeIs",new String[]{timeAsString});
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), message, Translator.get("RobotMenu.GetTimeEstimate"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void openPlotterControls() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), Translator.get("PlotterControls.Title"));
        dialog.setPreferredSize(new Dimension(PlotterControls.DIMENSION_PANEL_WIDTH, PlotterControls.DIMENSION_PANEL_HEIGHT));
        dialog.setMinimumSize(new Dimension(PlotterControls.DIMENSION_PANEL_WIDTH, PlotterControls.DIMENSION_PANEL_HEIGHT));
        PlotterControls plotterControls = new PlotterControls(frame.getEditorContext().getPlotter(),frame.getEditorContext().getTurtle(), dialog);
        dialog.add(plotterControls);
        dialog.pack();

        frame.enableMenuBar(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                plotterControls.onDialogClosing();
                frame.enableMenuBar(true);
            }
        });

        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private JMenu createSettingsMenu() {
        JMenu menu = new JMenu(Translator.get("MenuSettings"));
        menu.setMnemonic('S');

        JMenuItem bOpenPaperSettings = new JMenuItem(new NamedAbstractAction("OpenPaperSettings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPaperSettings();
            }
        });
        menu.add(bOpenPaperSettings);
        bOpenPaperSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, SHORTCUT_CTRL));

        JMenuItem bOpenPlotterSettings = new JMenuItem(new NamedAbstractAction("OpenPlotterSettings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPlotterSettings();
            }
        });
        bOpenPlotterSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, SHORTCUT_CTRL));
        menu.add(bOpenPlotterSettings);

        return menu;
    }

    private void openPlotterSettings() {
        PlotterSettingsManagerPanel plotterSettingsPanel = new PlotterSettingsManagerPanel(frame.getPlotterSettingsManager());
        plotterSettingsPanel.addListener(frame::onPlotterSettingsUpdate);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),Translator.get("PlotterSettingsPanel.Title"));
        dialog.add(plotterSettingsPanel);
        dialog.setMinimumSize(new Dimension(350,300));
        dialog.setResizable(true);
        dialog.pack();

        frame.enableMenuBar(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.onPlotterSettingsUpdate(frame.getPlotterSettingsManager().getLastSelectedProfile());
                frame.enableMenuBar(true);
            }
        });

        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private void openPaperSettings() {
        PaperSettingsPanel settings = new PaperSettingsPanel(frame.getEditorContext().getPaper());
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),Translator.get("PaperSettings.Title"));
        dialog.add(settings);
        dialog.setMinimumSize(new Dimension(300,300));
        dialog.pack();

        frame.enableMenuBar(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                settings.save();
                frame.enableMenuBar(true);
            }
        });

        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    public RecentFiles getRecentFiles() {
        return recentFiles;
    }
}
