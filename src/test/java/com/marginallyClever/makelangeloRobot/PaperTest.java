package com.marginallyclever.makelangeloRobot;

import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PaperTest {
	@BeforeEach
	public void beforeEach() {
		PreferencesHelper.start();
	}

	/*
	@Test
	public void testChangeToolMessage() {
		Translator.start();
		MakelangeloRobot r = new MakelangeloRobot();
		r.changeToTool(0);
		r.changeToTool((255<<16));
		r.changeToTool((255<< 8));
		r.changeToTool((255<< 0));
		r.changeToTool((255<< 8)+(255<< 0));
	}
	*/

	@Test
	public void testPaperSettingChanges() {
		Paper a = new Paper();
		a.loadConfig();
		double w = a.getPaperWidth();
		double h = a.getPaperHeight();
		a.setPaperSize(w/2,h/2,0,0);
		a.saveConfig();
		Paper b = new Paper();
		b.loadConfig();
		Assertions.assertEquals(w/2, b.getPaperWidth());
		Assertions.assertEquals(h/2, b.getPaperHeight());
		a.setPaperSize(w,h,0,0);
		a.saveConfig();
		// TODO: this is a potentially destructive change if the test fails.
	}
}
