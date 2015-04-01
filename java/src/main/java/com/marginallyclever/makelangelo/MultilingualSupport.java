package com.marginallyclever.makelangelo;

import org.apache.commons.io.IOUtils;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;


// from http://www.java-samples.com/showtutorial.php?tutorialid=152
public class MultilingualSupport {
	private static final int SUPPORTED_LANGUAGES = 4;
	protected String currentLanguage="English";
	Map<String,LanguageContainer> languages = new HashMap<String,LanguageContainer>();
	
	private Preferences prefs = Preferences.userRoot().node("Language");
	
	private static MultilingualSupport singletonObject=null;
	
	public static MultilingualSupport getSingleton() {
		if(singletonObject==null) {
			singletonObject = new MultilingualSupport();
		}
		return singletonObject;
	}

	protected MultilingualSupport() {
		LoadLanguages();
		LoadConfig();

		// Did the language file disappear?  Offer the language dialog.
		if(!languages.keySet().contains(currentLanguage)) {
			prefs.putBoolean("first time", false);
		}
		
		if(prefs.getBoolean("first time", true)) {
			ChooseLanguage();
			prefs.putBoolean("first time", false);
		}
	}
	
	
	private void SaveConfig() {
		prefs.put("language", currentLanguage );
	}
	
	private void LoadConfig() {
		currentLanguage = prefs.get("language", "English");
	}

	
	// display a dialog box of available languages and let the user select their preference.
	public void ChooseLanguage() {
		final JDialog driver = new JDialog(MainGUI.getSingleton().getParentFrame(),":) ?",true);
		driver.setLayout(new GridBagLayout());

		final String [] choices = getLanguageList();
		final JComboBox<String> language_options = new JComboBox<String>(choices);
		final JButton save = new JButton(">>>");

		GridBagConstraints c = new GridBagConstraints();
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=2;	c.gridx=0;	c.gridy=0;	driver.add(language_options,c);
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=2;  c.gridy=0;  driver.add(save,c);
		
		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();
				// TODO prevent "close" icon.  Must press save to continue!
				if(subject == save) {
					currentLanguage = choices[language_options.getSelectedIndex()];
					SaveConfig();
					driver.dispose();
				}
			}
		};

		save.addActionListener(driveButtons);

		driver.pack();
		driver.setVisible(true);
	}
	
	public void LoadLanguages() {
		LanguageContainer lang = null;
		for (SupportedLanguage supportedLanguage : SupportedLanguage.values()) {
			lang = new LanguageContainer();
			lang.Load(getClass().getClassLoader().getResourceAsStream("languages/" + supportedLanguage.name().toLowerCase() + ".xml"));
			this.languages.put(lang.getName(), lang);
		}	
	}
	
	public String get(String key) {
		String value=null;
		try {
			value = languages.get(currentLanguage).get(key);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	
	protected String [] getLanguageList() {
		final String [] choices = new String[languages.keySet().size()];
		Object[] lang_keys = languages.keySet().toArray();
		
		for(int i=0;i<lang_keys.length;++i) {
			choices[i] = (String)lang_keys[i];
		}
		
		return choices;
	}
}
