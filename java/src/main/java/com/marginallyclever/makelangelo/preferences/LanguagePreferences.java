package com.marginallyclever.makelangelo.preferences;

import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.Translator;

public class LanguagePreferences {
	static private String[] languageList;
	static private JComboBox<String> languageOptions;
	
	
	static public JPanel buildPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		languageList = Translator.getLanguageList();
		languageOptions = new JComboBox<>(languageList);
		int currentIndex = Translator.getCurrentLanguageIndex();
		languageOptions.setSelectedIndex(currentIndex);
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(languageOptions, c);
		
		return panel;
	}
	
	static public void save() {}
	
	static public void cancel() {}


	/**
	 * Display a dialog box of available languages and let the user select their preference.
	 * TODO replace all strings with strings from new language.
	 */
	static public void chooseLanguage() {
		JPanel panel = buildPanel();

		int result;
		do {
			result = JOptionPane.showConfirmDialog(null, panel, "Language", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE);
		} while(result != JOptionPane.OK_OPTION);
		
		Translator.setCurrentLanguage(languageList[languageOptions.getSelectedIndex()]);
		Translator.saveConfig();
	}
}
