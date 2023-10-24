package com.marginallyclever.makelangelo.paper;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.awt.*;

import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

/**
 * {@code PaperSettings} tests
 * @author Dan Royer
 *
 */
public class PaperSettingsTest extends AssertJSwingJUnitTestCase {
	private FrameFixture window;

	@BeforeAll
	public static void beforeAll() {
		PreferencesHelper.start();
		Translator.start();
	}

	@Override
	protected void onSetUp() {
		application(PaperSettings.class).start();
		
		final String title = PaperSettings.class.getSimpleName();
		
		window = findFrame(new GenericTypeMatcher<>(Frame.class) {
		  protected boolean isMatching(Frame frame) {
		    return title.equals(frame.getTitle()) && frame.isShowing();
		  }
		}).using(robot());
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void runPaperSettings() {
		window.comboBox("size").selectItem(1);
		assert(window.textBox("width.field").equals("1682"));
		assert(window.textBox("height.field").equals("2378"));
		window.comboBox("size").selectItem(1);
		window.checkBox("landscape").click();
		assert(window.textBox("width.field").equals("2378"));
		assert(window.textBox("height.field").equals("1682"));
		window.close();
	}
}
