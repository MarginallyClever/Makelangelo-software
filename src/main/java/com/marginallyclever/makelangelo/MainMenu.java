package com.marginallyclever.makelangelo;

import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.convenience.log.LogPanel;
import com.marginallyclever.makelangelo.firmwareuploader.FirmwareUploaderPanel;
import com.marginallyclever.makelangelo.makeart.TurtleModifierAction;
import com.marginallyclever.makelangelo.makeart.io.OpenFileChooser;
import com.marginallyclever.makelangelo.makeart.tools.*;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGeneratorFactory;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGeneratorPanel;
import com.marginallyclever.makelangelo.makelangelosettingspanel.GFXPreferences;
import com.marginallyclever.makelangelo.makelangelosettingspanel.MakelangeloSettingPanel;
import com.marginallyclever.makelangelo.paper.PaperSettings;
import com.marginallyclever.makelangelo.plotter.PiCaptureAction;
import com.marginallyclever.makelangelo.plotter.marlinsimulation.MarlinSimulation;
import com.marginallyclever.makelangelo.plotter.plottercontrols.PlotterControls;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManagerPanel;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFactory;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;

public class MainMenu extends JMenuBar {
    private static final Logger logger = LoggerFactory.getLogger(MainMenu.class);
    private static int SHORTCUT_CTRL = InputEvent.CTRL_DOWN_MASK;
    private static int SHORTCUT_ALT = InputEvent.ALT_DOWN_MASK;
    private final Makelangelo app;
    private final SaveDialog saveDialog = new SaveDialog();
    private RecentFiles recentFiles;
    private final MakelangeloSettingPanel myPreferencesPanel = new MakelangeloSettingPanel();
    private boolean isMacOS = false;

    public MainMenu(Makelangelo app) {
        super();
        this.app = app;
        setSystemLookAndFeelForMacos();
        add(createFileMenu());
        add(createSettingsMenu());
        add(createGenerateMenu());
        add(createToolsMenu());
        add(createViewMenu());
        add(createRobotMenu());
        add(createHelpMenu());
        updateUI();

    }

