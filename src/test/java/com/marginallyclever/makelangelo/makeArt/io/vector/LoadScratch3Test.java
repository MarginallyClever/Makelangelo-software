package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static com.marginallyclever.makelangelo.makeArt.io.vector.LoadHelper.loadAndTestFiles;
import static com.marginallyclever.makelangelo.makeArt.io.vector.LoadHelper.readFile;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class LoadScratch3Test {

    @BeforeEach
    public void beforeEach() {
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
            assertNotNull(turtle.history);
            assertEquals(readFile(fileExpected), turtle.history.toString());
        } catch( Exception e) {
            fail(e);
        }
    }
}