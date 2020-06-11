package com.marginallyclever.makelangelo.preferences;

import java.util.prefs.Preferences;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.util.PreferencesHelper;

public class GFXPreferences {
	static private SelectPanel panel;
	static private SelectBoolean showPenUpCheckbox;
	static private SelectBoolean antialiasOnCheckbox;
	static private SelectBoolean speedOverQualityCheckbox;
	static private SelectBoolean drawAllWhileRunningCheckbox;

	
	static public SelectPanel buildPanel() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		
		panel = new SelectPanel();
		showPenUpCheckbox = new SelectBoolean(Translator.get("MenuGraphicsPenUp"),prefs.getBoolean("show pen up", false));
		antialiasOnCheckbox = new SelectBoolean(Translator.get("MenuGraphicsAntialias"),prefs.getBoolean("antialias", true));
		speedOverQualityCheckbox = new SelectBoolean(Translator.get("MenuGraphicsSpeedVSQuality"),prefs.getBoolean("speed over quality", true));
		drawAllWhileRunningCheckbox = new SelectBoolean(Translator.get("MenuGraphicsDrawWhileRunning"),prefs.getBoolean("Draw all while running", true));

		panel.add(showPenUpCheckbox);
		panel.add(drawAllWhileRunningCheckbox);
		panel.add(antialiasOnCheckbox);
		panel.add(speedOverQualityCheckbox);
		panel.finish();

		return panel;
	}
	
	static public void save() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		prefs.putBoolean("show pen up", showPenUpCheckbox.isSelected());
		prefs.putBoolean("antialias", antialiasOnCheckbox.isSelected());
		prefs.putBoolean("speed over quality", speedOverQualityCheckbox.isSelected());
		prefs.putBoolean("Draw all while running", drawAllWhileRunningCheckbox.isSelected());
	}
	
	static public void cancel() {
		
	}
	
	static public boolean getShowPenUp() {
		if(showPenUpCheckbox != null) return showPenUpCheckbox.isSelected();
		
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		return prefs.getBoolean("show pen up",false);
	}
}
