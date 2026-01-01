package com.marginallyclever.makelangelo.preview;

import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class OpenGLPanelTest {
    @Test
    public void testOpenGLPanelInitialization() {
        PreferencesHelper.start();
        OpenGLPanel panel = new OpenGLPanel();

        // create and display a frame
        javax.swing.JFrame frame = new javax.swing.JFrame("OpenGLPanel Test");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // pause for 0.5 seconds
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}

        frame.dispose();
    }
}
