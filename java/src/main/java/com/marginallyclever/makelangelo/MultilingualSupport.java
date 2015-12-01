package com.marginallyclever.makelangelo;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.File;
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
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;
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
  public static final String WORKING_DIRECTORY = /*File.separator + */"languages"/*+File.separator*/;


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
      String path = MarginallyCleverTranslationXmlFileHelper.getDefaultLanguageFilePath();
      System.out.println("default path requested: "+path);
      URL pathFound = getClass().getClassLoader().getResource(path);
      System.out.println("path found: "+pathFound);
      try (InputStream s = pathFound.openStream()) {
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
   * @see http://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file
   * @throws IllegalStateException No language files found
   */
  	public void loadLanguages() throws IllegalStateException {
  		// iterate and find all language files
  		URI uri = null;
		try {
			uri = getClass().getClassLoader().getResource(WORKING_DIRECTORY).toURI();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	    Path myPath;
  	    if (uri.getScheme().equals("jar")) {
  	        FileSystem fileSystem = null;
			try {
				fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
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
  	        
  	    	InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
  	    	//if( stream != null ) 
  	    	{
  	  	    	LanguageContainer lang = new LanguageContainer();
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
   * Display a dialog box of available languages and let the user select their preference.
   */
  public void chooseLanguage() {
    final String[] choices = getLanguageList();
    final JComboBox<String> language_options = new JComboBox<String>(choices);

    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(language_options);

	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	panel.setLocation((screenSize.width - panel.getWidth()) / 2, (screenSize.height - panel.getHeight()) / 2);

    final int result = JOptionPane.showConfirmDialog(null, panel, "Language", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
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
