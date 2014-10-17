package Makelangelo;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;


// from http://www.java-samples.com/showtutorial.php?tutorialid=152
public class MultilingualSupport {
	protected String currentLanguage="english";
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
		if(prefs.getBoolean("first time", true)) {
			ChooseLanguage();
			prefs.putBoolean("first time", false);
		}
		LoadLanguages();
		currentLanguage = prefs.get("language", "english");
	}
	
	
	// display a dialog box of available languages and let the user select their preference.
	public void ChooseLanguage() {
		
	}
	
	public void LoadLanguages() {
		// @TODO scan folder for language files?
		LanguageContainer lang = new LanguageContainer();
		lang.Load("english");
		languages.put("english", lang);
	}
	
	public String get(String key) {
		return languages.get(currentLanguage).get(key);
	}
}
