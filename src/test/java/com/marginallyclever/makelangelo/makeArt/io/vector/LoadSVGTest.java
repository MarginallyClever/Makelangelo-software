package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.stream.Stream;

import static com.marginallyclever.makelangelo.makeArt.io.vector.LoadHelper.loadAndTestFiles;
import static com.marginallyclever.makelangelo.makeArt.io.vector.LoadHelper.readFile;
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
        return loadAndTestFiles("src/test/resources/svg", ".svg", this::verifyLoadSvg);
    }

    private void verifyLoadSvg(File filenameToTest, File fileExpected) {
        try {

            // given
            TurtleLoader loader = new LoadSVG();

            // when
            Turtle turtle = loader.load(new FileInputStream(filenameToTest));

            // then
            assertNotNull(turtle);
            assertNotNull(turtle.history);
            assertEquals(readFile(fileExpected), turtle.history.toString());
        } catch( Exception e) {
            fail(e);
        }
    }
}
