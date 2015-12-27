package com.marginallyclever.util;

import static com.marginallyclever.makelangelo.Translator.WORKING_DIRECTORY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Helper utility class to aid in loading of language files.
 * @author Peter Colapietro
 * @since v7.1.4
 */
public final class MarginallyCleverTranslationXmlFileHelper {

  /**
   * NOOP Constructor
   * @throws IllegalStateException
   */
  private MarginallyCleverTranslationXmlFileHelper() throws IllegalStateException {
    throw new IllegalStateException();
  }

  /**
   * SLF4J log.
   */
  private static final Logger log = LoggerFactory.getLogger(MarginallyCleverTranslationXmlFileHelper.class);

  /**
   * Languages folder location relative to the user's working directory.
   */
  private static final String LANGUAGES_FOLDER_LOCATION = /*File.separator +*/ "languages";

  /**
   * The default language file.
   */
  private static final String DEFAULT_LANGUAGE_XML_FILE = "english.xml";

  @SuppressWarnings("unused")
  private static final boolean LOG_MISSING_KEYS = true;

  private static final boolean DO_NOT_LOG_MISSING_KEYS = false;

  private static final boolean CHECK_ALL_LANGUAGE_FILES = true;

  @SuppressWarnings("unused")
  private static final boolean DO_NOT_CHECK_ALL_LANGUAGE_FILES = false;

  /**
   * Used when writing a set to disk.
   * @see #writeSetObjectToFile
   */
  private static final String SET_OBJECT_FILE_NAME = "keys.txt";

  /**
   * @param args command line arguments.
   * @see <a href="http://stackoverflow.com/a/14026865">Comparing key and values of two java maps</a>
   */
  public static void main(String[] args) {
    areLanguageFilesMissingKeys(DO_NOT_LOG_MISSING_KEYS, CHECK_ALL_LANGUAGE_FILES);
  }

  /**
   * Check to see if language translation keys are missing. It can check and log all language files, or fail fast
   * on the first missing key.
   * @param logMissingKeys log missing keys
   * @param checkAllLanguageFiles check all files, if set to false the methods stops on the first missing key
   * @return if any language translation keys are missing
   */
  public static boolean areLanguageFilesMissingKeys(boolean logMissingKeys, boolean checkAllLanguageFiles) {
    final URL languagesFolderUrl = getLanguagesFolderUrl();
    if (languagesFolderUrl != null) {
      try {
        final File[] languageFiles = getLanguageFiles(languagesFolderUrl);
        final File defaultLanguageFile = searchForDefaultLanguageInLanguagesFolder(languagesFolderUrl, languageFiles);

        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

        final Set<String> defaultLanguageFilesKeys = getKeySet(docBuilder.parse(defaultLanguageFile).getDocumentElement());
        Boolean doAllLanguageFilesContainAllTheDefaultKeys = null;
        for (final File languageFile : languageFiles) {
          final String languageFileName = languageFile.getName();
          final boolean isDefaultLanguageFile = languageFileName.equals(DEFAULT_LANGUAGE_XML_FILE);
          if (!isDefaultLanguageFile) {
            log.info("{}", languageFile);
            final Document parseXmlLanguageDocument = docBuilder.parse(languageFile);
            final Set<String> thisLanguageFilesKeys = getKeySet(parseXmlLanguageDocument.getDocumentElement());

            final boolean doesThisLanguageFileContainAllTheDefaultKeys = doesThisLanguageFileContainAllTheDefaultKeys(defaultLanguageFilesKeys, thisLanguageFilesKeys, languageFileName);
            if (!doesThisLanguageFileContainAllTheDefaultKeys) {
              if (logMissingKeys) {
                logMissingKeys(defaultLanguageFilesKeys, thisLanguageFilesKeys);
              }
              doAllLanguageFilesContainAllTheDefaultKeys = false;
            }
            if (!checkAllLanguageFiles) {
              break;
            }
          }
        }
        if (doAllLanguageFilesContainAllTheDefaultKeys != null) {
          return true;
        }
      } catch (SAXException | IOException | URISyntaxException | ParserConfigurationException e) {
        log.error("{}", e);
      }
    }
    return false;
  }

