package com.marginallyclever.makelangelo.preferences;

import java.util.prefs.Preferences;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.util.PreferencesHelper;

public class MetricsPreferences {
	static private SelectPanel panel;
	static private SelectBoolean collectAnonymousMetricsCheckbox;

	static private String COLLECT_ANONYMOUS_METRICS_LABEL = "Collect Anonymous Metrics";
	
	static public SelectPanel buildPanel() {
		panel = new SelectPanel();
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		collectAnonymousMetricsCheckbox = new SelectBoolean(
				Translator.get("collectAnonymousMetrics"),
				prefs.getBoolean(COLLECT_ANONYMOUS_METRICS_LABEL, false));

		panel.add(collectAnonymousMetricsCheckbox);
		panel.finish();

		return panel;
	}
	
	static public void save() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		prefs.putBoolean(COLLECT_ANONYMOUS_METRICS_LABEL, collectAnonymousMetricsCheckbox.isSelected());
	}
	
	static public void cancel() {
		
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
