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

public class LoadScratch3Test {

    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void canLoad() {
        // given
        TurtleLoader loader = new LoadScratch3();

        // then
        assertTrue(loader.canLoad("file.sb3"));
        assertTrue(loader.canLoad("file.Sb3"));
        assertFalse(loader.canLoad("file.txt"));
    }

    @Test
    public void throwExceptionWhenStreamIsNull() {
        // given
        TurtleLoader loader = new LoadSVG();

        // then
        assertThrows(NullPointerException.class, () -> {
            loader.load(LoadScratch3Test.class.getResourceAsStream("/doesNotExist"));
        }, "Input stream is null");
    }

    @TestFactory
    public Stream<DynamicTest> testAllFiles() {
        return loadAndTestFiles(of("test_02_koch_curve_03.sb3"),
                "/scratch3",
                this::verifyLoadScratch3);
    }

    private void verifyLoadScratch3(String filenameToTest, String fileExpected) {
        try {

            // given
            TurtleLoader loader = new LoadScratch3();

            // when
            Turtle turtle = loader.load(LoadScratch3Test.class.getResourceAsStream(filenameToTest));

            // then
            assertNotNull(turtle);
            assert(turtle.hasDrawing());
            assertEquals(readFile(fileExpected), turtle.generateHistory());
        } catch( Exception e) {
            fail(e);
        }
    }
}