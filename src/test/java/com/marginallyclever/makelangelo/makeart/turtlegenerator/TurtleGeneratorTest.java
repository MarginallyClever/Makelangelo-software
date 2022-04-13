package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class TurtleGeneratorTest {

    @Test
    public void testNoMissingGeneratorPanels() {
        PreferencesHelper.start();
        Translator.start();
        try {
            for (TurtleGenerator c : TurtleGeneratorFactory.available) {
                assertNotNull(new TurtleGeneratorPanel(c));
            }
        } catch (Exception e) {
            fail("Missing panel! " + e.getLocalizedMessage());
        }
    }
}
