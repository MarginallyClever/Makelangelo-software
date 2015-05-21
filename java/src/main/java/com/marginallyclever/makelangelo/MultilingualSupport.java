package com.marginallyclever.makelangelo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 *
 * FIXME Write Javadoc.
 *
 * @see <a href="http://www.java-samples.com/showtutorial.php?tutorialid=152">XML and Java - Parsing XML using Java Tutorial</a>
 */
public class MultilingualSupport {

	/**
	 *
	 */
	private static final String FIRST_TIME_KEY = "first time";

	/**
	 *
	 */
	private static final String LANGUAGE_KEY = "language";

	/**
	 *
	 */
	protected String currentLanguage="English";

	/**
	 *
	 */
	private final Map<String,LanguageContainer> languages = new HashMap<>();

	/**
	 *
	 */
	private Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LANGUAGE);

	/**
	 *
	 */
	public MultilingualSupport() {
		loadLanguages();
		loadConfig();
	}

	/**
	 *
	 * @return
	 */
	public boolean isThisTheFirstTime() {
		// Did the language file disappear?  Offer the language dialog.
		if(!languages.keySet().contains(currentLanguage)) {
			prefs.putBoolean(FIRST_TIME_KEY, false);
		}

		return prefs.getBoolean(FIRST_TIME_KEY, true);
	}

	/**
	 *
	 */
	public void saveConfig() {
		prefs.put(LANGUAGE_KEY, currentLanguage );
	}

	/**
	 *
	 */
	public void loadConfig() {
		currentLanguage = prefs.get(LANGUAGE_KEY, "English");
	}

	/**
	 * Scan folder for language files.
	 */
	public void loadLanguages() throws IllegalStateException {
        String workingDirectory=System.getProperty("user.dir")+File.separator+"languages";
		final File f = new File(workingDirectory);
		final File [] all_files = f.listFiles();
		if(all_files.length<=0) {
			throw new IllegalStateException("No language files found!");
		}
		createLanguageContainersFromLanguageFiles(all_files);
	}

	/**
	 *
	 * @param all_files
	 */
	private void createLanguageContainersFromLanguageFiles(File[] all_files) {
		LanguageContainer lang;
		for(int i=0;i<all_files.length;++i) {
			if(all_files[i].isHidden()) continue;
			if(all_files[i].isDirectory()) continue;
			// get extension
			int j = all_files[i].getPath().lastIndexOf('.');
			if (j <= 0) continue;  // no extension
			if(all_files[i].getPath().substring(j+1).toLowerCase().equals("xml")==false) continue;  // only .XML or .xml files
			lang = new LanguageContainer();
			lang.load(all_files[i].getAbsolutePath());
			languages.put(lang.getName(), lang);
		}
	}

	/**
	 *
	 * @param key
	 * @return
	 */
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

	/**
	 *
	 * @return
	 */
	protected String [] getLanguageList() {
		final String [] choices = new String[languages.keySet().size()];
		final Object[] lang_keys = languages.keySet().toArray();
		
		for(int i=0;i<lang_keys.length;++i) {
			choices[i] = (String)lang_keys[i];
		}
		
		return choices;
	}
}
