package com.marginallyclever.makelangelo.preview;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.Generator_Border;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.Polargraph;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoordinatePreviewTest {
    @BeforeAll
    public static void init() {
        PreferencesHelper.start();
    }

    @Test
    public void tooltipDisplaysBothFramesAndRefreshesWhenSettingsChange() {
        PlotterSettings settings = settingsAtTopLeft();
        Camera camera = new Camera();
        camera.reshape(100, 100);
        RenderPanel panel = new RenderPanel();
        Paper paper = new Paper();
        paper.setPaperSize(200, 400, 10, -10);
        panel.setCamera(camera);
        panel.setPaper(paper);
        panel.setPlotterSettings(settings);
        panel.mouseMoved(new MouseEvent(panel, MouseEvent.MOUSE_MOVED, 0, 0, 60, 40, 0, false));

        String tip = panel.getToolTipText();
        assertTrue(tip.contains("#1565c0"));
        assertTrue(tip.contains("#c62828"));
        assertTrue(tip.contains("Application: 10.000, 10.000"));
        assertTrue(tip.contains("Machine: 100.000, -180.000"));

        settings.setInteger(PlotterSettings.STARTING_POS_INDEX, 8);
        panel.setPlotterSettings(settings);
        assertTrue(panel.getToolTipText().contains("Machine: -100.000, 220.000"));
    }

    @Test
    public void markerFollowsTheSelectedMachineOrigin() {
        Plotter plotter = new Plotter();
        PlotterSettings settings = settingsAtTopLeft();
        plotter.setSettings(settings);
        Paper paper = new Paper();
        paper.setPaperSize(200, 400, 10, -10);
        plotter.setPaper(paper);

        BufferedImage first = drawOrigin(plotter);
        assertEquals(Color.RED.getRGB(), first.getRGB(110, 410));
        assertNotEquals(Color.RED.getRGB(), first.getRGB(200, 220));

        settings.setInteger(PlotterSettings.STARTING_POS_INDEX, 8);
        BufferedImage second = drawOrigin(plotter);
        assertEquals(Color.RED.getRGB(), second.getRGB(310, 10));
        assertNotEquals(Color.RED.getRGB(), second.getRGB(110, 410));
    }

    @Test
    public void paperBorderAndImageSamplingStayInApplicationCoordinates() {
        Paper paper = new Paper();
        paper.setPaperSize(200, 100, 0, 0);
        paper.setPaperMargin(0.8);
        Generator_Border generator = new Generator_Border();
        generator.setPaper(paper);
        AtomicReference<Turtle> generated = new AtomicReference<>();
        generator.addListener(generated::set);
        generator.generate();
        String history = generated.get().generateHistory();
        assertEquals(new Rectangle2D.Double(-80, -40, 160, 80), generated.get().getBounds());

        TransformedImage image = new TransformedImage(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB));
        int sample = image.sample1x1(0, 0);
        PlotterSettings settings = settingsAtTopLeft();
        settings.setInteger(PlotterSettings.STARTING_POS_INDEX, 8);
        generator.generate();

        assertEquals(history, generated.get().generateHistory());
        assertEquals(sample, image.sample1x1(0, 0));
    }

    private static PlotterSettings settingsAtTopLeft() {
        PlotterSettings settings = new PlotterSettings();
        settings.setDouble(PlotterSettings.LIMIT_LEFT, -100);
        settings.setDouble(PlotterSettings.LIMIT_RIGHT, 100);
        settings.setDouble(PlotterSettings.LIMIT_BOTTOM, -200);
        settings.setDouble(PlotterSettings.LIMIT_TOP, 200);
        settings.setInteger(PlotterSettings.STARTING_POS_INDEX, 0);
        return settings;
    }

    private static BufferedImage drawOrigin(Plotter plotter) {
        BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.translate(200, 220);
            Polargraph.paintOrigin(graphics, plotter);
        } finally {
            graphics.dispose();
        }
        return image;
    }
}