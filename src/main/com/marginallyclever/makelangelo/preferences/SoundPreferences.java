package com.marginallyclever.makelangelo.preferences;

import java.util.prefs.Preferences;

import com.marginallyclever.core.select.SelectFile;
import com.marginallyclever.core.select.SelectPanel;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;


/**
 * Adjust sound preferences
 */
public class SoundPreferences {
	static private SelectPanel panel;
	static private SelectFile sound_connect;
	static private SelectFile sound_disconnect;
	static private SelectFile sound_conversion_finished;
	static private SelectFile sound_drawing_finished;

	static public SelectPanel buildPanel() {
		Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.SOUND);

		panel = new SelectPanel();
		// TODO allow only valid audio files here.
		panel.add(sound_connect = new SelectFile(Translator.get("MenuSoundsConnect"),null,prefs.get("sound_connect", "")));
		panel.add(sound_disconnect = new SelectFile(Translator.get("MenuSoundsDisconnect"),null,prefs.get("sound_disconnect", "")));
		panel.add(sound_conversion_finished = new SelectFile(Translator.get("MenuSoundsFinishConvert"),null,prefs.get("sound_conversion_finished", "")));
		panel.add(sound_drawing_finished = new SelectFile(Translator.get("MenuSoundsFinishDraw"),null,prefs.get("sound_drawing_finished", "")));
		panel.finish();
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
