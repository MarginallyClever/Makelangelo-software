package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettings;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarlinPlotterInterfaceTest {

    @BeforeAll
    public static void init() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void onHearM114_OK() {
        Plotter plotter = new Plotter();
        plotter.setPos(23,45);
        MarlinPlotterInterface mpi = new MarlinPlotterInterface(plotter, new ChooseConnection());

        String message = "X:10.00 Y:-186.00 Z:200.00 Count X:72290 Y:72290 Z:32000";
        mpi.onHearM114(message);

        assertEquals(10, plotter.getPos().x);
        assertEquals(-186, plotter.getPos().y);
    }

    @Test
    public void onHearM114_KO() {
        Plotter plotter = new Plotter();
        plotter.setPos(23,45);
        MarlinPlotterInterface mpi = new MarlinPlotterInterface(plotter, new ChooseConnection());

        String message = "X:inva Y:-186.00 Z:200.00 Count X:72290 Y:72290 Z:32000";
        mpi.onHearM114(message);

        // original values
        assertEquals(23, plotter.getPos().x);
        assertEquals(45, plotter.getPos().y);
    }

    @Test
    public void onHearAcceleration_OK() {
        Plotter plotter = new Plotter();
        PlotterSettings ps = new PlotterSettings();
        ps.setAcceleration(42);
        plotter.setSettings(ps);
        MarlinPlotterInterface mpi = new MarlinPlotterInterface(plotter, new ChooseConnection());

        String message = "echo:  M201 X300.00 Y300.00 Z300.00";
        mpi.onHearAcceleration(message);

        assertEquals(300, plotter.getSettings().getMaxAcceleration());
    }

    @Test
    public void onHearAcceleration_KO() {
        Plotter plotter = new Plotter();
        PlotterSettings ps = new PlotterSettings();
        ps.setAcceleration(42);
        plotter.setSettings(ps);
        MarlinPlotterInterface mpi = new MarlinPlotterInterface(plotter, new ChooseConnection());

        String message = "echo:  M201 X300.00 Yinvalid";
        mpi.onHearAcceleration(message);

        // original values
        assertEquals(42, plotter.getSettings().getMaxAcceleration());
    }

    @Test
    public void onHearFeedrate_OK() {
        Plotter plotter = new Plotter();
        PlotterSettings ps = new PlotterSettings();
        ps.setDrawFeedRate(42);
        plotter.setSettings(ps);
        MarlinPlotterInterface mpi = new MarlinPlotterInterface(plotter, new ChooseConnection());

        String message = "echo:  M203 X200.00 Y200.00 Z200.00";
        mpi.onHearFeedrate(message);

        assertEquals(200, plotter.getSettings().getDrawFeedRate());
    }

    @Test
    public void onHearFeedrate_KO() {
        Plotter plotter = new Plotter();
        PlotterSettings ps = new PlotterSettings();
        ps.setDrawFeedRate(42);
        plotter.setSettings(ps);
        MarlinPlotterInterface mpi = new MarlinPlotterInterface(plotter, new ChooseConnection());

        String message = "echo:  M203 X200.00 Yinvalid";
        mpi.onHearFeedrate(message);

        // original values
        assertEquals(42, plotter.getSettings().getDrawFeedRate());
    }
}