  /**
   * Logs missing keys.
   * @param expected set of expected keys
   * @param actual set of actual keys
   */
  private static void logMissingKeys(Set<String> expected, Set<String> actual) {
    final Set<String> inANotB = getMissingKeys(expected, actual);
    log.error("Missing Keys: {}", inANotB);
  }

  /**
   * Get any missing keys in the actual set from the expected set.
   * @param expected set of expected keys
   * @param actual set of actual keys
   * @return
   * @see <a href="http://stackoverflow.com/a/14026865">Comparing key and values of two java maps</a>
   */
  private static Set<String> getMissingKeys(Set<String> expected, Set<String> actual) {
    final Set<String> keysInA = new HashSet<>(expected);
    final Set<String> keysInB = new HashSet<>(actual);

    // Keys in A and not in B
    final Set<String> inANotB = new HashSet<>(keysInA);
    inANotB.removeAll(keysInB);

    // Keys common to both maps
    final Set<String> commonKeys = new HashSet<>(keysInA);
    commonKeys.retainAll(keysInB);
    return inANotB;
  }

  /**
   * Write a set object to disk.
   * @param set Set you want written to disk.
   * @throws IOException
   * @see #SET_OBJECT_FILE_NAME
   */
  @SuppressWarnings("unused")
  private static void writeSetObjectToFile(Set<String> set) throws IOException {
    try (final OutputStream fos = new FileOutputStream(SET_OBJECT_FILE_NAME);
         final ObjectOutputStream oos = new ObjectOutputStream(fos)) {
      oos.writeObject(set);
    }
  }

  /**
   * Search the languages folder for the default language file.
   * @param languagesFolderUrl language folder.
   * @param languageFiles list of all language files to check for existence of the default
   * @return file object representing the default language file.
   * @see #DEFAULT_LANGUAGE_XML_FILE
   * @throws AssertionError
   */
  private static File searchForDefaultLanguageInLanguagesFolder(URL languagesFolderUrl, File[] languageFiles) throws AssertionError {
    final String defaultLanguagePathName = buildDefaultLanguageFilePathName(languagesFolderUrl);
    final int indexOfDefaultLanguageFile = Arrays.binarySearch(languageFiles, new File(defaultLanguagePathName));
    if (indexOfDefaultLanguageFile < 0) {
      throw new AssertionError();
    }
    return languageFiles[indexOfDefaultLanguageFile];
  }

  /**
   * @return url object representing the languages folder
   * @see #getLanguagesFolderUrlRelativeToClasspath()
   * @see #getLanguagesFolderUrlFromUserDirectory()
   */
  private static URL getLanguagesFolderUrl() {
    URL languagesFolderUrl = getLanguagesFolderUrlRelativeToClasspath();
    if( languagesFolderUrl!=null ) {
    	log.debug("languages relative to classpath: "+languagesFolderUrl.toString());
    }
    URL languageFolderUsingUserDirectory = getLanguagesFolderUrlFromUserDirectory();
    if( languageFolderUsingUserDirectory!=null ) {
    	log.debug("languages via user directory: "+languageFolderUsingUserDirectory.toString());
    }
    if (languagesFolderUrl == null) {
      languagesFolderUrl = languageFolderUsingUserDirectory;
    }
    return languagesFolderUrl;
  }

  /**
   * @return url object representing the language folder in the user's working directory.
   * @see com.marginallyclever.makelangelo.Translator#WORKING_DIRECTORY
   */
  private static URL getLanguagesFolderUrlFromUserDirectory() {
    URL languageFolderUsingUserDirectoryUrl = null;
    try {
    	File f = new File(WORKING_DIRECTORY);
      languageFolderUsingUserDirectoryUrl = f.toURI().toURL();
    } catch (MalformedURLException e) {
      log.error("{}", e);
    }
    return languageFolderUsingUserDirectoryUrl;
  }

  /**
   * @return url object representing languages folder relative to classpath.
   * @see #LANGUAGES_FOLDER_LOCATION
   * @see java.lang.ClassLoader#getResource(String)
   */
  public static URL getLanguagesFolderUrlRelativeToClasspath() {
    final ClassLoader thisClassesClassLoader = MarginallyCleverTranslationXmlFileHelper.class.getClassLoader();
    return thisClassesClassLoader.getResource(LANGUAGES_FOLDER_LOCATION);
  }

