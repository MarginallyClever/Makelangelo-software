package com.marginallyclever.makelangeloRobot;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGeneratorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class MakelangeloRobotPanelGUITest {

    @Test
    public void testNoMissingGeneratorPanels() {
        Log.start();
        Log.message("testNoMissingGeneratorPanels() begin.");
        Translator.start();
        try {
            for (TurtleGenerator c : TurtleGeneratorFactory.available) {
                assertNotNull(c.getPanel());
            }
        } catch (Exception e) {
            fail("Missing panel! " + e.getLocalizedMessage());
        }
        Log.message("testNoMissingGeneratorPanels() complete.");
    }
}
