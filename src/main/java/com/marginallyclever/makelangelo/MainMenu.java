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
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

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

        menu.add(new NewFileAction(Translator.get("MenuNewFile"),frame));
        menu.add(new LoadFileAction(Translator.get("MenuOpenFile"),frame,recentFiles));
        menu.add(recentFiles);
        menu.add(new ImportFileAction(Translator.get("MenuImportFile"),frame));
        menu.add(new SaveFileAction(Translator.get("MenuSaveFile"),frame));
        menu.addSeparator();
        menu.add(new AdjustPreferencesAction(Translator.get("ApplicationSettings.title"),frame));
        menu.add(new UpdateFirmwareAction(Translator.get("FirmwareUpdate"),frame));

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
            menu.add(new QuitAction(Translator.get("MenuQuit"),frame));
        }
    }

    private JMenu createViewMenu() {
        JMenu menu = new JMenu(Translator.get("MenuView"));
        menu.setMnemonic('V');

        menu.add(createRenderStyleMenu());
        menu.addSeparator();

        var app = frame.getPreviewPanel();
        var camera = app.getCamera();
        var paper = app.getPaper();

        menu.add(new ZoomOutAction(Translator.get("MenuView.zoomOut"),camera));
        menu.add(new ZoomInAction(Translator.get("MenuView.zoomIn"),camera));
        menu.add(new ZoomToFitAction(Translator.get("MenuView.zoomFit"),camera,paper));

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
        menuWindows.add(new ResetLayoutAction(Translator.get("ResetLayout"),frame));

        return menuWindows;
    }

    private JMenu createHelpMenu() {
        JMenu menu = new JMenu(Translator.get("Help"));
        menu.setMnemonic('H');

        JMenuItem buttonViewLog = new JMenuItem(Translator.get("ShowLog"));
        buttonViewLog.addActionListener((e) -> runLogPanel());
        buttonViewLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, SHORTCUT_CTRL));//"ctrl L"
        menu.add(buttonViewLog);

        JMenuItem buttonLogFolder = new JMenuItem(Translator.get("OpenLogFolder"));
        buttonLogFolder.addActionListener((e) -> openLogDirectory());
        buttonLogFolder.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-folder-16.png"))));
        menu.add(buttonLogFolder);

        JMenuItem buttonReportBug = createMenuItemBrowse(Translator.get("ReportBug"),"https://github.com/MarginallyClever/Makelangelo-Software/issues");
        buttonReportBug.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-bug-16.png"))));
        menu.add(buttonReportBug);

        JMenuItem buttonManual = createMenuItemBrowse(Translator.get("MenuManual"), "https://mcr.dozuki.com/c/Makelangelo_3_and_5_Guide");
        buttonManual.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-open-book-16.png"))));
        menu.add(buttonManual);

        JMenuItem buttonForums = createMenuItemBrowse(Translator.get("MenuForums"), "https://discord.gg/Q5TZFmB");
        buttonForums.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-discord-16.png"))));
        menu.add(buttonForums);

        JMenuItem buttonDonation = createMenuItemBrowse(Translator.get("MenuItemPayPalDonation"), "https://www.marginallyclever.com/products/makelangelo-software/");
        menu.add(buttonDonation);

        JMenuItem buttonTranslate = createMenuItemBrowse(Translator.get("MenuItemTranslate"), "https://crowdin.com/project/makelangelo");
        buttonTranslate.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-translate-16.png"))));
        menu.add(buttonTranslate);

        JMenuItem buttonCheckForUpdate = new JMenuItem(Translator.get("MenuUpdate"));
        buttonCheckForUpdate.addActionListener((e) -> frame.checkForUpdate(true));
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
            JMenuItem buttonAbout = new JMenuItem(Translator.get("MenuAbout"));
            buttonAbout.addActionListener((e) -> frame.onDialogAbout() );
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
        JMenuItem jmi = new JMenuItem(menuLabelAlreadyTranslated);
        jmi.setToolTipText(urlAsString);
        jmi.addActionListener((e) -> {
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
        });
        return jmi;
    }

    private void runLogPanel() {
        LogPanel.runAsDialog(SwingUtilities.getWindowAncestor(this));
    }

    private JMenu createGenerateMenu() {
        return createGeneratorMenuFromTree(TurtleGeneratorFactory.available);
    }

    private JMenu createGeneratorMenuFromTree(TurtleGeneratorFactory.TurtleGeneratorNode root) {
        JMenu menu = new JMenu(root.getName());
        for (TurtleGeneratorFactory.TurtleGeneratorNode child : root.getChildren()) {
            if (child.getChildren().isEmpty()) {
                JMenuItem menuItem = new JMenuItem(child.getName());
                menu.add(menuItem);
                menuItem.addActionListener((e) -> runGeneratorDialog(child.getGenerator()));
            } else {
                JMenu subMenu = createGeneratorMenuFromTree(child);
                menu.add(subMenu);
            }
        }
        return menu;
    }

    private void runGeneratorDialog(TurtleGenerator turtleGenerator) {
        turtleGenerator.setPaper(frame.getPaper());
        turtleGenerator.addListener(frame::setTurtle);
        turtleGenerator.setTurtle(frame.getTurtle());
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
                frame.getPaper().setRotationRef(0);
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

            JButton bCapture = new JButton(Translator.get("MenuCaptureImage"));
            bCapture.addActionListener((e)-> pc.run(frame,frame.getPaper()));
            bCapture.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-camera-16.png"))));
            menu.add(bCapture);
            menu.addSeparator();
        } catch (FailedToRunRaspistillException e) {
            logger.debug("PiCaptureAction unavailable.");
        }

        menu.add(createActionMenuItem(new ResizeTurtleToPaperAction(frame.getPaper(),false,Translator.get("ConvertImagePaperFit"))));
        menu.add(createActionMenuItem(new ResizeTurtleToPaperAction(frame.getPaper(),true,Translator.get("ConvertImagePaperFill"))));
        menu.add(createActionMenuItem(new CenterTurtleToPaperAction(Translator.get("ConvertImagePaperCenter"))));

        menu.add(createMover(Translator.get("Translate"),"/com/marginallyclever/makelangelo/icons8-move-16.png",(e)->runTranslatePanel()));
        menu.add(createMover(Translator.get("Scale"),"/com/marginallyclever/makelangelo/icons8-resize-16.png",(e)->runScalePanel()));
        menu.add(createMover(Translator.get("Rotate"),"/com/marginallyclever/makelangelo/icons8-rotate-16.png",(e)->runRotatePanel()));

        menu.addSeparator();

        var a4 = new FlipTurtleAction(1,-1,Translator.get("FlipV"));
        a4.putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-flip-horizontal-16.png"))));
        a4.setSource(frame);
        a4.addModifierListener(frame::setTurtle);
        menu.add(a4);

        var a5 = new FlipTurtleAction(-1,1,Translator.get("FlipH"));
        a5.putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-flip-vertical-16.png"))));
        a5.setSource(frame);
        a5.addModifierListener(frame::setTurtle);
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
        action.setSource(frame);
        action.addModifierListener(frame::setTurtle);

        return action;
    }

    private JMenuItem createMover(String label, String resource, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(label);
        menuItem.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(resource))));
        menuItem.addActionListener(listener);
        return menuItem;
    }

    private TurtleTool createActionMenuItem(TurtleTool action) {
        action.setSource(frame);
        action.addModifierListener(frame::setTurtle);
        return action;
    }

    private void runRotatePanel() {
        RotateTurtlePanel.runAsDialog(SwingUtilities.getWindowAncestor(this), frame.getTurtle());
    }

    private void runScalePanel() {
        ScaleTurtlePanel.runAsDialog(SwingUtilities.getWindowAncestor(this), frame.getTurtle());
    }

    private void runTranslatePanel() {
        TranslateTurtlePanel.runAsDialog(SwingUtilities.getWindowAncestor(this), frame.getTurtle());
    }
  
    private JMenu createRobotMenu() {
        JMenu menu = new JMenu(Translator.get("Robot"));
        menu.setMnemonic('k');

        JMenuItem bEstimate = new JMenuItem(Translator.get("RobotMenu.GetTimeEstimate"));
        bEstimate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, SHORTCUT_CTRL));
        bEstimate.addActionListener((e)-> estimateTime());
        bEstimate.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-stopwatch-16.png"))));
        menu.add(bEstimate);

        JMenuItem bSaveToSD = new JMenuItem(Translator.get("RobotMenu.SaveGCode"));
        bSaveToSD.addActionListener((e)-> frame.saveGCode());
        bSaveToSD.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, SHORTCUT_CTRL));
        bSaveToSD.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-export-16.png"))));
        menu.add(bSaveToSD);

        JMenuItem bOpenControls = new JMenuItem(Translator.get("RobotMenu.OpenControls"));
        bOpenControls.addActionListener((e)-> openPlotterControls());
        bOpenControls.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, SHORTCUT_CTRL));
        bOpenControls.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-joystick-16.png"))));
        menu.add(bOpenControls);

        return menu;
    }

    private JMenuItem createRenderStyleMenu() {
        JMenu menu = new JMenu(Translator.get("RobotMenu.RenderStyle"));

        ButtonGroup group = new ButtonGroup();

        Arrays.stream(TurtleRenderFactory.values()).forEach(iter -> {
                TurtleRenderer renderer = iter.getTurtleRenderer();
                String name = iter.getName();
                JRadioButtonMenuItem button = new JRadioButtonMenuItem(renderer.getTranslatedName());
                if (frame.getTurtleRenderer() == renderer) button.setSelected(true);
                button.addActionListener((e)-> onTurtleRenderChange(name));
                menu.add(button);
                group.add(button);
        });

        return menu;
    }

    private void onTurtleRenderChange(String name) {
        logger.debug("Switching to render style '{}'", name);
        TurtleRenderer renderer = TurtleRenderFactory.findByName(name).getTurtleRenderer();
        frame.setTurtleRenderer(renderer);
    }

    private void estimateTime() {
        MarlinSimulation ms = new MarlinSimulation(frame.getPlotter().getSettings());
        int estimatedSeconds = (int)Math.ceil(ms.getTimeEstimate(frame.getTurtle()));
        String timeAsString = StringHelper.getElapsedTime(estimatedSeconds);
        String message = Translator.get("EstimatedTimeIs",new String[]{timeAsString});
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), message, Translator.get("RobotMenu.GetTimeEstimate"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void openPlotterControls() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), Translator.get("PlotterControls.Title"));
        dialog.setPreferredSize(new Dimension(PlotterControls.DIMENSION_PANEL_WIDTH, PlotterControls.DIMENSION_PANEL_HEIGHT));
        dialog.setMinimumSize(new Dimension(PlotterControls.DIMENSION_PANEL_WIDTH, PlotterControls.DIMENSION_PANEL_HEIGHT));
        PlotterControls plotterControls = new PlotterControls(frame.getPlotter(),frame.getTurtle(), dialog);
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

        JMenuItem bOpenPaperSettings = new JMenuItem(Translator.get("OpenPaperSettings"));
        bOpenPaperSettings.addActionListener((e)-> openPaperSettings());
        menu.add(bOpenPaperSettings);

        JMenuItem bOpenPlotterSettings = new JMenuItem(Translator.get("OpenPlotterSettings"));
        bOpenPlotterSettings.addActionListener((e)-> openPlotterSettings());
        bOpenPlotterSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, SHORTCUT_CTRL));//"ctrl P"
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
        PaperSettingsPanel settings = new PaperSettingsPanel(frame.getPaper());
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
