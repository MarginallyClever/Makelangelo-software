package com.marginallyclever.makelangelo.paper;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

/**
 * {@code PaperSettings} tests
 * @author Dan Royer
 *
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class PaperSettingsTest {
	@Before
	public void before() {
		Log.start();
		PreferencesHelper.start();
		Translator.start();
	}
	
	@After
	public void after() {
		Log.end();
	}
	
	@Test
	@Timeout(10)
	public void runPaperSettings() {
		JFrame frame = new JFrame(PaperSettings.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PaperSettings(new Paper()));
		frame.pack();
		frame.setVisible(true);
	}
}
