package com.marginallyclever.makelangelo.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

public class MetricsPreferences {
	static private JPanel panel;
	static private JCheckBox collectAnonymousMetricsCheckbox;

	static private String COLLECT_ANONYMOUS_METRICS_LABEL = "Collect Anonymous Metrics";
	
	static public JPanel buildPanel() {
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		collectAnonymousMetricsCheckbox = new JCheckBox(Translator.get("collectAnonymousMetrics"));

		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		collectAnonymousMetricsCheckbox.setSelected(prefs.getBoolean(COLLECT_ANONYMOUS_METRICS_LABEL, false));
	
		GridBagConstraints c = new GridBagConstraints();	
		int y = 0;
	
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		panel.add(collectAnonymousMetricsCheckbox, c);
		y++;
		
		return panel;
	}
	
	static public void save() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		prefs.putBoolean(COLLECT_ANONYMOUS_METRICS_LABEL, collectAnonymousMetricsCheckbox.isSelected());
	}
	
	static public void cancel() {
		
	}
	
	static public boolean getAllowMetrics() {
		if(collectAnonymousMetricsCheckbox != null) return collectAnonymousMetricsCheckbox.isSelected();
		
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		return prefs.getBoolean(COLLECT_ANONYMOUS_METRICS_LABEL,false);
	}
}
