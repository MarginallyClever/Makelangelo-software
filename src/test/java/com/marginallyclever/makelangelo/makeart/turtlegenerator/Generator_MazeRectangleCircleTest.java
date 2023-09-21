package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.maze.Generator_MazeCircle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Generator_MazeRectangleCircleTest {
    @BeforeAll
    public static void setup() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void testCount() {
        Generator_MazeCircle m = new Generator_MazeCircle();
        m.setRings(8);

        Assertions.assertEquals(1,m.getCellsPerRing(0));
        Assertions.assertEquals(16,m.getCellsPerRing(1));
        Assertions.assertEquals(16,m.getCellsPerRing(2));
        Assertions.assertEquals(32,m.getCellsPerRing(3));
        Assertions.assertEquals(32,m.getCellsPerRing(4));
        Assertions.assertEquals(32,m.getCellsPerRing(5));
        Assertions.assertEquals(32,m.getCellsPerRing(6));
        Assertions.assertEquals(64,m.getCellsPerRing(7));
        Assertions.assertEquals(64,m.getCellsPerRing(8));
    }
}
