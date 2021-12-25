package com.marginallyclever.makelangeloRobot;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGeneratorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class MakelangeloRobotPanelTest {

    @Test
    public void testNoMissingGeneratorPanels() {
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
