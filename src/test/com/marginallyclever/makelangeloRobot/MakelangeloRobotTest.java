package com.marginallyclever.makelangeloRobot;

import org.junit.Test;

import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;


public class MakelangeloRobotTest {
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
		MakelangeloRobotSettings a = new MakelangeloRobotSettings();
		a.loadConfig(0);
		double w = a.getPaperWidth();
		double h = a.getPaperHeight();
		a.setPaperSize(w/2,h/2,0,0);
		a.saveConfig();
		MakelangeloRobotSettings b = new MakelangeloRobotSettings();
		b.loadConfig(0);
		assert(w/2 == b.getPaperWidth());
		assert(h/2 == b.getPaperHeight());
		a.setPaperSize(w,h,0,0);
		a.saveConfig();
		// TODO: this is a potentially destructive change if the test fails.
	}
}
