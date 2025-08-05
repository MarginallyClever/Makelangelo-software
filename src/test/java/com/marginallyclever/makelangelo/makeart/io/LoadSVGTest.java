package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static com.marginallyclever.makelangelo.makeart.io.LoadHelper.loadAndTestFiles;
import static com.marginallyclever.makelangelo.makeart.io.LoadHelper.readFile;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class LoadSVGTest {

    @Test
    public void canLoad() {
        // given
        TurtleLoader loader = new LoadSVG();

        // then
        assertTrue(loader.canLoad("file.svg"));
        assertFalse(loader.canLoad("file.txt"));
    }

    @Test
    public void throwExceptionWhenStreamIsNull() {
        // given
        TurtleLoader loader = new LoadSVG();

        // then
        assertThrows(NullPointerException.class, () -> {
            loader.load(LoadSVGTest.class.getResourceAsStream("/doesNotExist"));
        }, "Input stream is null");
    }

    @TestFactory
    public Stream<DynamicTest> testAllSVG() {
        return loadAndTestFiles(of("line.svg",
                        "circle.svg",
                        "ellipse.svg",
                        //"eule.svg",  // too big for test!
                        "hersheyTest.svg",
                        "multi_shapes_ignatus1.svg",
                        "multi_shapes_ignatus2.svg",
                        "multi_shapes_path-circle-line-rect.svg",
                        "multi_shapes_ying-yang.svg",
                        "rect.svg"),
                "/svg",
                this::verifyLoadSvg);
    }

    private void verifyLoadSvg(String filenameToTest, String fileExpected) {
        try {
            // given
            TurtleLoader loader = new LoadSVG();
            Turtle turtle = loader.load(LoadSVGTest.class.getResourceAsStream(filenameToTest));
            // then
            assertNotNull(turtle);
            assert(turtle.hasDrawing());
            assertEquals(readFile(fileExpected), turtle.generateHistory());
        } catch (Exception e) {
            fail(e);
        }
    }
}
