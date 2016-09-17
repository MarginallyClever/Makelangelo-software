package com.marginallyclever.makelangelo.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;


/**
 * Adjust sound preferences
 */
public class SoundPreferences extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9152926016619203157L;
	static private Preferences prefs;
	private JFrame rootFrame;
	
	private JTextField sound_connect;
	private JButton change_sound_connect;
	
	private JTextField sound_disconnect;
	private JButton change_sound_disconnect;
	
	private JTextField sound_conversion_finished;
	private JButton change_sound_conversion_finished;

	private JTextField sound_drawing_finished;
	private JButton change_sound_drawing_finished;
	
	
	public SoundPreferences(JFrame arg0) {
		this.rootFrame=arg0;
		prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.SOUND);
	}
	
	public void buildPanel() {
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		GridBagConstraints label = new GridBagConstraints();
		GridBagConstraints field = new GridBagConstraints();
		GridBagConstraints button = new GridBagConstraints();

		label.anchor = GridBagConstraints.EAST;
		label.fill = GridBagConstraints.HORIZONTAL;
		label.gridwidth = 4;
		label.gridx = 0;
		
		field.anchor = GridBagConstraints.EAST;
		field.gridwidth = 3;
		field.gridx = 0;

		button.anchor = GridBagConstraints.EAST;
		label.fill = GridBagConstraints.HORIZONTAL;
		button.gridwidth = 1;
		button.gridx = field.gridwidth + field.gridx;

		int y=0;
		
		label.gridy=y++;
		field.gridy=button.gridy=y++;
		

		change_sound_connect = new JButton(Translator.get("Choose"));
		change_sound_connect.addActionListener(this);
		sound_connect = new JTextField(prefs.get("sound_connect", ""), 32);
		this.add(new JLabel(Translator.get("MenuSoundsConnect")), label);
		label.gridy=y++;
		this.add(change_sound_connect, button);
		this.add(sound_connect, field);
		field.gridy=button.gridy=y++;

		change_sound_disconnect = new JButton(Translator.get("Choose"));
		change_sound_disconnect.addActionListener(this);
		sound_disconnect = new JTextField(prefs.get("sound_disconnect", ""), 32);
		this.add(new JLabel(Translator.get("MenuSoundsDisconnect")), label);
		label.gridy=y++;
		this.add(change_sound_disconnect, button);
		this.add(sound_disconnect, field);
		field.gridy=button.gridy=y++;

		change_sound_conversion_finished = new JButton(Translator.get("Choose"));
		change_sound_conversion_finished.addActionListener(this);
		sound_conversion_finished = new JTextField(prefs.get("sound_conversion_finished", ""), 32);
		this.add(new JLabel(Translator.get("MenuSoundsFinishConvert")), label);
		label.gridy=y++;
		this.add(change_sound_conversion_finished, button);
		this.add(sound_conversion_finished, field);
		field.gridy=button.gridy=y++;

		change_sound_drawing_finished = new JButton(Translator.get("Choose"));
		change_sound_drawing_finished.addActionListener(this);
		sound_drawing_finished = new JTextField(prefs.get("sound_drawing_finished", ""), 32);
		this.add(new JLabel(Translator.get("MenuSoundsFinishDraw")), label);
		label.gridy=y++;
		this.add(change_sound_drawing_finished, button);
		this.add(sound_drawing_finished, field);
		field.gridy=button.gridy=y++;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		if (subject == change_sound_connect) {
			sound_connect.setText(selectFile(sound_connect.getText()));
		}
		if (subject == change_sound_disconnect) {
			sound_disconnect.setText(selectFile(sound_disconnect.getText()));
		}
		if (subject == change_sound_conversion_finished) {
			sound_conversion_finished.setText(selectFile(sound_conversion_finished.getText()));
		}
		if (subject == change_sound_drawing_finished) {
			sound_drawing_finished.setText(selectFile(sound_drawing_finished.getText()));
		}
	}
	
	
	private String selectFile(String cancelValue) {
		JFileChooser choose = new JFileChooser();
		int returnVal = choose.showOpenDialog(rootFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = choose.getSelectedFile();
			return file.getAbsolutePath();
		} else {
			return cancelValue;
		}
	}
	
	public void save() {
		prefs.put("sound_connect", sound_connect.getText());
		prefs.put("sound_disconnect", sound_disconnect.getText());
		prefs.put("sound_conversion_finished", sound_conversion_finished.getText());
		prefs.put("sound_drawing_finished", sound_drawing_finished.getText());
	}
	
	public void cancel() {
		
	}
	
	public String getConnectSoundFilename() {
		return prefs.get("sound_connect", "");
	}
	public String getDisonnectSoundFilename() {
		return prefs.get("sound_disconnect", "");
	}
	public String getConversionFinishedSoundFilename() {
		return prefs.get("sound_conversion_finished", "");
	}
	public String getDrawingFinishedSoundFilename() {
		return prefs.get("sound_drawing_finished", "");
	}
}
