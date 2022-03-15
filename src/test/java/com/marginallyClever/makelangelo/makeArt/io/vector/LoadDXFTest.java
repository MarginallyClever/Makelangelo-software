package com.marginallyClever.makelangelo.makeArt.io.vector;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.turtle.Turtle;
import com.marginallyClever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static com.marginallyClever.makelangelo.makeArt.io.vector.LoadHelper.loadAndTestFiles;
import static com.marginallyClever.makelangelo.makeArt.io.vector.LoadHelper.readFile;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class LoadDXFTest {

    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void canLoad() {
        // given
        TurtleLoader loader = new LoadDXF();

        // then
        assertTrue(loader.canLoad("file.dxf"));
        assertTrue(loader.canLoad("file.DXF"));
        assertFalse(loader.canLoad("file.txt"));
    }

    @Test
    public void throwExceptionWhenStreamIsNull() {
        // given
        TurtleLoader loader = new LoadDXF();

        // then
        assertThrows(NullPointerException.class, () -> {
            loader.load(LoadDXFTest.class.getResourceAsStream("/doesNotExist"));
        }, "Input stream is null");
    }

    @TestFactory
    public Stream<DynamicTest> testAllFiles() {
        return loadAndTestFiles(of("circle.dxf",
                "ellipse.dxf",
                "multi_shapes_ignatus1.dxf",
                "multi_shapes_ignatus2.dxf",
                "multi_shapes_path-circle-line-rect.dxf",
                "multi_shapes_ying-yang.dxf",
                "rect.dxf"),
                "/dxf",
                this::verifyLoadDXF);
    }

    private void verifyLoadDXF(String filenameToTest, String fileExpected) {
        try {

            // given
            TurtleLoader loader = new LoadDXF();

            // when
            Turtle turtle = loader.load(LoadDXFTest.class.getResourceAsStream(filenameToTest));

            // then
            assertNotNull(turtle);
            assertNotNull(turtle.history);
            assertEquals(readFile(fileExpected), turtle.history.toString());
        } catch( Exception e) {
            fail(e);
        }
    }
}