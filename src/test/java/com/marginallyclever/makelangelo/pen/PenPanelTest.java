package com.marginallyclever.makelangelo.pen;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.swing.*;
import java.awt.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class PenPanelTest {
    private FrameFixture window;
    private final Pen pen = new Pen();

    @BeforeEach
    public void setUp() {
        Robot robot = BasicRobot.robotWithNewAwtHierarchy();
        PreferencesHelper.start();
        Translator.start();

        PenPanel panel = new PenPanel(pen);
        JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.pack();
        window = new FrameFixture(robot, frame);
        window.show(); // shows the frame to test
    }

    @AfterEach
    public void tearDown() {
        if (window != null) window.cleanUp();
    }

    @Test
    //@Disabled("This test is not yet working.  It needs a way to simulate the color selection dialog.")
    public void testChangePenColor() {
        JPanelFixture panel = window.panel(PenPanel.class.getSimpleName());
        panel.requireVisible();
        pen.color = new Color(255, 0, 0);
        // Assuming "SelectColor" component has a name set to "Pen.Color"
        panel.button("PenColor.button").click(); // Simulate selecting a new color
        // this opens a color selection dialog.  Click "ok" in the dialog to close it.

        // Wait for the color selection dialog to become visible
        DialogFixture dialog = window.dialog();

        // Since we cannot directly interact with the JColorChooser, we focus on confirming the dialog
        // This assumes the dialog has an "OK" button that can be identified
        dialog.button(JButtonMatcher.withText("OK")).click();

        // Verify the color change, assuming the color change reflects immediately on the pen object
        Color expectedColor = new Color(0, 0, 0); // Example expected color, adjust as necessary
        assert expectedColor.equals(pen.color) : "Pen color should be updated";
    }

    @Test
    //@Disabled("This test is not yet working.  It needs a way to simulate the radius change.")
    public void testChangePenRadius() {
        JPanelFixture panel = window.panel(PenPanel.class.getSimpleName());
        panel.requireVisible();
        pen.radius=0;
        // Assuming "SelectDouble" component for radius has a name set to "Pen.Radius"
        var f = panel.textBox("PenRadius.field");
        f.selectAll().enterText("1.5"); // Simulate changing the pen radius
        // wait 150ms
        try {
            Thread.sleep(250); // Wait for 150 milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Handle the InterruptedException by re-interrupting the thread
        }
        // Verify the radius change, assuming the change reflects immediately on the pen object
        double expectedRadius = 1.5; // Example expected radius, adjust as necessary
        assert pen.radius == expectedRadius : "Pen radius should be updated";
    }
}