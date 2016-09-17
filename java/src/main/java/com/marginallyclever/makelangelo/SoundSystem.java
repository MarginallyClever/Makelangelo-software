package com.marginallyclever.makelangelo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.prefs.Preferences;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.marginallyclever.makelangelo.preferences.SoundPreferences;
import com.marginallyclever.util.PreferencesHelper;


public class SoundSystem {
	static public void playSound(String url) {
		if (url.isEmpty()) return;

		try {
			Clip clip = AudioSystem.getClip();
			BufferedInputStream x = new BufferedInputStream(new FileInputStream(url));
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(x);
			clip.open(inputStream);
			clip.start();
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
	}

	static public void playConnectSound() {
		SoundPreferences prefs = new SoundPreferences(null);
		playSound(prefs.getConnectSoundFilename());
	}

	static public void playDisconnectSound() {
		SoundPreferences prefs = new SoundPreferences(null);
		playSound(prefs.getDisonnectSoundFilename());
	}

	static public void playConversionFinishedSound() {
		SoundPreferences prefs = new SoundPreferences(null);
		playSound(prefs.getConnectSoundFilename());
	}

	static public void playDrawingFinishedSound() {
		SoundPreferences prefs = new SoundPreferences(null);
		playSound(prefs.getDrawingFinishedSoundFilename());
	}
}
