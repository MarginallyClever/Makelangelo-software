package com.marginallyclever.makelangelo.paper;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

/**
 * {@code PaperSettings} tests
 * @author Dan Royer
 *
 */
public class PaperSettingsPanelTest extends AssertJSwingJUnitTestCase {
	private JPanelFixture panel;

	@Override
	protected void onSetUp() {
		PreferencesHelper.start();
		Translator.start();
		panel = new JPanelFixture(robot(),new PaperSettingsPanel(new Paper()));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testLandscapeToPortrait() {
		panel.comboBox("size").selectItem(1);
		assert(panel.textBox("width.field").equals("1682"));
		assert(panel.textBox("height.field").equals("2378"));
		panel.comboBox("size").selectItem(1);
		panel.checkBox("landscape").click();
		assert(panel.textBox("width.field").equals("2378"));
		assert(panel.textBox("height.field").equals("1682"));
	}
}
