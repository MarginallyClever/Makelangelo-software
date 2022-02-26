package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.marginallyclever.makelangelo.makeArt.io.vector.LoadHelper.readFile;
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

    @Test
    public void load_koch_curve() throws Exception {
        verifyLoadScratch("/scratch3/test_02_koch_curve_03.sb3", "/scratch3/test_02_koch_curve_03_expected.txt");
    }

    private void verifyLoadScratch(String svgFile, String expectedFile) throws Exception {
        // given
        TurtleLoader loader = new LoadScratch3();

        // when
        Turtle turtle = loader.load(LoadSVGTest.class.getResourceAsStream(svgFile));

        // then
        assertNotNull(turtle);
        assertNotNull(turtle.history);
        assertEquals(readFile(expectedFile), turtle.history.toString());
    }
}