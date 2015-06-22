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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Created on 6/22/15. TODO refactor into a test class.
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
        final ClassLoader thisClassesClassLoader = MarginallyCleverTranslationXmlFileHelper.class.getClassLoader();
        URL languagesFolderUrl = thisClassesClassLoader.getResource(LANGUAGES_FOLDER_LOCATION);
        final String workingDirectory = System.getProperty("user.dir") + File.separator + "languages";
        final File languageFolderUsingUserDirectory = new File(workingDirectory);
        if(languagesFolderUrl != null || languageFolderUsingUserDirectory != null) {
            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

            try {
                if(languagesFolderUrl == null) {
                    languagesFolderUrl = languageFolderUsingUserDirectory.toURI().toURL();
                }
                final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                final File languagesFolder = new File(languagesFolderUrl.toURI());
                final File[] languageFiles = languagesFolder.listFiles();
                final String defaultLanguagePathName = languagesFolderUrl.getPath() + '/' + DEFAULT_LANGUAGE_XML_FILE;
                final int indexOfDefaultLanguageFile = Arrays.binarySearch(languageFiles, new File(defaultLanguagePathName));
                if (indexOfDefaultLanguageFile < 0) {
                    throw new AssertionError();
                }
                final File defaultLanguageFile = languageFiles[indexOfDefaultLanguageFile];
                final Set<String> defaultLanguageFilesKeys = getKeySet(docBuilder.parse(defaultLanguageFile).getDocumentElement());
                for (final File languageFile : languageFiles) {
                    final String languageFileName = languageFile.getName();
                    final boolean isDefaultLanguageFile = languageFileName.equals(DEFAULT_LANGUAGE_XML_FILE);
                    if (!isDefaultLanguageFile) {
                        logger.info("{}", languageFile);
                        final Document parseXmlLanguageDocument = docBuilder.parse(languageFile);
                        final Set<String> thisLanguageFilesKeys = getKeySet(parseXmlLanguageDocument.getDocumentElement());
                        final boolean doesThisLanguageFileContainAllTheDefaultKeys = thisLanguageFilesKeys.containsAll(defaultLanguageFilesKeys);
                        if (!doesThisLanguageFileContainAllTheDefaultKeys) {
                            logger.error("{} does not contain all the default translation keys.", languageFileName);
                        } else {
                            logger.error("{} contains all the default translation keys.", languageFile);
                        }

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
                }
            } catch (SAXException | IOException | URISyntaxException | ParserConfigurationException e) {
                logger.error("{}", e);
            }
        }
    }

    /**
     *
     * @param node
     *
     * @see <a href="http://stackoverflow.com/a/5511298">Java: Most efficient method to iterate over all elements in a org.w3c.dom.Document?</a>
     */
    public static void doSomething(Node node) {
        final String nodeName = node.getNodeName();
        if(nodeName.equals("key")) {
            logger.info("node name: {}, node value: {}", nodeName, node.getTextContent());
        }
        final NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                //calls this method for all the children which is Element
                doSomething(currentNode);
            }
        }
    }

    /**
     *
     * @param node
     *
     * @see <a href="http://stackoverflow.com/a/5511298">Java: Most efficient method to iterate over all elements in a org.w3c.dom.Document?</a>
     */
    public static Set<String> getKeySet(Node node) {
        final Set<String> keySet = new HashSet<>();
        final String nodeName = node.getNodeName();
        if(nodeName.equals("key")) {
            final String nodeTextContentAsValue = node.getTextContent();
            logger.info("node name: {}, node value: {}", nodeName, nodeTextContentAsValue);
            keySet.add(nodeTextContentAsValue);
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
}
