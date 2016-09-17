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

public class FilePreferences extends JPanel implements ActionListener  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7977300219402211265L;
	static private Preferences prefs;
	private JFrame rootFrame;
	private JButton chooseButton;
	private JTextField fieldValue;
	
	public FilePreferences(JFrame arg0) {
		this.rootFrame=arg0;
		prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
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
		

		chooseButton = new JButton(Translator.get("Choose"));
		chooseButton.addActionListener(this);
		fieldValue = new JTextField(getTempFolder(), 32);
		this.add(new JLabel(Translator.get("MenuSoundsConnect")), label);
		label.gridy=y++;
		this.add(chooseButton, button);
		this.add(fieldValue, field);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		if (subject == chooseButton) {
			fieldValue.setText(selectFile(fieldValue.getText()));
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
		prefs.put("temp folder", fieldValue.getText());
	}
	
	public void cancel() {
	}
	
	public String getTempFolder() {
		return prefs.get("temp folder", System.getProperty("user.dir"));
	}
}
