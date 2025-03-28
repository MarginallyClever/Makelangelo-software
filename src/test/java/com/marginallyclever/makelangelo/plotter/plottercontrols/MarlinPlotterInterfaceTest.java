package com.marginallyclever.makelangelo.plotter.plottercontrols;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Assertions;
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
        MarlinPlotterPanel mpi = new MarlinPlotterPanel(plotter, new ChooseConnection());

        String message = "X:10.00 Y:-186.00 Z:200.00 Count X:72290 Y:72290 Z:32000";
        mpi.onHearM114(message);

        assertEquals(10, plotter.getPos().x);
        assertEquals(-186, plotter.getPos().y);
    }

    @Test
    public void onHearM114_KO() {
        Plotter plotter = new Plotter();
        plotter.setPos(23,45);
        MarlinPlotterPanel mpi = new MarlinPlotterPanel(plotter, new ChooseConnection());

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
        ps.setDouble(PlotterSettings.MAX_ACCELERATION,42);
        plotter.setSettings(ps);
        MarlinPlotterPanel mpi = new MarlinPlotterPanel(plotter, new ChooseConnection());

        String message = "echo:  M201 X300.00 Y300.00 Z300.00";
        mpi.onHearAcceleration(message);

        assertEquals(300, plotter.getSettings().getDouble(PlotterSettings.MAX_ACCELERATION));
    }

    @Test
    public void onHearAcceleration_KO() {
        Plotter plotter = new Plotter();
        PlotterSettings ps = new PlotterSettings();
        ps.setDouble(PlotterSettings.MAX_ACCELERATION,42);
        plotter.setSettings(ps);
        MarlinPlotterPanel mpi = new MarlinPlotterPanel(plotter, new ChooseConnection());

        String message = "echo:  M201 X300.00 Yinvalid";
        mpi.onHearAcceleration(message);

        // original values
        assertEquals(42, plotter.getSettings().getDouble(PlotterSettings.MAX_ACCELERATION));
    }

    @Test
    public void onHearFeedrate_OK() {
        Plotter plotter = new Plotter();
        PlotterSettings ps = new PlotterSettings();
        ps.setDouble(PlotterSettings.FEED_RATE_DRAW,42);
        plotter.setSettings(ps);
        MarlinPlotterPanel mpi = new MarlinPlotterPanel(plotter, new ChooseConnection());

        String message = "echo:  M203 X200.00 Y200.00 Z200.00";
        mpi.onHearFeedrate(message);

        assertEquals(200, plotter.getSettings().getDouble(PlotterSettings.FEED_RATE_DRAW));
    }

    @Test
    public void onHearFeedrate_KO() {
        Plotter plotter = new Plotter();
        PlotterSettings ps = new PlotterSettings();
        ps.setDouble(PlotterSettings.FEED_RATE_DRAW,42);
        plotter.setSettings(ps);
        MarlinPlotterPanel mpi = new MarlinPlotterPanel(plotter, new ChooseConnection());

        String message = "echo:  M203 X200.00 Yinvalid";
        mpi.onHearFeedrate(message);

        // original values
        assertEquals(42, plotter.getSettings().getDouble(PlotterSettings.FEED_RATE_DRAW));
    }

    @Test
    public void removeComment() {
        Plotter plotter = new Plotter();
        PlotterSettings ps = new PlotterSettings();
        ps.setDouble(PlotterSettings.FEED_RATE_DRAW,42);
        plotter.setSettings(ps);
        MarlinPlotterPanel mpi = new MarlinPlotterPanel(plotter, new ChooseConnection());

        assertEquals("G0 X20 Y30",mpi.removeComment("G0 X20 Y30 ; this is a comment"));
        assertEquals("G0 X40 Y50",mpi.removeComment("G0 X40 Y50"));
        assertEquals("G0 Y60",mpi.removeComment(" G0 Y60"));
        assertEquals("G0 F600",mpi.removeComment(" G0 F600 ; ;;;"));
    }
    @Test
    public void testZAxisGcode() {
        testZAxisGcode(PlotterSettings.Z_MOTOR_TYPE_SERVO,"M280 P0 S45 T50","M280 P0 S90 T150");
        testZAxisGcode(PlotterSettings.Z_MOTOR_TYPE_STEPPER,"G1 Z45 F50","G0 Z90 F150");
    }

    private void testZAxisGcode(int type,String matchDown,String matchUp) {
        Plotter plotter = new Plotter();
        PlotterSettings ps = plotter.getSettings();
        ps.setInteger(PlotterSettings.Z_MOTOR_TYPE,type);
        ps.setDouble(PlotterSettings.PEN_ANGLE_DOWN,45);
        ps.setDouble(PlotterSettings.PEN_ANGLE_UP,90);
        ps.setDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME,50);
        ps.setDouble(PlotterSettings.PEN_ANGLE_UP_TIME,150);
        plotter.setSettings(ps);
        Assertions.assertEquals(matchDown, plotter.getSettings().getPenDownString());
        Assertions.assertEquals(matchUp, plotter.getSettings().getPenUpString());
    }
}
