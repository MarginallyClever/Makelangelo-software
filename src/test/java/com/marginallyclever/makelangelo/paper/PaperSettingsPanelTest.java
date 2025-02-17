package com.marginallyclever.makelangelo.paper;

import com.marginallyclever.makelangelo.Translator;
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

/**
 * {@code PaperSettings} tests
 * @author Dan Royer
 *
 */

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class PaperSettingsPanelTest {
	private FrameFixture window;

	@BeforeEach
	public void setUp() {
		Robot robot = BasicRobot.robotWithNewAwtHierarchy();

		PreferencesHelper.start();
		Translator.start();

		PaperSettingsPanel panel = new PaperSettingsPanel(new Paper());
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

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testLandscapeToPortrait() {
		JPanelFixture panel = window.panel(PaperSettingsPanel.class.getSimpleName());
		panel.requireVisible();
		panel.comboBox("size.field").selectItem(1);
		assert(Double.parseDouble(panel.textBox("width.field").text()) == 1682.0);
		assert(Double.parseDouble(panel.textBox("height.field").text()) == 2378.0);
		panel.comboBox("size.field").selectItem(1);
		panel.checkBox("landscape.field").click();
		assert(Double.parseDouble(panel.textBox("width.field").text()) == 2378.0);
		assert(Double.parseDouble(panel.textBox("height.field").text()) == 1682.0);
	}
}
