package com.marginallyclever.makelangelo.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

public class GFXPreferences {
	static private JPanel panel;
	static private JCheckBox showPenUpCheckbox;
	static private JCheckBox antialiasOnCheckbox;
	static private JCheckBox speedOverQualityCheckbox;
	static private JCheckBox drawAllWhileRunningCheckbox;

	
	static public JPanel buildPanel() {
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		//final JCheckBox allow_metrics = new JCheckBox(String.valueOf("I want to add the distance drawn to the // total"));
		//allow_metrics.setSelected(allowMetrics);
	
		showPenUpCheckbox = new JCheckBox(Translator.get("MenuGraphicsPenUp"));
		antialiasOnCheckbox = new JCheckBox(Translator.get("MenuGraphicsAntialias"));
		speedOverQualityCheckbox = new JCheckBox(Translator.get("MenuGraphicsSpeedVSQuality"));
		drawAllWhileRunningCheckbox = new JCheckBox(Translator.get("MenuGraphicsDrawWhileRunning"));

		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		showPenUpCheckbox.setSelected(prefs.getBoolean("show pen up", false));
		antialiasOnCheckbox.setSelected(prefs.getBoolean("antialias", true));
		speedOverQualityCheckbox.setSelected(prefs.getBoolean("speed over quality", true));
		drawAllWhileRunningCheckbox.setSelected(prefs.getBoolean("Draw all while running", true));
	
		GridBagConstraints c = new GridBagConstraints();
		//c.gridwidth=4;  c.gridx=0;  c.gridy=0;  driver.add(allow_metrics,c);
	
		int y = 0;
	
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		panel.add(showPenUpCheckbox, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		panel.add(drawAllWhileRunningCheckbox, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		panel.add(antialiasOnCheckbox, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		panel.add(speedOverQualityCheckbox, c);
		y++;
		
		return panel;
	}
	
	static public void save() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
		//allowMetrics = allow_metrics.isSelected();
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
