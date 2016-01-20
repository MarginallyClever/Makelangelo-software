package com.marginallyclever.makelangelo;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.prefs.Preferences;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

public class SoundSystem {
	@SuppressWarnings("deprecation")
	private Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);




	private String selectFile(Frame owner) {
		JFileChooser choose = new JFileChooser();
		int returnVal = choose.showOpenDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = choose.getSelectedFile();
			return file.getAbsolutePath();
		} else {
			//System.out.println("File access cancelled by user.");
			return "";
		}
	}

	/**
	 * Adjust sound preferences
	 */
	protected void adjust(final Frame owner, Translator translator) {
		final JDialog driver = new JDialog(owner, Translator.get("MenuSoundsTitle"), true);
		driver.setLayout(new GridBagLayout());
		
		final JTextField sound_connect = new JTextField(prefs.get("sound_connect", ""), 32);
		final JTextField sound_disconnect = new JTextField(prefs.get("sound_disconnect", ""), 32);
		final JTextField sound_conversion_finished = new JTextField(prefs.get("sound_conversion_finished", ""), 32);
		final JTextField sound_drawing_finished = new JTextField(prefs.get("sound_drawing_finished", ""), 32);

		final JButton change_sound_connect = new JButton(Translator.get("MenuSoundsConnect"));
		final JButton change_sound_disconnect = new JButton(Translator.get("MenuSoundsDisconnect"));
		final JButton change_sound_conversion_finished = new JButton(Translator.get("MenuSoundsFinishConvert"));
		final JButton change_sound_drawing_finished = new JButton(Translator.get("MenuSoundsFinishDraw"));

		//final JCheckBox allow_metrics = new JCheckBox(String.valueOf("I want to add the distance drawn to the // total"));
		//allow_metrics.setSelected(allowMetrics);

		final JButton cancel = new JButton(Translator.get("Cancel"));
		final JButton save = new JButton(Translator.get("Save"));

		GridBagConstraints c = new GridBagConstraints();
		//c.gridwidth=4;  c.gridx=0;  c.gridy=0;  driver.add(allow_metrics,c);

		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 3;
		driver.add(change_sound_connect, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 3;
		driver.add(sound_connect, c);
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 4;
		driver.add(change_sound_disconnect, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 4;
		driver.add(sound_disconnect, c);
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 5;
		driver.add(change_sound_conversion_finished, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 5;
		driver.add(sound_conversion_finished, c);
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 6;
		driver.add(change_sound_drawing_finished, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 6;
		driver.add(sound_drawing_finished, c);

		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 2;
		c.gridy = 12;
		driver.add(save, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 3;
		c.gridy = 12;
		driver.add(cancel, c);

		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();
				if (subject == change_sound_connect) sound_connect.setText(selectFile(owner));
				if (subject == change_sound_disconnect) sound_disconnect.setText(selectFile(owner));
				if (subject == change_sound_conversion_finished) sound_conversion_finished.setText(selectFile(owner));
				if (subject == change_sound_drawing_finished) sound_drawing_finished.setText(selectFile(owner));

				if (subject == save) {
					prefs.put("sound_connect", sound_connect.getText());
					prefs.put("sound_disconnect", sound_disconnect.getText());
					prefs.put("sound_conversion_finished", sound_conversion_finished.getText());
					prefs.put("sound_drawing_finished", sound_drawing_finished.getText());
					driver.dispose();
				}
				if (subject == cancel) {
					driver.dispose();
				}
			}
		};

		change_sound_connect.addActionListener(driveButtons);
		change_sound_disconnect.addActionListener(driveButtons);
		change_sound_conversion_finished.addActionListener(driveButtons);
		change_sound_drawing_finished.addActionListener(driveButtons);

		save.addActionListener(driveButtons);
		cancel.addActionListener(driveButtons);
		driver.getRootPane().setDefaultButton(save);
		driver.pack();
		driver.setVisible(true);
	}
	
	
	private void playSound(String url) {
		if (url.isEmpty()) return;

		try {
			Clip clip = AudioSystem.getClip();
			BufferedInputStream x = new BufferedInputStream(new FileInputStream(url));
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(x);
			clip.open(inputStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}

	public void playConnectSound() {
		playSound(prefs.get("sound_connect", ""));
	}

	public void playDisconnectSound() {
		playSound(prefs.get("sound_disconnect", ""));
	}

	public void playConversionFinishedSound() {
		playSound(prefs.get("sound_conversion_finished", ""));
	}

	public void playDrawingFinishedSound() {
		playSound(prefs.get("sound_drawing_finished", ""));
	}
}
