package com.marginallyclever.makelangelo.plotter.plottercontrols;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.editorcontext.EditorContext;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterEvent;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarlinPlotterInterfaceTest {

    @BeforeAll
    public static void init() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void onHearM114_OK() {
        Plotter plotter = new Plotter();
        setTopLeftMachineOrigin(plotter);
        plotter.setPos(23,45);
        RecordingMarlinPlotterPanel mpi = new RecordingMarlinPlotterPanel(plotter);
        List<Integer> events = new ArrayList<>();
        plotter.addPlotterEventListener(event -> events.add(event.type));

        String message = "X:10.00 Y:-186.00 Z:200.00 Count X:72290 Y:72290 Z:32000";
        mpi.onHearM114(message);

        assertEquals(-90, plotter.getPos().x);
        assertEquals(14, plotter.getPos().y);
        assertTrue(mpi.commands.isEmpty());
        assertEquals(List.of(PlotterEvent.POSITION_FEEDBACK), events);
    }

    @Test
    public void onHearM114_KO() {
        Plotter plotter = new Plotter();
        setTopLeftMachineOrigin(plotter);
        plotter.setPos(23,45);
        RecordingMarlinPlotterPanel mpi = new RecordingMarlinPlotterPanel(plotter);

        String message = "X:10.00 Y:invalid Z:200.00 Count X:72290 Y:72290 Z:32000";
        mpi.onHearM114(message);

        // original values
        assertEquals(23, plotter.getPos().x);
        assertEquals(45, plotter.getPos().y);
        assertTrue(mpi.commands.isEmpty());

        mpi.onHearM114("X:10.00 Y:20.00 Z:invalid Count X:0 Y:0 Z:0");
        assertEquals(23, plotter.getPos().x);
        assertEquals(45, plotter.getPos().y);
        assertTrue(mpi.commands.isEmpty());
    }

    @Test
    public void onHearM114_PartialAxisPreservesOtherMachineCoordinate() {
        Plotter plotter = new Plotter();
        setTopLeftMachineOrigin(plotter);
        plotter.setPos(23,45);
        RecordingMarlinPlotterPanel mpi = new RecordingMarlinPlotterPanel(plotter);

        mpi.onHearM114("X:10.00 Count X:72290 Y:72290 Z:32000");

        assertEquals(-90, plotter.getPos().x);
        assertEquals(45, plotter.getPos().y);
        assertTrue(mpi.commands.isEmpty());

        mpi.onDataReceived(new NetworkSessionEvent(mpi, NetworkSessionEvent.DATA_RECEIVED,
                "Y:-150.00 Count X:72290 Y:72290 Z:32000"));
        assertEquals(-90, plotter.getPos().x);
        assertEquals(50, plotter.getPos().y);
        assertTrue(mpi.commands.isEmpty());
    }

    @Test
    public void generatedMovesUseMachineCoordinatesAndAbsoluteMode() {
        Plotter plotter = new Plotter();
        setTopLeftMachineOrigin(plotter);
        RecordingMarlinPlotterPanel mpi = new RecordingMarlinPlotterPanel(plotter);

        plotter.setPos(0,0);

        assertEquals(List.of("G90" + System.lineSeparator() + "G1 X100.000 Y-200.000"), mpi.commands);
    }

    @Test
    public void editorContextKeepsActivePaperOnPlotter() {
        EditorContext context = new EditorContext();
        Paper paper = new Paper();
        context.setPaper(paper);
        assertSame(paper, context.getPlotter().getPaper());

        Plotter plotter = new Plotter();
        context.setPlotter(plotter);
        assertSame(paper, plotter.getPaper());
    }

    @Test
    public void jobTransformRemainsStableUntilTheJobFinishes() throws Exception {
        Plotter plotter = new Plotter();
        setTopLeftMachineOrigin(plotter);
        RecordingMarlinPlotterPanel mpi = new RecordingMarlinPlotterPanel(plotter);
        mpi.beginJob();

        plotter.setPos(0,0);
        plotter.getSettings().setInteger(PlotterSettings.STARTING_POS_INDEX, 8);
        plotter.getPaper().setPaperSize(400,600,20,-30);
        plotter.setPos(10,20);
        mpi.pendingCommands = true;
        mpi.finishJob();
        plotter.setPos(10,20);

        mpi.pendingCommands = false;
        mpi.onDataReceived(new NetworkSessionEvent(mpi, NetworkSessionEvent.DATA_RECEIVED, "ok"));
        SwingUtilities.invokeAndWait(() -> {});
        plotter.setPos(10,20);

        assertEquals("G1 X100.000 Y-200.000", mpi.commands.get(0).split(System.lineSeparator())[1]);
        assertEquals("G1 X110.000 Y-180.000", mpi.commands.get(1).split(System.lineSeparator())[1]);
        assertEquals("G1 X110.000 Y-180.000", mpi.commands.get(2).split(System.lineSeparator())[1]);
        assertEquals("G1 X-210.000 Y350.000", mpi.commands.get(3).split(System.lineSeparator())[1]);
    }

    @Test
    public void findHomeDoesNotGuessTheReportedPosition() {
        Plotter plotter = new Plotter();
        setTopLeftMachineOrigin(plotter);
        plotter.setPos(23,45);
        RecordingMarlinPlotterPanel mpi = new RecordingMarlinPlotterPanel(plotter);

        plotter.findHome();

        assertEquals(23, plotter.getPos().x);
        assertEquals(45, plotter.getPos().y);
        assertTrue(mpi.commands.contains(plotter.getSettings().getFindHomeString()));
        assertTrue(mpi.commands.contains("M114"));
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
        var separator = System.lineSeparator();
        testZAxisGcode("M280 P0 S%4 T%5"+separator+"G0 F%3",
                "M280 P0 S%1 T%2"+separator+"G0 F%3",
                "M280 P0 S45.0 T50.0"+separator+"G0 F3000.0",
                "M280 P0 S90.0 T150.0"+separator+"G0 F3000.0");
        testZAxisGcode("G1 Z%4 F%5"+separator+"G0 F%3",
                "G0 Z%1 F%2"+separator+"G0 F%3",
                "G1 Z45.0 F50.0"+separator+"G0 F3000.0",
                "G0 Z90.0 F150.0"+separator+"G0 F3000.0");
    }

    private void testZAxisGcode(String setDown,String setUp,String matchDown,String matchUp) {
        Plotter plotter = new Plotter();
        PlotterSettings ps = plotter.getSettings();
        ps.setString(PlotterSettings.PEN_UP_GCODE,setUp);
        ps.setString(PlotterSettings.PEN_DOWN_GCODE,setDown);
        ps.setDouble(PlotterSettings.PEN_ANGLE_DOWN,45);
        ps.setDouble(PlotterSettings.PEN_ANGLE_UP,90);
        ps.setDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME,50);
        ps.setDouble(PlotterSettings.PEN_ANGLE_UP_TIME,150);
        plotter.setSettings(ps);
        Assertions.assertEquals(matchDown, plotter.getSettings().getPenDownString());
        Assertions.assertEquals(matchUp, plotter.getSettings().getPenUpString());
    }

    private static void setTopLeftMachineOrigin(Plotter plotter) {
        PlotterSettings settings = plotter.getSettings();
        settings.setDouble(PlotterSettings.LIMIT_LEFT, -100);
        settings.setDouble(PlotterSettings.LIMIT_RIGHT, 100);
        settings.setDouble(PlotterSettings.LIMIT_BOTTOM, -200);
        settings.setDouble(PlotterSettings.LIMIT_TOP, 200);
        settings.setInteger(PlotterSettings.STARTING_POS_INDEX, 0);
        Paper paper = new Paper();
        paper.setPaperSize(200,400,0,0);
        plotter.setPaper(paper);
    }

    private static class RecordingMarlinPlotterPanel extends MarlinPlotterPanel {
        private final List<String> commands = new ArrayList<>();
        private boolean pendingCommands;

        private RecordingMarlinPlotterPanel(Plotter plotter) {
            super(plotter, new ChooseConnection());
        }

        @Override
        public void queueAndSendCommands(String command) {
            commands.add(command);
        }

        @Override
        protected boolean hasPendingCommands() {
            return pendingCommands;
        }
    }
}
