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

/**
 *
 * Created on 6/22/15. TODO write missing Javadoc tag descriptions.
 *
 * @author Peter Colapietro
 * @since v7.1.4
 *
 */
final class MarginallyCleverTranslationXmlFileHelper {

    /**
     *
     */
    private static final Logger logger = LoggerFactory.getLogger(MarginallyCleverTranslationXmlFileHelper.class);

    /**
     *
     */
    private static final String LANGUAGES_FOLDER_LOCATION = "languages";

    /**
     *
     */
    private static final String DEFAULT_LANGUAGE_XML_FILE = "english.xml";

    /**
     *
     * @param args command line arguments.
     *
     *             @see <a href="http://stackoverflow.com/a/14026865">Comparing key and values of two java maps</a>
     */
    public static void main(String[] args) {
        areLanguageFilesMissingKeys(false);
    }

    /**
     *
     */
    public static boolean areLanguageFilesMissingKeys(boolean logMissingKeys) {
        final URL languagesFolderUrl = getLanguagesFolderUrl();
        if(languagesFolderUrl != null) {
            try {
                final File[] languageFiles = getLanguageFiles(languagesFolderUrl);
                final File defaultLanguageFile = getDefaultLanguageFile(languagesFolderUrl, languageFiles);

                final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

                final Set<String> defaultLanguageFilesKeys = getKeySet(docBuilder.parse(defaultLanguageFile).getDocumentElement());
                for (final File languageFile : languageFiles) {
                    final String languageFileName = languageFile.getName();
                    final boolean isDefaultLanguageFile = languageFileName.equals(DEFAULT_LANGUAGE_XML_FILE);
                    if (!isDefaultLanguageFile) {
                        logger.info("{}", languageFile);
                        final Document parseXmlLanguageDocument = docBuilder.parse(languageFile);
                        final Set<String> thisLanguageFilesKeys = getKeySet(parseXmlLanguageDocument.getDocumentElement());

                        final boolean doesThisLanguageFileContainAllTheDefaultKeys = doesThisLanguageFileContainAllTheDefaultKeys(defaultLanguageFilesKeys, thisLanguageFilesKeys, languageFileName);
                        if(!doesThisLanguageFileContainAllTheDefaultKeys) {
                            if(logMissingKeys) {
                                logMissingKeys(defaultLanguageFilesKeys, thisLanguageFilesKeys);
                            }
                            return true;
                        }
                    }
                }
            } catch (SAXException | IOException | URISyntaxException | ParserConfigurationException e) {
                logger.error("{}", e);
            }
        }
        return false;
    }

    /**
     *
     * @param defaultLanguageFilesKeys
     * @param thisLanguageFilesKeys
     */
    private static void logMissingKeys(Set<String> defaultLanguageFilesKeys, Set<String> thisLanguageFilesKeys) {
        final Set<String> keysInA = new HashSet<>(defaultLanguageFilesKeys);
        final Set<String> keysInB = new HashSet<>(thisLanguageFilesKeys);

        // Keys in A and not in B
        final Set<String> inANotB = new HashSet<>(keysInA);
        inANotB.removeAll(keysInB);

        // Keys common to both maps
        final Set<String> commonKeys = new HashSet<>(keysInA);
        commonKeys.retainAll(keysInB);
        logger.error("Missing Keys: {}", inANotB);
    }

    /**
     *
     * @param defaultLanguageFilesKeys
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private static void writeSetObjectToFile(Set<String> defaultLanguageFilesKeys) throws IOException {
        try(final OutputStream fos = new FileOutputStream("keys.dat");
            final ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(defaultLanguageFilesKeys);
        }
    }

    /**
     *
     * @param languagesFolderUrl
     * @param languageFiles
     * @return
     */
    private static File getDefaultLanguageFile(URL languagesFolderUrl, File[] languageFiles) {
        final String defaultLanguagePathName = languagesFolderUrl.getPath() + '/' + DEFAULT_LANGUAGE_XML_FILE;
        final int indexOfDefaultLanguageFile = Arrays.binarySearch(languageFiles, new File(defaultLanguagePathName));
        if (indexOfDefaultLanguageFile < 0) {
            throw new AssertionError();
        }
        return languageFiles[indexOfDefaultLanguageFile];
    }

    /**
     *
     * @return
     */
    private static URL getLanguagesFolderUrl() {
        URL languagesFolderUrl = getLanguagesFolderUrlRelativeToClasspath();
        final URL languageFolderUsingUserDirectory = getLanguagesFolderUrlFromUserDirectory();
        if(languagesFolderUrl == null) {
            languagesFolderUrl = languageFolderUsingUserDirectory;
        }
        return languagesFolderUrl;
    }

    /**
     *
     * @return
     */
    private static URL getLanguagesFolderUrlFromUserDirectory() {
        final String workingDirectory = System.getProperty("user.dir") + File.separator + "languages";
        URL languageFolderUsingUserDirectoryUrl = null;
        try {
            languageFolderUsingUserDirectoryUrl = new URL("file", null , workingDirectory);
        } catch (MalformedURLException e) {
            logger.error("{}", e);
        }
        return languageFolderUsingUserDirectoryUrl ;
    }

    /**
     *
     * @return
     */
    private static URL getLanguagesFolderUrlRelativeToClasspath() {
        final ClassLoader thisClassesClassLoader = MarginallyCleverTranslationXmlFileHelper.class.getClassLoader();
        return thisClassesClassLoader.getResource(LANGUAGES_FOLDER_LOCATION);
    }

    /**
     *
     * @param node
     *
     * @see <a href="http://stackoverflow.com/a/5511298">Java: Most efficient method to iterate over all elements in a org.w3c.dom.Document?</a>
     */
    @SuppressWarnings("unused")
    private static void logNodeNameAndValue(Node node) {
        final String nodeName = node.getNodeName();
        if(nodeName.equals("key")) {
            logger.info("node name: {}, node value: {}", nodeName, node.getTextContent());
        }
        final NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                //calls this method for all the children which is Element
                logNodeNameAndValue(currentNode);
            }
        }
    }

    /**
     *
     * @param node
     *
     * @see <a href="http://stackoverflow.com/a/5511298">Java: Most efficient method to iterate over all elements in a org.w3c.dom.Document?</a>
     */
    private static Set<String> getKeySet(Node node) {
        final Set<String> keySet = new HashSet<>();
        if(node.getNodeName().equals("key")) {
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
     *
     * @param languagesFolderUrl
     * @return
     * @throws URISyntaxException
     */
    private static File[] getLanguageFiles(URL languagesFolderUrl) throws URISyntaxException {
        final File languagesFolder = new File(languagesFolderUrl.toURI());
        return languagesFolder.listFiles();
    }

    /**
     *
     * @param defaultLanguageFilesKeys
     * @param thisLanguageFilesKeys
     * @param thisLanguageFilesName
     * @return
     */
    private static boolean doesThisLanguageFileContainAllTheDefaultKeys(Set<String> defaultLanguageFilesKeys, Set<String> thisLanguageFilesKeys, String thisLanguageFilesName) {
        final boolean doesThisLanguageFileContainAllTheDefaultKeys = thisLanguageFilesKeys.containsAll(defaultLanguageFilesKeys);
        if (!doesThisLanguageFileContainAllTheDefaultKeys) {
            logger.error("{} does not contain all the default translation keys.", thisLanguageFilesName);
        } else {
            logger.error("{} contains all the default translation keys.", thisLanguageFilesName);
        }
        return doesThisLanguageFileContainAllTheDefaultKeys;
    }

}
