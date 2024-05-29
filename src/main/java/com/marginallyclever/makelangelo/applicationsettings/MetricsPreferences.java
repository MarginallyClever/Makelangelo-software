package com.marginallyclever.makelangelo.applicationsettings;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.util.PreferencesHelper;

import java.util.prefs.Preferences;

public class MetricsPreferences {
	static private final String COLLECT_ANONYMOUS_METRICS_LABEL = "Collect Anonymous Metrics";

	static private SelectPanel panel;
	static private SelectBoolean collectAnonymousMetricsCheckbox;

	static public SelectPanel buildPanel() {
		panel = new SelectPanel();
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		collectAnonymousMetricsCheckbox = new SelectBoolean("collect",
				Translator.get("MetricsPreferences.collectAnonymousMetrics"),
				prefs.getBoolean(COLLECT_ANONYMOUS_METRICS_LABEL, false));

		panel.add(collectAnonymousMetricsCheckbox);

		return panel;
	}
	
	static public void save() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		prefs.putBoolean(COLLECT_ANONYMOUS_METRICS_LABEL, collectAnonymousMetricsCheckbox.isSelected());
	}

	static public boolean areAllowedToShare() {
		if(collectAnonymousMetricsCheckbox != null) return collectAnonymousMetricsCheckbox.isSelected();
		
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		return prefs.getBoolean(COLLECT_ANONYMOUS_METRICS_LABEL,false);
	}
	
	static public void setAllowedToShare(boolean newState) {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		prefs.putBoolean(COLLECT_ANONYMOUS_METRICS_LABEL, newState);
	}
}
