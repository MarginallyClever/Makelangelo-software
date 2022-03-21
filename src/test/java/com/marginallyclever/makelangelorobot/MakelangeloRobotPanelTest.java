package com.marginallyclever.makelangeloRobot;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGeneratorFactory;
import com.marginallyclever.util.PreferencesHelper;
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
