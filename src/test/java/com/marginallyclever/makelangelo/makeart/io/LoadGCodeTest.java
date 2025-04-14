package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

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