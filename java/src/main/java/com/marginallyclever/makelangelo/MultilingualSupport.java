package com.marginallyclever.makelangelo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * FIXME Write Javadoc.
 *
 * @see <a href="http://www.java-samples.com/showtutorial.php?tutorialid=152">XML and Java - Parsing XML using Java Tutorial</a>
 */
public final class MultilingualSupport {

	/**
	 *
	 */
	private static final String LANGUAGE_KEY = "language";

	/**
	 *
	 */
	public static final String DEFAULT_LANGUAGE = "English";

	/**
	 *
	 */
	private String currentLanguage;

	/**
	 *
	 */
	private final Map<String,LanguageContainer> languages = new HashMap<>();

	/**
	 *
	 */
	private final Preferences languagePreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LANGUAGE);

	/**
	 *
	 */
	private final Logger logger = LoggerFactory.getLogger(MultilingualSupport.class);

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
	public boolean isThisTheFirstTimeLoadingLanguageFiles() {
		// Did the language file disappear?  Offer the language dialog.
		try {
			if (doesLanguagePreferenceExist()) {
				return false;
			}
		} catch (BackingStoreException e) {
			logger.error("{}",e);
		}
		return true;
	}

	/**
	 *
	 * @return
	 * @throws BackingStoreException
	 */
	private boolean doesLanguagePreferenceExist() throws BackingStoreException {
		if(Arrays.asList(languagePreferenceNode.keys()).contains(LANGUAGE_KEY)) {
			return true;
		}
		return false;
	}

	/**
	 *
	 */
	public void saveConfig() {
		languagePreferenceNode.put(LANGUAGE_KEY, currentLanguage);
	}

	/**
	 *
	 */
	public void loadConfig() {
		currentLanguage = languagePreferenceNode.get(LANGUAGE_KEY, DEFAULT_LANGUAGE);
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

	public void setCurrentLanguage(String currentLanguage) {
		this.currentLanguage = currentLanguage;
	}
}
