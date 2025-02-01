package com.marginallyclever.makelangelo.applicationsettings;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class LanguagePreferences {
	private static final Logger logger = LoggerFactory.getLogger(LanguagePreferences.class);
	
	static private final SelectPanel panel = new SelectPanel();
	static private final String[] languageList = Translator.getLanguageList();
	static private SelectOneOfMany languageOptions;

	static SelectPanel buildPanel() {
		int currentIndex = Translator.getCurrentLanguageIndex();
		languageOptions = new SelectOneOfMany("language","Language",languageList,currentIndex);

		panel.clear();
		panel.add(languageOptions);
		panel.add(new SelectReadOnlyText("learnMore",
				"<![CDATA[<html><body><a href=\"https://crowdin.com/project/makelangelo\">Add translations</a></body></html>]]>"));

		return panel;
	}
	
	static public void save() {
		logger.debug("Changing to language {}", languageList[languageOptions.getSelectedIndex()]);
		Translator.setCurrentLanguage(languageList[languageOptions.getSelectedIndex()]);
		Translator.saveConfig();
	}

	/**
	 * Display a dialog box of available languages and let the user select their preference.
	 * TODO when language changes, restart app OR replace all strings with new language.
	 */
	static public void chooseLanguage() {
		SelectPanel panel = buildPanel();

		int result;
		do {
			result = JOptionPane.showConfirmDialog(null, panel, "Language", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE);
		} while(result != JOptionPane.OK_OPTION);
		
		save();
	}
	
	// TEST
	
	public static void main(String[] args) throws Exception {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		chooseLanguage();
	}
}
