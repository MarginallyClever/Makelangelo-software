package com.marginallyclever.makelangelo.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import com.marginallyclever.util.PreferencesHelper;


/**
 * Adjust sound preferences
 */
public class SoundPreferences {
	static private JPanel panel;
	static private SelectSound sound_connect;
	static private SelectSound sound_disconnect;
	static private SelectSound sound_conversion_finished;
	static private SelectSound sound_drawing_finished;

	static public JPanel buildPanel() {
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		GridBagConstraints label = new GridBagConstraints();

		label.anchor = GridBagConstraints.NORTH;
		label.fill = GridBagConstraints.HORIZONTAL;
		label.gridwidth = 1;
		label.gridx = 0;
		label.gridy = 0;

		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.SOUND);

		sound_connect = new SelectSound("MenuSoundsConnect",prefs.get("sound_connect", ""));
		panel.add(sound_connect,label);  label.gridy++;

		sound_disconnect = new SelectSound("MenuSoundsDisconnect",prefs.get("sound_disconnect", ""));
		panel.add(sound_disconnect,label);  label.gridy++;

		sound_conversion_finished = new SelectSound("MenuSoundsFinishConvert",prefs.get("sound_conversion_finished", ""));
		panel.add(sound_conversion_finished,label);  label.gridy++;

		sound_drawing_finished = new SelectSound("MenuSoundsFinishDraw",prefs.get("sound_drawing_finished", ""));
		panel.add(sound_drawing_finished,label);  label.gridy++;
		
		return panel;
	}
	
	static public void save() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.SOUND);
		
		prefs.put("sound_connect", sound_connect.getText());
		prefs.put("sound_disconnect", sound_disconnect.getText());
		prefs.put("sound_conversion_finished", sound_conversion_finished.getText());
		prefs.put("sound_drawing_finished", sound_drawing_finished.getText());
	}
	
	static public void cancel() {}
	
	static public String getConnectSoundFilename() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.SOUND);
		
		return prefs.get("sound_connect", "");
	}
	static public String getDisonnectSoundFilename() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.SOUND);
		
		return prefs.get("sound_disconnect", "");
	}
	static public String getConversionFinishedSoundFilename() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.SOUND);
		
		return prefs.get("sound_conversion_finished", "");
	}
	static public String getDrawingFinishedSoundFilename() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.SOUND);
		
		return prefs.get("sound_drawing_finished", "");
	}
}
