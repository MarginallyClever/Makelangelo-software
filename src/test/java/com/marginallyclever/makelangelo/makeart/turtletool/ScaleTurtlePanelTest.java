package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.swing.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class ScaleTurtlePanelTest {
    private FrameFixture window;

    @BeforeEach
    public void setUp() {
        Robot robot = BasicRobot.robotWithNewAwtHierarchy();

        PreferencesHelper.start();
        Translator.start();

        // make a Turtle of a rectangle
        Turtle turtle = new Turtle();
        turtle.jumpTo(0, 0);
        turtle.moveTo(100, 0);
        turtle.moveTo(100, 50);
        turtle.moveTo(0, 50);
        turtle.moveTo(0, 0);

        ScaleTurtlePanel panel = new ScaleTurtlePanel(turtle);
        JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.pack();
        window = new FrameFixture(robot, frame);
        window.show(); // shows the frame to test
    }

    @AfterEach
    public void tearDown() {
        if(window!=null) window.cleanUp();
    }

    @Test
    public void testScaleTurtlePanel() {
        JPanelFixture panel = window.panel(ScaleTurtlePanel.class.getSimpleName());
        panel.requireVisible();

        var w = panel.spinner("width");
        var h = panel.spinner("height");
        var units = panel.comboBox("units");

        panel.checkBox("lockRatio").requireSelected();
        w.requireValue(100.0);
        h.requireValue(50.0);
        units.requireSelection("mm");

        // changing to % should make it 100% and 100%
        units.selectItem("%");
        w.requireValue(100.0);
        h.requireValue(100.0);

        // changing width should change height
        w.enterText("200");
        h.click();
        w.requireValue(200.0);
        h.requireValue(200.0);

        // unlock ratio, changing width should NOT change height
        panel.checkBox("lockRatio").uncheck();
        w.enterText("100");
        h.click();
        w.requireValue(100.0);
        h.requireValue(200.0);

        // trigger a combo box change without actually changing value
        units.selectItem("%");
        w.requireValue(100.0);
        h.requireValue(200.0);
        units.selectItem("mm");
        w.requireValue(100.0);
        h.requireValue(100.0);
    }
}
