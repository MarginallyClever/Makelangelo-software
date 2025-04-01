package com.marginallyclever.makelangelo.preview;

import com.marginallyclever.makelangelo.ActionShowPenUpMoves;
import com.marginallyclever.makelangelo.MakeleangeloRangeSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;
import com.marginallyclever.makelangelo.makeart.io.LoadFilePanel;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRenderer;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRendererFactory;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.MarlinSimulationVisualizer;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFacade;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFactory;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class PreviewPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(PreviewPanel.class);

    private final Plotter myPlotter = new Plotter();
    private final Paper myPaper = new Paper();

    private final OpenGLPanel openGLPanel = new OpenGLPanel();
    private final MakeleangeloRangeSlider rangeSlider = new MakeleangeloRangeSlider();
    private final Camera camera = new Camera();
    private final TurtleRenderFacade myTurtleRenderer = new TurtleRenderFacade();
    private PlotterRenderer myPlotterRenderer;
    private final PlotterSettingsManager plotterSettingsManager = new PlotterSettingsManager();

    public PreviewPanel() {
        super(new BorderLayout());
        setOpaque(true);

        openGLPanel.setCamera(camera);
        openGLPanel.addListener(myPaper);
        openGLPanel.addListener(myPlotter);
        openGLPanel.addListener(myTurtleRenderer);
        addPlotterRendererToPreviewPanel();

        add(openGLPanel, BorderLayout.CENTER);
        add(rangeSlider, BorderLayout.SOUTH);

        JToolBar toolBar = createToolBar();
        add(toolBar, BorderLayout.NORTH);

        myPlotter.setSettings(plotterSettingsManager.getLastSelectedProfile());
        myPaper.loadConfig();

        rangeSlider.addChangeListener(e->{
            myTurtleRenderer.setFirst(rangeSlider.getBottom());
            myTurtleRenderer.setLast(rangeSlider.getTop());
        });

        onPlotterSettingsUpdate(myPlotter.getSettings());


        camera.zoomToFit( Paper.DEFAULT_WIDTH, Paper.DEFAULT_HEIGHT);
    }

    private JToolBar createToolBar() {
        var bar = new JToolBar();

        var buttonZoomOut = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoom(1);
            }
        };
        buttonZoomOut.putValue(Action.SHORT_DESCRIPTION, Translator.get("MenuView.zoomOut"));
        buttonZoomOut.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
        buttonZoomOut.putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/actions/icons8-zoom-out-16.png"))));
        bar.add(buttonZoomOut);

        var buttonZoomIn = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoom(-1);
            }
        };
        buttonZoomIn.putValue(Action.SHORT_DESCRIPTION,Translator.get("MenuView.zoomIn"));
        buttonZoomIn.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));
        buttonZoomIn.putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/actions/icons8-zoom-in-16.png"))));
        bar.add(buttonZoomIn);

        var buttonZoomToFit = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                camera.zoomToFit(myPaper.getPaperWidth(),myPaper.getPaperHeight());
            }
        };
        buttonZoomToFit.putValue(Action.SHORT_DESCRIPTION,Translator.get("MenuView.zoomFit"));
        buttonZoomToFit.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
        buttonZoomToFit.putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/actions/icons8-zoom-to-fit-16.png"))));
        bar.add(buttonZoomToFit);

        var checkboxShowPenUpMoves = new JToggleButton(new ActionShowPenUpMoves());

        checkboxShowPenUpMoves.setSelected(GFXPreferences.getShowPenUp());
        GFXPreferences.addListener((e)->checkboxShowPenUpMoves.setSelected ((boolean)e.getNewValue()));

        bar.add(checkboxShowPenUpMoves);
        checkboxShowPenUpMoves.setName("");

        return bar;
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
        myTurtleRenderer.setShowTravel(GFXPreferences.getShowPenUp());
        // myTurtleRenderer.setDownColor() would be meaningless, the down color is stored in each Turtle.

        updatePlotterRenderer();

        if(openGLPanel != null) openGLPanel.repaint();
    }

    private void addPlotterRendererToPreviewPanel() {
        openGLPanel.addListener((gl2)->{
            if(myPlotterRenderer!=null) {
                myTurtleRenderer.setShowTravel(GFXPreferences.getShowPenUp());
                myPlotterRenderer.render(gl2, myPlotter);
            }
        });
    }

    public void stop() {
        openGLPanel.removeListener(myPlotter);
        myPlotter.getSettings().save();
        plotterSettingsManager.setLastSelectedProfile(myPlotter.getSettings().getUID());
        openGLPanel.stop();
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

    public void setTurtle(Turtle turtle) {
        myTurtleRenderer.setTurtle(turtle);
        rangeSlider.setLimits(0,turtle.history.size());
    }

    public int getRangeBottom() {
        return rangeSlider.getValue();
    }

    public int getRangeTop() {
        return rangeSlider.getUpperValue();
    }

    public void addListener(LoadFilePanel loader) {
        openGLPanel.addListener(loader);
    }

    public void removeListener(LoadFilePanel loader) {
        openGLPanel.removeListener(loader);
    }
}
