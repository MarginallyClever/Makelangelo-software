package com.marginallyclever.makelangelo;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.util.MarginallyCleverTranslationXmlFileHelper;
import com.marginallyclever.util.PreferencesHelper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

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
	// a list of all languages and their translations strings
	private static final Map<String, TranslatorLanguage> languages = new HashMap<>();
	private static TranslatorLanguage currentLanguageContainer;

	public static void start() {
		logger.info("starting translator...");
		
		Locale locale = Locale.getDefault();
		defaultLanguage = locale.getDisplayLanguage(Locale.ENGLISH);
		logger.info("Default language = {}", defaultLanguage);
		
		loadLanguages();
		loadConfig();
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
				if (!languages.containsKey(languageNameFromPref)) {
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
	 * Scan folder for language files.
	 * See http://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file
	 * @throws IllegalStateException No language files found
	 */
	public static void loadLanguages() {
		languages.clear();
		
		try {
			int found=0;
			Stream<Path> walk = getLanguagePaths();
			Iterator<Path> it = walk.iterator();
			while (it.hasNext()) {
				Path p = it.next();
				String name = p.toString();
				//if( f.isDirectory() || f.isHidden() ) continue;
				if (FilenameUtils.getExtension(name).equalsIgnoreCase("xml") ) {
					if (name.endsWith("pom.xml")) {
						continue;
					}
					
					// found an XML file in the /languages folder.  Good sign!
					if (attemptToLoadLanguageXML(name)) found++;
				}
			}
			walk.close();
			
			//logger.debug("total found: "+found);
	
			if(found==0) {
				throw new IllegalStateException("No translations found.");
			}
		}
		catch(Exception e) {
			logger.error("{}. Defaulting to {}. Language folder expected to be located at {}", e.getMessage(), defaultLanguage, WORKING_DIRECTORY);
			final TranslatorLanguage languageContainer  = new TranslatorLanguage();
			String path = MarginallyCleverTranslationXmlFileHelper.getDefaultLanguageFilePath();
			logger.debug("default path requested: {}", path);
			URL pathFound = Translator.class.getClassLoader().getResource(path);
			logger.debug("path found: {}", pathFound);
			try (InputStream s = pathFound.openStream()) {
				languageContainer.loadFromInputStream(s);
			} catch (IOException ie) {
				logger.error(ie.getMessage());
			}
			languages.put(languageContainer.getName(), languageContainer);
		}
	}

	private static Stream<Path> getLanguagePaths() throws Exception {
		URI uri = Translator.class.getClassLoader().getResource(WORKING_DIRECTORY).toURI();
		logger.trace("Looking for translations in {}", uri.toString());
		
		Path myPath;
		if (uri.getScheme().equals("jar")) {
			FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
			myPath = fileSystem.getPath(WORKING_DIRECTORY);
		} else {
			myPath = Paths.get(uri);
		}

		Path rootPath = FileSystems.getDefault().getPath(FileAccess.getWorkingDirectory());
		logger.trace("rootDir={}", rootPath);

		// we'll look inside the JAR file first, then look in the working directory.
		// this way new translation files in the working directory will replace the old JAR files.
		Stream<Path> walk = Stream.concat(
				Files.walk(myPath, 1),	// check inside the JAR file.
				Files.walk(rootPath,1)	// then check the working directory
				);

		return walk;
	}


	private static boolean attemptToLoadLanguageXML(String name) throws Exception {
		InputStream stream;
		String actualFilename;

		File externalFile = new File(name);
		if(externalFile.exists()) {
			stream = new FileInputStream(name);
			actualFilename = name;
		} else {
			String nameInsideJar = WORKING_DIRECTORY+"/"+FilenameUtils.getName(name);
			stream = Translator.class.getClassLoader().getResourceAsStream(nameInsideJar);
			actualFilename = "Jar:"+nameInsideJar;
		}
		if( stream == null ) return false;

		logger.trace("Found {}", actualFilename);
		TranslatorLanguage lang = new TranslatorLanguage();
		try {
			lang.loadFromInputStream(stream);
		} catch(Exception e) {
			logger.error("Failed to load {}", actualFilename, e);
			// if the xml file is invalid then an exception can occur.
			// make sure lang is empty in case of a partial-load failure.
			lang = new TranslatorLanguage();
		}

		stream.close();

		if( !lang.getName().isEmpty() &&
			!lang.getAuthors().isEmpty()) {
			// we loaded a language file that seems pretty legit.
			languages.put(lang.getName(), lang);
			return true;
		}

		return false;
	}

	/**
	 * @param key name of key to find in translation list.  <b>Keys must be Strings, not variables</b>.
	 *            If you use a variable then the tests for missing and duplicate translations will not
	 *            work in the distant future.
	 * @return the translated value for key, or "missing:key".
	 */
	public static String get(String key) {
		String translation = currentLanguageContainer.get(key);
		if (translation == null) {
			logger.warn("Missing translation '{}' in language '{}'", key, currentLanguage);
			return MISSING +key;
		}
		return translation;
	}

	/**
	 * Translates a string and fills in some details.  String contains the special character sequence "%N", where N is the n-th parameter passed to get()
	 * A %1 is replaced with the first parameter, %2 with the second, and so on.  There is no escape character.
	 * @param key name of key to find in translation list
	 * @param params 
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
		final String[] choices = new String[languages.keySet().size()];
		final Object[] lang_keys = languages.keySet().toArray();

		for (int i = 0; i < lang_keys.length; ++i) {
			choices[i] = (String) lang_keys[i];
		}

		return choices;
	}

	/**
	 * @param language the name of the language to make active.
	 */
	public static void setCurrentLanguage(String language) {
		currentLanguage = language;
		currentLanguageContainer = languages.get(language);
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
