package com.marginallyclever.makelangelo.preferences;

import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.Translator;

public class LanguagePreferences extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8814104322237127526L;

	private JFrame rootFrame;
	
	private String[] languageList;
	private JComboBox<String> languageOptions;
	
	public LanguagePreferences(JFrame arg0) {
		this.rootFrame=arg0;
	}
	
	public void buildPanel() {
		this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		languageList = Translator.getLanguageList();
		languageOptions = new JComboBox<>(languageList);
		int currentIndex = Translator.getCurrentLanguageIndex();
		languageOptions.setSelectedIndex(currentIndex);
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		this.add(languageOptions, c);
	}
	
	public void save() {
		
	}
	
	public void cancel() {
		
	}


	/**
	 * Display a dialog box of available languages and let the user select their preference.
	 * TODO replace all strings with strings from new language.
	 */
	public void chooseLanguage() {
		this.buildPanel();

		int result;
		do {
			result = JOptionPane.showConfirmDialog(rootFrame, this, "Language", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE);
		} while(result != JOptionPane.OK_OPTION);
		
		Translator.setCurrentLanguage(languageList[languageOptions.getSelectedIndex()]);
		Translator.saveConfig();
	}
}
