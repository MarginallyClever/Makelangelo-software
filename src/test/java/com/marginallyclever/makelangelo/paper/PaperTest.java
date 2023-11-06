package com.marginallyclever.makelangelo.paper;

import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PaperTest {
	@BeforeEach
	public void beforeEach() {
		PreferencesHelper.start();
	}

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

	@Test
	public void testPaperLocation() {
		Paper a = new Paper();
		a.setPaperSize(200,100,0,0);
		a.setPaperMargin(0.9);
		Assertions.assertEquals(180,a.getMarginWidth());
		Assertions.assertEquals(90,a.getMarginHeight());
		Assertions.assertEquals(-90,a.getMarginLeft());
		Assertions.assertEquals(-45,a.getMarginBottom());
		Assertions.assertEquals(90,a.getMarginRight());
		Assertions.assertEquals(45,a.getMarginTop());
		Assertions.assertEquals(0,a.getCenterX());
		Assertions.assertEquals(0,a.getCenterY());
		a.setPaperSize(200,100,50,100);
		Assertions.assertEquals(180,a.getMarginWidth());
		Assertions.assertEquals(90,a.getMarginHeight());
		Assertions.assertEquals(-90,a.getMarginLeft());
		Assertions.assertEquals(-45,a.getMarginBottom());
		Assertions.assertEquals(90,a.getMarginRight());
		Assertions.assertEquals(45,a.getMarginTop());
		Assertions.assertEquals(50,a.getCenterX());
		Assertions.assertEquals(100,a.getCenterY());
	}
}
