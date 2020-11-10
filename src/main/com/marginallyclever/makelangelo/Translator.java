package com.marginallyclever.makelangelo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.preferences.LanguagePreferences;
import com.marginallyclever.util.MarginallyCleverTranslationXmlFileHelper;
import com.marginallyclever.util.PreferencesHelper;

/**
 * MultilingualSupport is the translation engine.  You ask for a string it finds the matching string in the currently selected language.
 *
 * @author dan royer
 * @author Peter Colapietro
 * See <a href="http://www.java-samples.com/showtutorial.php?tutorialid=152">XML and Java - Parsing XML using Java Tutorial</a>
 */
public final class Translator {

	/**
	 * Working directory. This represents the directory where the java executable launched the jar from.
	 */
	public static final String WORKING_DIRECTORY = /*File.separator + */"languages"/*+File.separator*/;


	/**
	 * The name of the preferences node containing the user's choice.
	 */
	private static final String LANGUAGE_KEY = "language";

	/**
	 *
	 */
	private static final Preferences languagePreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LANGUAGE);


	/**
	 * The default choice when nothing has been selected.
	 */
	private static String defaultLanguage = "English";

	/**
	 * The current choice
	 */
	private static String currentLanguage;

	/**
	 * a list of all languages and their translations strings
	 */
	private static final Map<String, TranslatorLanguage> languages = new HashMap<>();

	/**
	 *
	 */
	static public void start() {
		Log.message("starting translator...");
		
		Locale locale = Locale.getDefault();
		defaultLanguage = locale.getDisplayLanguage(Locale.ENGLISH);
		Log.message("Default language = "+defaultLanguage);
		
		loadLanguages();
		loadConfig();

		if (isThisTheFirstTimeLoadingLanguageFiles()) {
			LanguagePreferences.chooseLanguage();
		}
	}
	

	/**
	 * @return true if this is the first time loading language files (probably on install)
	 */
	static private boolean isThisTheFirstTimeLoadingLanguageFiles() {
		// Did the language file disappear?  Offer the language dialog.
		try {
			if (doesLanguagePreferenceExist()) {
				return false;
			}
		} catch (BackingStoreException e) {
			Log.error(e.getMessage());
		}
		return true;
	}

	/**
	 * @return true if a preferences node exists
	 * @throws BackingStoreException
	 */
	static private boolean doesLanguagePreferenceExist() throws BackingStoreException {
		if (Arrays.asList(languagePreferenceNode.keys()).contains(LANGUAGE_KEY)) {
			return true;
		}
		return false;
	}

	/**
	 * save the user's current langauge choice
	 */
	static public void saveConfig() {
		languagePreferenceNode.put(LANGUAGE_KEY, currentLanguage);
	}

	/**
	 * load the user's language choice
	 */
	static public void loadConfig() {
		currentLanguage = languagePreferenceNode.get(LANGUAGE_KEY, defaultLanguage);
	}


	/**
	 * Scan folder for language files.
	 * See http://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file
	 * @throws IllegalStateException No language files found
	 */
	static public void loadLanguages() {
		try {			
			URI uri = Translator.class.getClassLoader().getResource(WORKING_DIRECTORY).toURI();
			Log.message("Looking for translations in "+uri.toString());
			
			Path myPath;
			if (uri.getScheme().equals("jar")) {
				FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
				myPath = fileSystem.getPath(WORKING_DIRECTORY);
			} else {
				myPath = Paths.get(uri);
			}

			Path rootPath = FileSystems.getDefault().getPath(System.getProperty("user.dir"));
			Log.message("rootDir="+rootPath.toString());
			
			// we'll look inside the JAR file first, then look in the working directory.
			// this way new translation files in the working directory will replace the old
			// JAR files.
			int found=0;
			Stream<Path> walk = Stream.concat(
					Files.walk(myPath, 1),	// check inside the JAR file.
					Files.walk(rootPath,1)	// then check the working directory
					);
			Iterator<Path> it = walk.iterator();
			while( it.hasNext() ) {
				Path p = it.next();
				String name = p.toString();
				//if( f.isDirectory() || f.isHidden() ) continue;
				if( FilenameUtils.getExtension(name).equalsIgnoreCase("xml") ) {
					// found an XML file in the /languages folder.  Good sign!
					String nameInsideJar = WORKING_DIRECTORY+"/"+FilenameUtils.getName(name);
					InputStream stream = Translator.class.getClassLoader().getResourceAsStream(nameInsideJar);
					String actualFilename = "Jar:"+nameInsideJar;
					File externalFile = new File(name);
					if(externalFile.exists()) {
						stream = new FileInputStream(new File(name));
						actualFilename = name;
					}
					if( stream != null ) {
						Log.message("Found "+actualFilename);
						TranslatorLanguage lang = new TranslatorLanguage();
						try {
							lang.loadFromInputStream(stream);
						} catch(Exception e) {
							Log.error("Failed to load "+actualFilename);
							// if the xml file is invalid then an exception can occur.
							// make sure lang is empty in case of a partial-load failure.
							lang = new TranslatorLanguage();
						}
						
						if( !lang.getName().isEmpty() && 
							!lang.getAuthor().isEmpty()) {
							// we loaded a language file that seems pretty legit.
							languages.put(lang.getName(), lang);
							++found;
						}
					}
				}
			}
			walk.close();
			
			//Log.message("total found: "+found);
	
			if(found==0) {
				throw new IllegalStateException("No translations found.");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		catch (IllegalStateException e) {
			Log.error( e.getMessage()+". Defaulting to "+defaultLanguage+". Language folder expected to be located at "+ WORKING_DIRECTORY);
			final TranslatorLanguage languageContainer  = new TranslatorLanguage();
			String path = MarginallyCleverTranslationXmlFileHelper.getDefaultLanguageFilePath();
			Log.message("default path requested: "+path);
			URL pathFound = Translator.class.getClassLoader().getResource(path);
			Log.message("path found: "+pathFound);
			try (InputStream s = pathFound.openStream()) {
				languageContainer.loadFromInputStream(s);
			} catch (IOException ie) {
				Log.error(ie.getMessage());
			}
			languages.put(languageContainer.getName(), languageContainer);
		}
	}

	/**
	 * @param name of key to find in translation list
	 * @return the translated value for key, or "missing:key".
	 */
	static public String get(String key) {
		return languages.get(currentLanguage).get(key);
	}

	/**
	 * @return the list of language names
	 */
	static public String[] getLanguageList() {
		final String[] choices = new String[languages.keySet().size()];
		final Object[] lang_keys = languages.keySet().toArray();

		for (int i = 0; i < lang_keys.length; ++i) {
			choices[i] = (String) lang_keys[i];
		}

		return choices;
	}

	/**
	 * @param currentLanguage the name of the language to make active.
	 */
	static public void setCurrentLanguage(String currentLanguage) {
		Translator.currentLanguage = currentLanguage;
	}

	static public int getCurrentLanguageIndex() {
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
