package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static com.marginallyclever.makelangelo.makeart.io.LoadHelper.loadAndTestFiles;
import static com.marginallyclever.makelangelo.makeart.io.LoadHelper.readFile;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class LoadGCodeTest {

    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void canLoad() {
        // given
        TurtleLoader loader = new LoadGCode();

        // then
        assertTrue(loader.canLoad("file.gcode"));
        assertTrue(loader.canLoad("file.GCode"));
        assertFalse(loader.canLoad("file.txt"));
    }

    @Test
    public void throwExceptionWhenStreamIsNull() {
        // given
        TurtleLoader loader = new LoadGCode();

        // then
        assertThrows(NullPointerException.class, () -> {
            loader.load(LoadGCodeTest.class.getResourceAsStream("/doesNotExist"));
        }, "Input stream is null");
    }

    @Test
    public void profileAwareLoadingTranslatesResolvedRelativeMovesAndArcs() throws Exception {
        PlotterSettings settings = new PlotterSettings();
        settings.setDouble(PlotterSettings.LIMIT_LEFT, -100);
        settings.setDouble(PlotterSettings.LIMIT_RIGHT, 100);
        settings.setDouble(PlotterSettings.LIMIT_BOTTOM, -200);
        settings.setDouble(PlotterSettings.LIMIT_TOP, 200);
        settings.setInteger(PlotterSettings.STARTING_POS_INDEX, 0);
        Paper paper = new Paper();
        paper.setPaperSize(200, 400, 0, 0);
        String gcode = "G90\nG0 X10 Y10\nM280 S0\nG91\nG1 X5 Y-2\nG3 X10 Y0 I-5 J0\n";
        LoadGCode loader = new LoadGCode();

        Turtle machineFrame = loader.load(new ByteArrayInputStream(gcode.getBytes(StandardCharsets.UTF_8)));
        Turtle applicationFrame = loader.load(new ByteArrayInputStream(gcode.getBytes(StandardCharsets.UTF_8)), settings, paper);
        var machineBounds = machineFrame.getBounds();
        var applicationBounds = applicationFrame.getBounds();

        assertEquals(machineFrame.countPoints(), applicationFrame.countPoints());
        assertEquals(machineFrame.getDrawDistance(), applicationFrame.getDrawDistance(), 1e-6);
        assertEquals(machineBounds.width, applicationBounds.width, 1e-6);
        assertEquals(machineBounds.height, applicationBounds.height, 1e-6);
        assertEquals(machineBounds.x - 100, applicationBounds.x, 1e-6);
        assertEquals(machineBounds.y + 200, applicationBounds.y, 1e-6);
        assertEquals(machineFrame.getX() - 100, applicationFrame.getX(), 1e-6);
        assertEquals(machineFrame.getY() + 200, applicationFrame.getY(), 1e-6);
    }

    @TestFactory
    public Stream<DynamicTest> testAllFiles() {
        return loadAndTestFiles(of("multi_shapes_ignatus2.gcode",
                "multi_shapes_path-circle-line-rect.gcode"),
                "/gcode",
                this::verifyLoadGCode);
    }

    private void verifyLoadGCode(String filenameToTest, String fileExpected) {
        try {
            // given
            TurtleLoader loader = new LoadGCode();
            Turtle turtle = loader.load(LoadGCodeTest.class.getResourceAsStream(filenameToTest));
            // then
            assertNotNull(turtle);
            assert(turtle.hasDrawing());
            assertEquals(readFile(fileExpected), turtle.generateHistory());
        } catch( Exception e) {
            fail(e);
        }
    }
}