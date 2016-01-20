package com.marginallyclever.makelangelo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;

import com.marginallyclever.util.MarginallyCleverTranslationXmlFileHelper;

/**
 * MultilingualSupport is the translation engine.  You ask for a string it finds the matching string in the currently selected language.
 *
 * @author dan royer
 * @author Peter Colapietro
 * @see <a href="http://www.java-samples.com/showtutorial.php?tutorialid=152">XML and Java - Parsing XML using Java Tutorial</a>
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
		// find the english name of the default language.
		Locale locale = Locale.getDefault();
		defaultLanguage = locale.getDisplayLanguage(Locale.ENGLISH);
		//System.out.println("Default language = "+defaultLanguage);
		
		// load the languages
		try {
			loadLanguages();
		} catch (IllegalStateException e) {
			Log.error( e.getMessage()+". Defaulting to "+defaultLanguage+". Language folder expected to be located at "+ WORKING_DIRECTORY);
			final TranslatorLanguage languageContainer  = new TranslatorLanguage();
			String path = MarginallyCleverTranslationXmlFileHelper.getDefaultLanguageFilePath();
			System.out.println("default path requested: "+path);
			URL pathFound = Translator.class.getClassLoader().getResource(path);
			System.out.println("path found: "+pathFound);
			try (InputStream s = pathFound.openStream()) {
				languageContainer.loadFromInputStream(s);
			} catch (IOException ie) {
				Log.error(ie.getMessage());
			}
			languages.put(languageContainer.getName(), languageContainer);
		}
		loadConfig();

		if (isThisTheFirstTimeLoadingLanguageFiles()) {
			chooseLanguage();
		}
	}


	// display a dialog box of available languages and let the user select their preference.
	static public void chooseLanguage() {
		JPanel panel = new JPanel(new GridBagLayout());

		final String[] languageList = getLanguageList();
		final JComboBox<String> languageOptions = new JComboBox<>(languageList);
		int currentIndex = getCurrentLanguageIndex();
		languageOptions.setSelectedIndex(currentIndex);
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(languageOptions, c);
		
		int result;
		do {
			result = JOptionPane.showConfirmDialog(null, panel, "Language", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE);
		} while(result != JOptionPane.OK_OPTION);
		
		setCurrentLanguage(languageList[languageOptions.getSelectedIndex()]);
		saveConfig();
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
	 * @see http://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file
	 * @throws IllegalStateException No language files found
	 */
	static public void loadLanguages() throws IllegalStateException {
		// iterate and find all language files
		URI uri = null;
		try {
			uri = Translator.class.getClassLoader().getResource(WORKING_DIRECTORY).toURI();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Path myPath;
		if (uri.getScheme().equals("jar")) {
			FileSystem fileSystem = null;
			try {
				fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
			} catch (IOException e) {
				e.printStackTrace();
			}
			myPath = fileSystem.getPath(WORKING_DIRECTORY);
		} else {
			myPath = Paths.get(uri);
		}
		Stream<Path> walk = null;
		try {
			walk = Files.walk(myPath, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int found=0;
		Iterator<Path> it = walk.iterator();
		while( it.hasNext() ) {
			Path p = it.next();
			String name = p.toString();
			//System.out.println("testing "+name);
			//if( f.isDirectory() || f.isHidden() ) continue;
			String ext = FilenameUtils.getExtension(name).toLowerCase(); 
			if( ext.equals("xml") == false ) {
				continue;
			}

			// found an XML file in the /languages folder.  Good sign!
			++found;
			name = WORKING_DIRECTORY+"/"+FilenameUtils.getName(name);
			//System.out.println("found: "+name);

			InputStream stream = Translator.class.getClassLoader().getResourceAsStream(name);
			//if( stream != null ) 
			{
				TranslatorLanguage lang = new TranslatorLanguage();
				lang.loadFromInputStream(stream);
				languages.put(lang.getName(), lang);
			}
		}
		//System.out.println("total found: "+found);

		if(found==0) {
			throw new IllegalStateException();
		}
	}

	/**
	 * @param key
	 * @return the translated value for key
	 */
	static public String get(String key) {
		String value = null;
		try {
			value = languages.get(currentLanguage).get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * @return the list of language names
	 */
	static protected String[] getLanguageList() {
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