  /**
   * recursively logs the names and values of a given xml node
   * @param node xml node to recursively log
   * @see <a href="http://stackoverflow.com/a/5511298">Java: Most efficient method to iterate over all elements in a org.w3c.dom.Document?</a>
   * @see #logAllNodesNamesAndValues(org.w3c.dom.Node)
   */
  @SuppressWarnings("unused")
  private static void logAllNodesNamesAndValues(Node node) {
    logNodeNameAndValue(node);
    final NodeList nodeList = node.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      final Node currentNode = nodeList.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
        //calls this method for all the children which is Element
        logAllNodesNamesAndValues(currentNode);
      }
    }
  }

  /**
   * logs the name and value of a given xml node
   * @param node xml node to log
   */
  private static void logNodeNameAndValue(Node node) {
    final String nodeName = node.getNodeName();
    if (nodeName.equals("key")) {
      log.info("node name: {}, node value: {}", nodeName, node.getTextContent());
    }
  }

  /**
   * gets a key set from a given xml node
   * @param node node to get all elements with the name {@code "key"}
   * @see <a href="http://stackoverflow.com/a/5511298">Java: Most efficient method to iterate over all elements in a org.w3c.dom.Document?</a>
   */
  private static Set<String> getKeySet(Node node) {
    final Set<String> keySet = new HashSet<>();
    if (node.getNodeName().equals("key")) {
      keySet.add(node.getTextContent());
    }
    final NodeList nodeList = node.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      final Node currentNode = nodeList.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
        // calls this method for all the children which is Element
        keySet.addAll(getKeySet(currentNode));
      }
    }
    return keySet;
  }

  /**
   * @param languagesFolderUrl - url object representing the language files folder
   * @return an array of file objects in the language folder.
   * @throws URISyntaxException if the url object parameter is not correct
   */
  private static File[] getLanguageFiles(URL languagesFolderUrl) throws URISyntaxException {
    final File languagesFolder = new File(languagesFolderUrl.toURI());
    return languagesFolder.listFiles();
  }

  /**
   * @param defaultLanguageFilesKeys default language file's keys
   * @param thisLanguageFilesKeys this language files keys
   * @param thisLanguageFilesName this language files name
   * @return does this language file contain all the default keys
   */
  private static boolean doesThisLanguageFileContainAllTheDefaultKeys(Set<String> defaultLanguageFilesKeys, Set<String> thisLanguageFilesKeys, String thisLanguageFilesName) {
    final boolean doesThisLanguageFileContainAllTheDefaultKeys = thisLanguageFilesKeys.containsAll(defaultLanguageFilesKeys);
    if (!doesThisLanguageFileContainAllTheDefaultKeys) {
      log.error("{} does not contain all the default translation keys.", thisLanguageFilesName);
      Iterator<String> k = defaultLanguageFilesKeys.iterator();
      while(k.hasNext()) {
    	  String s = k.next();
    	  if(!thisLanguageFilesKeys.contains(s)) {
    		  log.error("missing " + s);
    	  }
      }
    } else {
      log.info("{} contains all the default translation keys.", thisLanguageFilesName);
    }
    return doesThisLanguageFileContainAllTheDefaultKeys;
  }

  @SuppressWarnings("unused")
  private static File getDefaultLanguageFileFromClasspath() {
    return getDefaultLanguageFile(getLanguagesFolderUrlRelativeToClasspath());
  }

  public static String getDefaultLanguageFilePathNameFromClassPath() {
    return buildDefaultLanguageFilePathName(getLanguagesFolderUrlRelativeToClasspath());
  }

  private static File getDefaultLanguageFile(URL languagesFolderUrl) {
    return new File(buildDefaultLanguageFilePathName(languagesFolderUrl));
  }

  public static String buildDefaultLanguageFilePathName(URL languagesFolderUrl) {
    return languagesFolderUrl.getPath() + '/' + DEFAULT_LANGUAGE_XML_FILE;
  }

  public static String getDefaultLanguageFilePath() {
    return LANGUAGES_FOLDER_LOCATION + File.separator + DEFAULT_LANGUAGE_XML_FILE;
  }
}
