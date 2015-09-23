package com.marginallyclever.makelangelo;

import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marginallyclever.util.MarginallyCleverTranslationXmlFileHelper;

/**
 * MultilingualSupport is the translation engine.  You ask for a string it finds the matching string in the currently selected language.
 *
 * @author dan royer
 * @author Peter Colapietro
 * @see <a href="http://www.java-samples.com/showtutorial.php?tutorialid=152">XML and Java - Parsing XML using Java Tutorial</a>
 */
public final class MultilingualSupport {

  /**
   * Working directory. This represents the directory where the java executable launched the jar from.
   */
  public static final String WORKING_DIRECTORY = System.getProperty("user.dir") + File.separator + "languages";


  /**
   * The name of the preferences node containing the user's choice.
   */
  private static final String LANGUAGE_KEY = "language";

  /**
   *
   */
  private final Preferences languagePreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LANGUAGE);


  /**
   * The default choice when nothing has been selected.
   */
  public static final String DEFAULT_LANGUAGE = "English";

  /**
   * The current choice
   */
  private String currentLanguage;

  /**
   * a list of all languages and their translations strings
   */
  private final Map<String, LanguageContainer> languages = new HashMap<>();

  /**
   *
   */
  private final Logger logger = LoggerFactory.getLogger(MultilingualSupport.class);

  /**
   *
   */
  public MultilingualSupport() {
    try {
      loadLanguages();
    } catch (IllegalStateException e) {
      logger.error("{}. Defaulting to {}. Language folder expected to be located at {}", e.getMessage(), DEFAULT_LANGUAGE, WORKING_DIRECTORY);
      final LanguageContainer languageContainer  = new LanguageContainer();
      try (InputStream s = getClass().getClassLoader().getResourceAsStream(MarginallyCleverTranslationXmlFileHelper.getDefaultLanguageFilePath())) {
        languageContainer.loadFromInputStream(s);
      } catch (IOException ie) {
        logger.error("{}", ie.getMessage());
      }
      languages.put(languageContainer.getName(), languageContainer);
    }
    loadConfig();
  }

  /**
   * @return true if this is the first time loading language files (probably on install)
   */
  public boolean isThisTheFirstTimeLoadingLanguageFiles() {
    // Did the language file disappear?  Offer the language dialog.
    try {
      if (doesLanguagePreferenceExist()) {
        return false;
      }
    } catch (BackingStoreException e) {
      logger.error("{}", e);
    }
    return true;
  }

  /**
   * @return true if a preferences node exists
   * @throws BackingStoreException
   */
  private boolean doesLanguagePreferenceExist() throws BackingStoreException {
    if (Arrays.asList(languagePreferenceNode.keys()).contains(LANGUAGE_KEY)) {
      return true;
    }
    return false;
  }

  /**
   * save the user's current langauge choice
   */
  public void saveConfig() {
    languagePreferenceNode.put(LANGUAGE_KEY, currentLanguage);
  }

  /**
   * load the user's language choice
   */
  public void loadConfig() {
    currentLanguage = languagePreferenceNode.get(LANGUAGE_KEY, DEFAULT_LANGUAGE);
  }


  /**
   * Scan folder for language files.
   * @throws IllegalStateException No language files found
   */
  public void loadLanguages() throws IllegalStateException {
    final File f = new File(WORKING_DIRECTORY);
    final File[] allLanguageFiles = f.listFiles();
    checkLanguagesDirectoryExists(allLanguageFiles);
    createLanguageContainersFromLanguageFiles(allLanguageFiles);
  }

  /**
   * Checks to make sure the language folder exists.
   * @param allLanguageFiles - Array of all language files in the user's working directory.
   * @throws IllegalStateException No language files found
   */
  private void checkLanguagesDirectoryExists(File[] allLanguageFiles) throws IllegalStateException {
    final IllegalStateException illegalStateException = new IllegalStateException("No language files found!");
    if (allLanguageFiles == null) {
      throw illegalStateException;
    }
    if (allLanguageFiles.length <= 0) {
      throw illegalStateException;
    }
  }

  /**
   * scan a list of files, find all XML files, try to load them as languages.
   *
   * @param all_files the list of files
   */
  private void createLanguageContainersFromLanguageFiles(File[] all_files) {
    LanguageContainer lang;
    for (int i = 0; i < all_files.length; ++i) {
      if (all_files[i].isHidden()) {
        continue;
      }
      if (all_files[i].isDirectory()) {
        continue;
      }
      // get extension
      int j = all_files[i].getPath().lastIndexOf('.');
      if (j <= 0) {
        continue;  // no extension
      }
      if (all_files[i].getPath().substring(j + 1).toLowerCase().equals("xml") == false) {
        continue;  // only .XML or .xml files
      }
      lang = new LanguageContainer();
      lang.loadFromString(all_files[i].getAbsolutePath());
      languages.put(lang.getName(), lang);
    }
  }

  /**
   * Display a dialog box of available languages and let the user select their preference.
   */
  public void chooseLanguage() {
    final String[] choices = getLanguageList();
    final JComboBox<String> language_options = new JComboBox<String>(choices);

    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(language_options);

    final int result = JOptionPane.showConfirmDialog(null, panel, "Language", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.YES_NO_OPTION) {
      setCurrentLanguage(choices[language_options.getSelectedIndex()]);
      saveConfig();
    }
  }

  /**
   * @param key
   * @return the translated value for key
   */
  public String get(String key) {
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
  protected String[] getLanguageList() {
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
  public void setCurrentLanguage(String currentLanguage) {
    this.currentLanguage = currentLanguage;
  }
}
