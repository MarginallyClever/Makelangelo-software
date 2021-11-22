package com.marginallyclever.makelangelo.makelangeloSettingsPanel;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.ScaleTurtlePanel;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;

public class LanguagePreferences {
	static private SelectPanel panel;
	static private String[] languageList;
	static private SelectOneOfMany languageOptions;
	
	
	static SelectPanel buildPanel() {
		panel = new SelectPanel();
		
		languageList = Translator.getLanguageList();
		int currentIndex = Translator.getCurrentLanguageIndex();
		languageOptions = new SelectOneOfMany("language","Language",languageList,currentIndex);
		
		panel.add(languageOptions);
		panel.finish();

		return panel;
	}
	
	static public void save() {}
	
	static public void cancel() {}

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
		
		Translator.setCurrentLanguage(languageList[languageOptions.getSelectedIndex()]);
		Translator.saveConfig();
	}
	
	// TEST
	
	public static void main(String[] args) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}

		try {
			chooseLanguage();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
}
