package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.fail;

public class TurtleGeneratorTest {

    @Test
    public void testNoMissingGeneratorPanels() {
        PreferencesHelper.start();
        Translator.start();
        try {
            testNothingMissingInTreeNode(TurtleGeneratorFactory.available);
        } catch (Exception e) {
            fail("Missing panel! " + e.getLocalizedMessage());
        }
    }

    public void testNothingMissingInTreeNode(TurtleGeneratorFactory.TurtleGeneratorLeaf node) {
        JMenu menu = new JMenu(node.getName());
        for (TurtleGeneratorFactory.TurtleGeneratorLeaf child : node.getChildren()) {
            if (child.getChildren().isEmpty()) {
                new TurtleGeneratorPanel(child.getGenerator());
            } else {
                testNothingMissingInTreeNode(child);
            }
        }
    }
}
