package com.marginallyclever.makelangelo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TranslatorLanguage {
	private static final Logger logger = LoggerFactory.getLogger(TranslatorLanguage.class);

	private String name = "";
	private final List<String> authors = new ArrayList<>();
	private final Map<String, String> strings = new HashMap<>();


	/**
	 * @param languageFile
	 */
	public void loadFromString(String languageFile) {
		final DocumentBuilder db = getDocumentBuilder();
		if (db == null) {
			return;
		}
		Document dom = null;
		try {
			//Using factory get an instance of document builder
			//parse using builder to get DOM representation of the XML file
			dom = db.parse(languageFile);
		} catch (SAXException | IOException e) {
			logger.error("Failed to load file {}", languageFile, e);
		}
		if (dom == null) {
			return;
		}
		load(dom);
	}

	/**
	 * @param inputStream
	 */
	public void loadFromInputStream(InputStream inputStream) {
		final DocumentBuilder db = getDocumentBuilder();
		if (db == null) {
			return;
		}
		try {
			Document dom = db.parse(inputStream);
			load(dom);
		} catch (SAXException | IOException e) {
			logger.error("Failed to parse language file", e);
		}
	}

	private void load(Document dom) {
		final Element docEle = dom.getDocumentElement();

		name = docEle.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
		readAllAuthors(docEle);
		readAllStrings(docEle);
	}

	private void readAllAuthors(Element docEle) {
		NodeList authors = docEle.getElementsByTagName("authors");
		for (int i = 0; i < authors.getLength(); i++) {
			Node authorNode = authors.item(i);
			if (authorNode.getNodeType() == Node.ELEMENT_NODE) {
				Element authorElement = (Element) authorNode;
				NodeList authorList = authorElement.getElementsByTagName("author");
				for (int j = 0; j < authorList.getLength(); j++) {
					Node author = authorList.item(j);
					if (author.getNodeType() == Node.ELEMENT_NODE) {
						Element authorElement2 = (Element) author;
						this.authors.add(authorElement2.getFirstChild().getNodeValue());
					}
				}
			}
		}
	}

	/**
	 * read all key/value pairs from the xml file.
	 * @param docEle the root element of the xml file
	 */
	private void readAllStrings(Element docEle) {
		NodeList nl = docEle.getElementsByTagName("string");
		for (int i = 0; i < nl.getLength(); i++) {

			//get the element
			Element el = (Element) nl.item(i);
			String key = getTextValue(el, "key");
			String value = getTextValue(el, "value");

			// store key/value pairs into a map
			//logger.debug(language_file +"\t"+key+"\t=\t"+value);
			strings.put(key, value);
		}
	}

	private DocumentBuilder getDocumentBuilder() {
		DocumentBuilder db = null;
		try {
			db = buildDocumentBuilder().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Failed to create a new document", e);
		}
		return db;
	}

	private DocumentBuilderFactory buildDocumentBuilder() {
		return DocumentBuilderFactory.newInstance();
	}

	public String get(String key) {
		return strings.get(key);
	}


	/**
	 * <p>
	 * When a newline character "\n" was being read in from an xml file,
	 * it was being escaped ("\\n") and thus not behaving as an actual newline.
	 * This method replaces any "\\n" with "\n".
	 * </p>
	 * <p>
	 * <p>
	 * I take a xml element and the tag name, look for the tag and get
	 * the text content
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is 'name' I will return John
	 * </p>
	 *
	 * @param ele     XML element
	 * @param tagName name of 'tag' or child XML element of ele
	 * @return text value of tagName
	 */
	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			// to allow empty value as translation 
			final Node firstChild = el.getFirstChild();
			if ( firstChild != null){
				textVal = firstChild.getNodeValue();
			}else{				
				textVal = "";
			}
		}
		textVal = textVal.replace("\\n", "\n");
		return textVal;
	}

	public String getName() {
		return name;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public Set<String> getKeys() {
		// return a copy of strings
		return strings.keySet();
	}
}
