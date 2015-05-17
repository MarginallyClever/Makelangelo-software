package com.marginallyclever.makelangelo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;


// from http://www.java-samples.com/showtutorial.php?tutorialid=152
public class MultilingualSupport {
	public static final String FIRST_TIME_KEY = "first time";
	/**
	 *
	 */
	private static final String LANGUAGE_KEY = "language";
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
		prefs.put(LANGUAGE_KEY, currentLanguage );
	}
	
	public void loadConfig() {
		currentLanguage = prefs.get(LANGUAGE_KEY, "English");
	}
	
	public void loadLanguages() {
		// Scan folder for language files
        String workingDirectory=System.getProperty("user.dir")+File.separator+"languages";
        //System.out.println(workingDirectory);
		File f = new File(workingDirectory);
		LanguageContainer lang;

		File [] all_files = f.listFiles();
		try {
			if(all_files.length<=0) {
				throw new Exception("No language files found!");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		for(int i=0;i<all_files.length;++i) {
			if(all_files[i].isHidden()) continue;
			if(all_files[i].isDirectory()) continue;
			// get extension
			int j = all_files[i].getPath().lastIndexOf('.');
			if (j <= 0) continue;  // no extension
			if(all_files[i].getPath().substring(j+1).toLowerCase().equals("xml")==false) continue;  // only .XML or .xml files
			lang = new LanguageContainer();
			lang.Load(all_files[i].getAbsolutePath());
			languages.put(lang.getName(), lang);
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