    private void setSystemLookAndFeelForMacos() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if ((os.contains("mac")) || (os.contains("darwin"))) {
            isMacOS=true;
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            SHORTCUT_CTRL = InputEvent.META_DOWN_MASK;
            SHORTCUT_ALT = InputEvent.META_DOWN_MASK;
        }
    }

    private JMenu createFileMenu() {
        JMenu menu = new JMenu(Translator.get("MenuMakelangelo"));
        menu.setMnemonic('f');

        JMenuItem buttonNewFile = new JMenuItem(Translator.get("MenuNewFile"));
        buttonNewFile.addActionListener((e) -> newFile());
        buttonNewFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, SHORTCUT_CTRL));//"ctrl N"
        buttonNewFile.setIcon(new UnicodeIcon("\uD83C\uDF31"));
        menu.add(buttonNewFile);

        JMenuItem buttonOpenFile = new JMenuItem(Translator.get("MenuOpenFile"));
        buttonOpenFile.addActionListener((e) -> openLoadFile());
        buttonOpenFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, SHORTCUT_CTRL));//"ctrl O"
        buttonOpenFile.setIcon(new UnicodeIcon("\uD83D\uDDC1"));
        menu.add(buttonOpenFile);

        recentFiles = new RecentFiles(Translator.get("MenuReopenFile"));
        recentFiles.addSubmenuListener((e)-> app.openFile(((JMenuItem)e.getSource()).getText()));
        menu.add(recentFiles);

        JMenuItem buttonSaveFile = new JMenuItem(Translator.get("MenuSaveFile"));
        buttonSaveFile.addActionListener((e) -> saveFile());
        buttonSaveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, SHORTCUT_CTRL));//"ctrl S"
        buttonSaveFile.setIcon(new UnicodeIcon("\uD83D\uDCBE"));
        menu.add(buttonSaveFile);

        menu.addSeparator();

        JMenuItem buttonAdjustPreferences = new JMenuItem(Translator.get("MenuPreferences"));
        buttonAdjustPreferences.addActionListener((e)-> myPreferencesPanel.run(SwingUtilities.getWindowAncestor(this)));
        if (isMacOS) {
            buttonAdjustPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, SHORTCUT_CTRL));//"cmd ,"
        } else {
            buttonAdjustPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, SHORTCUT_ALT));//"alt P"
        }
        buttonAdjustPreferences.setIcon(new UnicodeIcon("⚙"));
        menu.add(buttonAdjustPreferences);

        JMenuItem buttonFirmwareUpdate = new JMenuItem(Translator.get("FirmwareUpdate"));
        buttonFirmwareUpdate.addActionListener((e) -> runFirmwareUpdate());
        buttonFirmwareUpdate.setIcon(new UnicodeIcon("⬆"));
        menu.add(buttonFirmwareUpdate);

        if (!isMacOS) {
            menu.addSeparator();

            JMenuItem buttonExit = new JMenuItem(Translator.get("MenuQuit"));
            buttonExit.addActionListener((e) -> {
                WindowEvent windowClosing = new WindowEvent(SwingUtilities.getWindowAncestor(this), WindowEvent.WINDOW_CLOSING);
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(windowClosing);
            });
            buttonExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, SHORTCUT_CTRL));//"ctrl Q"
            buttonExit.setMnemonic('Q');
            menu.add(buttonExit);
        }
        return menu;
    }

    private void newFile() {
        app.setTurtle(new Turtle());
        app.setMainTitle("");
    }

    public void openLoadFile() {
        logger.debug("Open file...");

        OpenFileChooser openFileChooser = new OpenFileChooser(SwingUtilities.getWindowAncestor(this));
        openFileChooser.setOpenListener(app::openFile);
        openFileChooser.chooseFile();
    }

    private JMenu createViewMenu() {
        JMenu menu = new JMenu(Translator.get("MenuView"));
        menu.setMnemonic('V');

        JMenuItem buttonZoomOut = new JMenuItem(Translator.get("MenuView.zoomOut"), KeyEvent.VK_MINUS);
        buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, SHORTCUT_CTRL));
        buttonZoomOut.addActionListener((e) -> app.getCamera().zoom(1));
        buttonZoomOut.setIcon(new UnicodeIcon("\uD83D\uDD0D-"));
        menu.add(buttonZoomOut);

        JMenuItem buttonZoomIn = new JMenuItem(Translator.get("MenuView.zoomIn"), KeyEvent.VK_EQUALS);
        buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, SHORTCUT_CTRL));
        buttonZoomIn.addActionListener((e) -> app.getCamera().zoom(-1));
        buttonZoomIn.setIcon(new UnicodeIcon("\uD83D\uDD0D+"));
        menu.add(buttonZoomIn);

        JMenuItem buttonZoomToFit = new JMenuItem(Translator.get("MenuView.zoomFit"), KeyEvent.VK_0);
        buttonZoomToFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, SHORTCUT_CTRL));
        buttonZoomToFit.addActionListener((e) -> app.getCamera().zoomToFit(app.getPaper().getPaperWidth(),app.getPaper().getPaperHeight()));
        menu.add(buttonZoomToFit);

        JCheckBoxMenuItem checkboxShowPenUpMoves = new JCheckBoxMenuItem(Translator.get("GFXPreferences.showPenUp"), GFXPreferences.getShowPenUp());
        checkboxShowPenUpMoves.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, SHORTCUT_CTRL));//"ctrl M"
        checkboxShowPenUpMoves.addActionListener((e) -> {
            boolean b = GFXPreferences.getShowPenUp();
            GFXPreferences.setShowPenUp(!b);
        });
        GFXPreferences.addListener((e)->{
            checkboxShowPenUpMoves.setSelected ((boolean)e.getNewValue());
        });
        checkboxShowPenUpMoves.setIcon(new UnicodeIcon("\uD83D\uDC41"));
        menu.add(checkboxShowPenUpMoves);

        menu.add(createRenderStyleMenu());

        return menu;
    }

    private JMenu createHelpMenu() {
        JMenu menu = new JMenu(Translator.get("Help"));
        menu.setMnemonic('H');

        JMenuItem buttonViewLog = new JMenuItem(Translator.get("ShowLog"));
        buttonViewLog.addActionListener((e) -> runLogPanel());
        buttonViewLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, SHORTCUT_CTRL));//"ctrl L"
        menu.add(buttonViewLog);

        JMenuItem buttonForums = createMenuItemBrowse(Translator.get("MenuForums"), "https://discord.gg/Q5TZFmB");
        menu.add(buttonForums);

        JMenuItem buttonDonation = createMenuItemBrowse(Translator.get("MenuItemPayPalDonation"), "https://www.marginallyclever.com/products/makelangelo-software/");
        menu.add(buttonDonation);

        JMenuItem buttonCheckForUpdate = new JMenuItem(Translator.get("MenuUpdate"));
        buttonCheckForUpdate.addActionListener((e) -> app.checkForUpdate(true));
        menu.add(buttonCheckForUpdate);

        menu.addSeparator();

        if (!isMacOS) {
            JMenuItem buttonAbout = new JMenuItem(Translator.get("MenuAbout"));
            buttonAbout.addActionListener((e) -> app.onDialogAbout());
            menu.add(buttonAbout);
        }

        return menu;
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

    private void saveFile() {
        logger.debug("Saving vector file...");
        try {
            saveDialog.run(app.getTurtle(), SwingUtilities.getWindowAncestor(this));
        } catch(Exception e) {
            logger.error("Error while saving the vector file", e);
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), Translator.get("SaveError") + e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runFirmwareUpdate() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),"Firmware Update");
        dialog.add(new FirmwareUploaderPanel());
        dialog.pack();
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        app.enableMenuBar(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                app.enableMenuBar(true);
            }
        });

        dialog.setVisible(true);
    }

    private JMenu createGenerateMenu() {
        JMenu menu = new JMenu(Translator.get("MenuGenerate"));
        menu.setMnemonic('A');
        for( TurtleGenerator ici : TurtleGeneratorFactory.available ) {
            JMenuItem mi = new JMenuItem(ici.getName());
            mi.addActionListener((e) -> runGeneratorDialog(ici));
            menu.add(mi);
        }

        return menu;
    }

    private void runGeneratorDialog(TurtleGenerator turtleGenerator) {
        turtleGenerator.setPaper(app.getPaper());
        turtleGenerator.addListener(app::setTurtle);
        turtleGenerator.generate();

        if(turtleGenerator.getPanelElements().isEmpty()) {
            return;
        }

        app.setMainTitle(turtleGenerator.getName());
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), turtleGenerator.getName());
        TurtleGeneratorPanel panel = new TurtleGeneratorPanel(turtleGenerator);
        dialog.add(panel);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setMinimumSize(new Dimension(300,300));
        dialog.pack();


        app.enableMenuBar(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                app.enableMenuBar(true);
                app.getPaper().setRotationRef(0);
                logger.debug("Generation finished");
            }
        });

        dialog.setVisible(true);
    }

    private JMenu createToolsMenu() {
        JMenu menu = new JMenu(Translator.get("Art Pipeline"));
        menu.setMnemonic('T');

        try {
            PiCaptureAction pc = new PiCaptureAction();

            JButton bCapture = new JButton(Translator.get("MenuCaptureImage"));
            bCapture.addActionListener((e)-> pc.run((Frame)SwingUtilities.getWindowAncestor(this),app.getPaper()));
            bCapture.setIcon(new UnicodeIcon("📷"));
            menu.add(bCapture);
            menu.addSeparator();
        } catch (FailedToRunRaspistillException e) {
            logger.debug("PiCaptureAction unavailable.");
        }

        TurtleModifierAction a6 = new ResizeTurtleToPaperAction(app.getPaper(),false,Translator.get("ConvertImagePaperFit"));
        TurtleModifierAction a7 = new ResizeTurtleToPaperAction(app.getPaper(),true,Translator.get("ConvertImagePaperFill"));
        a6.setSource(app);		a6.addModifierListener(app::setTurtle);		menu.add(a6);
        a7.setSource(app);		a7.addModifierListener(app::setTurtle);		menu.add(a7);

        JMenuItem translate = new JMenuItem(Translator.get("Translate"));
        menu.add(translate);
        translate.addActionListener((e) -> runTranslatePanel());

        JMenuItem scale = new JMenuItem(Translator.get("Scale"));
        menu.add(scale);
        scale.addActionListener((e) -> runScalePanel());

        JMenuItem rotate = new JMenuItem(Translator.get("Rotate"));
        rotate.setIcon(new UnicodeIcon("↻"));
        menu.add(rotate);
        rotate.addActionListener((e) -> runRotatePanel());
        menu.addSeparator();

        TurtleModifierAction a4 = new FlipTurtleAction(1,-1,Translator.get("FlipH"));
        a4.putValue(Action.SMALL_ICON, new UnicodeIcon("↕"));
        a4.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, SHORTCUT_CTRL));//"ctrl H"
        TurtleModifierAction a5 = new FlipTurtleAction(-1,1,Translator.get("FlipV"));
        a5.putValue(Action.SMALL_ICON, new UnicodeIcon("↔"));
        a5.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, SHORTCUT_CTRL));//"ctrl F"
        a4.setSource(app);		a4.addModifierListener(app::setTurtle);		menu.add(a4);
        a5.setSource(app);		a5.addModifierListener(app::setTurtle);		menu.add(a5);

        menu.addSeparator();

        TurtleModifierAction a1 = new SimplifyTurtleAction();
        a1.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, SHORTCUT_CTRL));//"ctrl Y"
        TurtleModifierAction a2 = new ReorderTurtleAction();
        a2.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, SHORTCUT_CTRL));//"ctrl R"
        TurtleModifierAction a3 = new InfillTurtleAction();
        a3.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, SHORTCUT_CTRL));//"ctrl I"
        a1.setSource(app);		a1.addModifierListener(app::setTurtle);		menu.add(a1);
        a2.setSource(app);		a2.addModifierListener(app::setTurtle);		menu.add(a2);
        a3.setSource(app);		a3.addModifierListener(app::setTurtle);		menu.add(a3);

        return menu;
    }

    private void runRotatePanel() {
        RotateTurtlePanel.runAsDialog(SwingUtilities.getWindowAncestor(this), app.getTurtle());
    }

    private void runScalePanel() {
        ScaleTurtlePanel.runAsDialog(SwingUtilities.getWindowAncestor(this), app.getTurtle());
    }

    private void runTranslatePanel() {
        TranslateTurtlePanel.runAsDialog(SwingUtilities.getWindowAncestor(this), app.getTurtle());
    }
  
    private JMenu createRobotMenu() {
        JMenu menu = new JMenu(Translator.get("Robot"));
        menu.setMnemonic('k');

        JMenuItem bEstimate = new JMenuItem(Translator.get("RobotMenu.GetTimeEstimate"));
        bEstimate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, SHORTCUT_CTRL));//"ctrl E"
        bEstimate.addActionListener((e)-> estimateTime());
        menu.add(bEstimate);

        JMenuItem bSaveToSD = new JMenuItem(Translator.get("RobotMenu.SaveGCode"));
        bSaveToSD.addActionListener((e)-> app.saveGCode());
        bSaveToSD.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, SHORTCUT_CTRL));//"ctrl G"
        menu.add(bSaveToSD);

        JMenuItem bOpenControls = new JMenuItem(Translator.get("RobotMenu.OpenControls"));
        bOpenControls.addActionListener((e)-> openPlotterControls());
        bOpenControls.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, SHORTCUT_CTRL));//"ctrl C"
        bOpenControls.setIcon(new UnicodeIcon("\uD83D\uDD79"));
        menu.add(bOpenControls);

        return menu;
    }

    private JMenuItem createRenderStyleMenu() {
        JMenu menu = new JMenu(Translator.get("RobotMenu.RenderStyle"));

        ButtonGroup group = new ButtonGroup();

        Arrays.stream(TurtleRenderFactory.values())
                .forEach(iter -> {
                    TurtleRenderer renderer = iter.getTurtleRenderer();
                    String name = iter.getName();
                    JRadioButtonMenuItem button = new JRadioButtonMenuItem(iter.getTranslatedText());
                    if (app.getTurtleRenderer() == renderer) button.setSelected(true);
                    button.addActionListener((e)-> onTurtleRenderChange(name));
                    menu.add(button);
                    group.add(button);
                });

        return menu;
    }

    private void onTurtleRenderChange(String name) {
        logger.debug("Switching to render style '{}'", name);
        TurtleRenderer renderer = TurtleRenderFactory.findByName(name).getTurtleRenderer();
        app.setTurtleRenderer(renderer);
    }

    private void estimateTime() {
        MarlinSimulation ms = new MarlinSimulation(app.getPlotter().getSettings());
        int estimatedSeconds = (int)Math.ceil(ms.getTimeEstimate(app.getTurtle()));
        String timeAsString = StringHelper.getElapsedTime(estimatedSeconds);
        String message = Translator.get("EstimatedTimeIs",new String[]{timeAsString});
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), message, Translator.get("RobotMenu.GetTimeEstimate"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void openPlotterControls() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), Translator.get("PlotterControls.Title"));
        dialog.setPreferredSize(new Dimension(PlotterControls.DIMENSION_PANEL_WIDTH, PlotterControls.DIMENSION_PANEL_HEIGHT));
        dialog.setMinimumSize(new Dimension(PlotterControls.DIMENSION_PANEL_WIDTH, PlotterControls.DIMENSION_PANEL_HEIGHT));
        PlotterControls plotterControls = new PlotterControls(app.getPlotter(),app.getTurtle(), dialog);
        dialog.add(plotterControls);
        dialog.pack();

        app.enableMenuBar(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                plotterControls.onDialogClosing();
                app.enableMenuBar(true);
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
        PlotterSettingsManagerPanel plotterSettingsPanel = new PlotterSettingsManagerPanel(app.getPlotterSettingsManager());
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),Translator.get("PlotterSettingsPanel.Title"));
        dialog.add(plotterSettingsPanel);
        dialog.setMinimumSize(new Dimension(350,300));
        dialog.setResizable(false);
        dialog.pack();

        app.enableMenuBar(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                app.onPlotterSettingsUpdate(app.getPlotterSettingsManager().getLastSelectedProfile());
                app.enableMenuBar(true);
            }
        });

        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private void openPaperSettings() {
        PaperSettings settings = new PaperSettings(app.getPaper());
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),Translator.get("PaperSettings.Title"));
        dialog.add(settings);
        dialog.setMinimumSize(new Dimension(300,300));
        dialog.pack();

        app.enableMenuBar(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                settings.save();
                app.enableMenuBar(true);
            }
        });

        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    public RecentFiles getRecentFiles() {
        return recentFiles;
    }
}
