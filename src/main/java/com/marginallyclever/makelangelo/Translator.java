package com.marginallyclever.makelangelo;

import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * <p></p>MultilingualSupport is the translation engine.  You ask for a string it finds the matching string in the currently selected language.
 * See <a href="http://www.java-samples.com/showtutorial.php?tutorialid=152">XML and Java - Parsing XML using Java Tutorial</a></p>
 * <p>TODO: This system loads all language files even though it only displays one at a time.  It could load a single language and save memory.</p>
 * @author Dan Royer
 * @author Peter Colapietro
 */
public final class Translator {
	private static final Logger logger = LoggerFactory.getLogger(Translator.class);

	public static final String MISSING = "Missing:";
	// Working directory. This represents the directory where the java executable launched the jar from.
	public static final String WORKING_DIRECTORY = /*File.separator + */"languages"/*+File.separator*/;
	// The name of the preferences node containing the user's choice.
	private static final String LANGUAGE_KEY = "language";
	// TODO get a better way to store user preference.
	private static final Preferences languagePreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LANGUAGE);
	// The default choice when nothing has been selected.
	private static String defaultLanguage = "English";
	// The current choice
	private static String currentLanguage;
	private static ResourceBundle bundle;

	public static void start() {
		logger.info("starting translator...");

		Locale locale = Locale.getDefault();
		defaultLanguage = locale.getDisplayLanguage(Locale.ENGLISH);
		logger.info("Default language = {}", defaultLanguage);

		loadConfig();
	}

	private static List<String> findAvailableBundles(String baseName) throws IOException {
		List<String> locales = new ArrayList<>();
		Enumeration<URL> resources = ClassLoader.getSystemClassLoader().getResources("");

		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			URLConnection conn = resource.openConnection();
			if (!(conn instanceof JarURLConnection jarConnection)) {
				continue;
			}
			JarFile jar = jarConnection.getJarFile();
			jar.stream()
					.map(JarEntry::getName)
					.filter(name -> name.startsWith(baseName) && name.endsWith(".properties"))
					.forEach(name -> locales.add(name.replace(baseName + "_", "").replace(".properties", "")));
		}
		return locales;
	}
	

	/**
	 * @return true if this is the first time loading language files (probably on install)
	 */
	public static boolean isThisTheFirstTimeLoadingLanguageFiles() {
		// Did the language file disappear?  Offer the language dialog.
		try {
			if (doesLanguagePreferenceExist()) {
				// does the list of languages contain the preferred choice?
				String languageNameFromPref = languagePreferenceNode.get(LANGUAGE_KEY, defaultLanguage);

				if (Arrays.stream(getLanguageList()).noneMatch(languageNameFromPref::equals)) {
					logger.error("Language '{}' not available ...", languageNameFromPref);

					// To avoid some null issues in Translator.get(String key),
					// lets say it's the first run (to ask the user to select a valid language name)
					return true;
				}
				return false;
			}
		} catch (BackingStoreException e) {
			logger.error("Failed to load language", e);
		}
		return true;
	}

	/**
	 * @return true if a preferences node exists
	 * @throws BackingStoreException
	 */
	static private boolean doesLanguagePreferenceExist() throws BackingStoreException {
		return Arrays.asList(languagePreferenceNode.keys()).contains(LANGUAGE_KEY);
	}

	/**
	 * save the user's current language choice
	 */
	public static void saveConfig() {
		logger.debug("saveConfig()");
		languagePreferenceNode.put(LANGUAGE_KEY, currentLanguage);
	}

	/**
	 * load the user's language choice
	 */
	public static void loadConfig() {
		logger.debug("loadConfig: {}={}", languagePreferenceNode.toString(), defaultLanguage);
		setCurrentLanguage(languagePreferenceNode.get(LANGUAGE_KEY, defaultLanguage));
	}

	/**
	 * @param key name of key to find in translation list.  <b>Keys must be Strings, not variables</b>.
	 *            If you use a variable then the tests for missing and duplicate translations will not
	 *            work in the distant future.
	 * @return the translated value for key, or "missing:key".
	 */
	public static String get(String key) {
		try {
			return bundle.getString(key);
		} catch(Exception e) {
			logger.warn("Missing translation '{}' in language '{}'", key, currentLanguage);
			return MISSING +key;
		}
	}

	/**
	 * Translates a string and fills in some details.  String contains the special character sequence "%N", where N is the n-th parameter passed to get()
	 * A %1 is replaced with the first parameter, %2 with the second, and so on.  There is no escape character.
	 * @param key name of key to find in translation list
	 * @param params the values to replace the %N with
	 * @return the translated value for key, or "missing:key".
	 */
	public static String get(String key,String [] params) {
		String modified = get(key);
		int n=1;
		for(String p : params) {
			modified = modified.replaceAll("%"+n, p);
			++n;
		}
		return modified;
	}

	/**
	 * @return the list of language names
	 */
	public static String[] getLanguageList() {
		String[] results = scanForResourceBundle();
		if(results.length == 0) {
			return new String[] {"en","de"};//,"fr","ar","nl","cn"
		}
		return results;
	}

	private static String[] scanForResourceBundle() {
		try {
			var availableLocales = findAvailableBundles("messages");
			return availableLocales.toArray(new String[0]);
		} catch(Exception e) {
			logger.error("Failed to load language list", e);
		}

		return new String [] {};
	}

	/**
	 * @param language the name of the language to make active.
	 */
	public static void setCurrentLanguage(String language) {
		currentLanguage = language;
		var locale = Locale.forLanguageTag(language);
		bundle = ResourceBundle.getBundle("messages",locale);
	}

	/**
	 * @return the index of the current language
	 */
	public static int getCurrentLanguageIndex() {
		String [] set = getLanguageList();
		// find the current language
		for( int i=0;i<set.length; ++i) {
			if( set[i].equals(Translator.currentLanguage)) return i;
		}
		// now try the default
		for( int i=0;i<set.length; ++i) {
			if( set[i].equals(Translator.defaultLanguage)) return i;
		}
		// failed both, return 0 for the first option.
		return 0;
	}
}
