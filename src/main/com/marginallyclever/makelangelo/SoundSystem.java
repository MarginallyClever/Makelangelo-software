package com.marginallyclever.makelangelo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.preferences.SoundPreferences;


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
		playSound(SoundPreferences.getConnectSoundFilename());
	}

	static public void playDisconnectSound() {
		playSound(SoundPreferences.getDisonnectSoundFilename());
	}

	static public void playConversionFinishedSound() {
		playSound(SoundPreferences.getConversionFinishedSoundFilename());
	}

	static public void playDrawingFinishedSound() {
		playSound(SoundPreferences.getDrawingFinishedSoundFilename());
	}
}
