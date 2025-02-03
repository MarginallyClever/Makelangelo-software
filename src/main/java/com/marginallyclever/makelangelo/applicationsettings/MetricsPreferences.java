package com.marginallyclever.makelangelo.applicationsettings;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.donatello.select.SelectBoolean;
import com.marginallyclever.donatello.select.SelectPanel;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.text.JTextComponent;
import java.util.prefs.Preferences;

public class MetricsPreferences {
	static private final String COLLECT_ANONYMOUS_METRICS_LABEL = "Collect Anonymous Metrics";

	static private SelectPanel panel;
	static private SelectBoolean collectAnonymousMetricsCheckbox;

	static public SelectPanel buildPanel() {
		panel = new SelectPanel();

		var aboutHtml = Translator.get("MetricsPreferences.collectAnonymousMetrics");
		final JTextComponent bottomText = SelectReadOnlyText.createJEditorPaneWithHyperlinkListenerAndToolTipsForDesktopBrowse(aboutHtml);

		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		collectAnonymousMetricsCheckbox = new SelectBoolean("collect", "",
				prefs.getBoolean(COLLECT_ANONYMOUS_METRICS_LABEL, false));

		panel.add(bottomText);
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
