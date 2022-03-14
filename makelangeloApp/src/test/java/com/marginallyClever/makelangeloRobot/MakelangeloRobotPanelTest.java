package com.marginallyClever.makelangeloRobot;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.makeArt.turtleGenerator.TurtleGenerator;
import com.marginallyClever.makelangelo.makeArt.turtleGenerator.TurtleGeneratorFactory;
import com.marginallyClever.util.PreferencesHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class MakelangeloRobotPanelTest {

    @Test
    public void testNoMissingGeneratorPanels() {
        PreferencesHelper.start();
        Translator.start();
        try {
            for (TurtleGenerator c : TurtleGeneratorFactory.available) {
                assertNotNull(c.getPanel());
            }
        } catch (Exception e) {
            fail("Missing panel! " + e.getLocalizedMessage());
        }
    }
}
