package com.marginallyclever.makelangelo;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;


// from http://www.java-samples.com/showtutorial.php?tutorialid=152
public class MultilingualSupport {
	public static final String FIRST_TIME_KEY = "first time";
	protected String currentLanguage="English";
	private final Map<String,LanguageContainer> languages = new HashMap<>();
	
	private Preferences prefs = Preferences.userRoot().node("Language");
	

	public MultilingualSupport() {
		loadLanguages();
		loadConfig();
	}

	public boolean isThisTheFirstTime() {
		// Did the language file disappear?  Offer the language dialog.
		if(!languages.keySet().contains(currentLanguage)) {
			prefs.putBoolean(FIRST_TIME_KEY, false);
		}

		return prefs.getBoolean(FIRST_TIME_KEY, true);
	}


	public void saveConfig() {
		prefs.put("language", currentLanguage );
	}
	
	public void loadConfig() {
		currentLanguage = prefs.get("language", "English");
	}

	public void loadLanguages() {
		LanguageContainer lang;
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
