package com.marginallyclever.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.marginallyclever.makelangelo.Translator.WORKING_DIRECTORY;

/**
 * Helper utility class to aid in loading of language files.
 * @author Peter Colapietro
 * @since v7.1.4
 */
public final class MarginallyCleverTranslationXmlFileHelper {

  private static final Logger logger = LoggerFactory.getLogger(MarginallyCleverTranslationXmlFileHelper.class);

  /**
   * NOOP Constructor
   * @throws IllegalStateException
   */
  private MarginallyCleverTranslationXmlFileHelper() throws IllegalStateException {
    throw new IllegalStateException();
  }

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
   * See #writeSetObjectToFile
   */
  private static final String SET_OBJECT_FILE_NAME = "keys.txt";

  /**
   * @param args command line arguments.
   * See <a href="http://stackoverflow.com/a/14026865">Comparing key and values of two java maps</a>
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
            logger.debug(languageFile.getAbsolutePath());
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
        logger.error("A language key is missing", e);
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
    logger.error("Missing Keys: {}", inANotB.toString());
  }

  /**
   * Get any missing keys in the actual set from the expected set.
   * @param expected set of expected keys
   * @param actual set of actual keys
   * @return
   * See <a href="http://stackoverflow.com/a/14026865">Comparing key and values of two java maps</a>
   */
  private static Set<String> getMissingKeys(Set<String> expected, Set<String> actual) {
    final Set<String> keysInA = new HashSet<String>(expected);
    final Set<String> keysInB = new HashSet<String>(actual);

    // Keys in A and not in B
    final Set<String> inANotB = new HashSet<String>(keysInA);
    inANotB.removeAll(keysInB);

    // Keys common to both maps
    final Set<String> commonKeys = new HashSet<String>(keysInA);
    commonKeys.retainAll(keysInB);
    return inANotB;
  }

  /**
   * Write a set object to disk.
   * @param set Set you want written to disk.
   * @throws IOException
   * See #SET_OBJECT_FILE_NAME
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
   * See #DEFAULT_LANGUAGE_XML_FILE
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
   * See #getLanguagesFolderUrlRelativeToClasspath()
   * See #getLanguagesFolderUrlFromUserDirectory()
   */
  private static URL getLanguagesFolderUrl() {
    URL languagesFolderUrl = getLanguagesFolderUrlRelativeToClasspath();
    if( languagesFolderUrl!=null ) {
    	logger.debug("languages relative to classpath: {}", languagesFolderUrl.toString());
    }
    URL languageFolderUsingUserDirectory = getLanguagesFolderUrlFromUserDirectory();
    if( languageFolderUsingUserDirectory!=null ) {
    	logger.debug("languages via user directory: {}", languageFolderUsingUserDirectory.toString());
    }
    if (languagesFolderUrl == null) {
      languagesFolderUrl = languageFolderUsingUserDirectory;
    }
    return languagesFolderUrl;
  }

  /**
   * @return url object representing the language folder in the user's working directory.
   * See com.marginallyclever.makelangelo.Translator#WORKING_DIRECTORY
   */
  private static URL getLanguagesFolderUrlFromUserDirectory() {
    URL languageFolderUsingUserDirectoryUrl = null;
    try {
      File f = new File(WORKING_DIRECTORY);
      languageFolderUsingUserDirectoryUrl = f.toURI().toURL();
    } catch (MalformedURLException e) {
      logger.error("url malformed {}", WORKING_DIRECTORY, e );
    }
    return languageFolderUsingUserDirectoryUrl;
  }

  /**
   * @return url object representing languages folder relative to classpath.
   * See #LANGUAGES_FOLDER_LOCATION
   * See java.lang.ClassLoader#getResource(String)
   */
  public static URL getLanguagesFolderUrlRelativeToClasspath() {
    final ClassLoader thisClassesClassLoader = MarginallyCleverTranslationXmlFileHelper.class.getClassLoader();
    return thisClassesClassLoader.getResource(LANGUAGES_FOLDER_LOCATION);
  }

  /**
   * recursively logs the names and values of a given xml node
   * @param node xml node to recursively log
   * See <a href="http://stackoverflow.com/a/5511298">Java: Most efficient method to iterate over all elements in a org.w3c.dom.Document?</a>
   * See #logAllNodesNamesAndValues(org.w3c.dom.Node)
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
      logger.debug("node name: {}, node value: {}", nodeName, node.getTextContent());
    }
  }

  /**
   * gets a key set from a given xml node
   * @param node node to get all elements with the name {@code "key"}
   * See <a href="http://stackoverflow.com/a/5511298">Java: Most efficient method to iterate over all elements in a org.w3c.dom.Document?</a>
   */
  private static Set<String> getKeySet(Node node) {
    final Set<String> keySet = new HashSet<String>();
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
      logger.error("{} does not contain all the default translation keys.", thisLanguageFilesName);
      for (String s : defaultLanguageFilesKeys) {
        if (!thisLanguageFilesKeys.contains(s)) {
          logger.error("missing {}", s);
        }
      }
    } else {
      logger.debug("{} contains all the default translation keys.", thisLanguageFilesName);
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
