package com.marginallyclever.convenience;

import com.marginallyclever.makelangelo.makelangeloSettingsPanel.SoundPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

@Deprecated
public class SoundSystem {

	private static final Logger logger = LoggerFactory.getLogger(SoundSystem.class);

	static public void playSound(String url) {
		if (url.isEmpty()) return;

		try {
			Clip clip = AudioSystem.getClip();
			BufferedInputStream x = new BufferedInputStream(new FileInputStream(url));
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(x);
			clip.open(inputStream);
			clip.start();
		} catch (Exception e) {
			logger.error("Failed to play sound", e);
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
