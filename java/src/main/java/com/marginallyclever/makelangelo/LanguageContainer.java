package com.marginallyclever.makelangelo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LanguageContainer {
	protected String name = "";
	protected String author = "";
	protected Map<String,String> strings = new HashMap<String,String>();

	void load(String language_file) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom=null;

		try {
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			dom = db.parse(language_file);
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch(SAXException se) {
			se.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}

		if(dom==null) return;

		Element docEle = dom.getDocumentElement();

		name = docEle.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
		author = docEle.getElementsByTagName("author").item(0).getFirstChild().getNodeValue();

		NodeList nl = docEle.getElementsByTagName("string");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {

				//get the element
				Element el = (Element)nl.item(i);
				String key = getTextValue(el,"key");
				String value = getTextValue(el,"value");

				// store key/value pairs into a map
				//System.out.println(language_file +"\t"+key+"\t=\t"+value);
				strings.put(key, value);
			}
		}
	}

	public String get(String key) {
		String x = strings.get(key);
		if( x == null ) x = key;
		return x;
	}


	/**
	 *
	 * <p>
	 * When a newline character "\n" was being read in from an xml file,
	 * it was being escaped ("\\n") and thus not behaving as an actual newline.
	 * This method replaces any "\\n" with "\n".
	 * </p>
	 *
	 * <p>
	 * I take a xml element and the tag name, look for the tag and get
	 * the text content
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is 'name' I will return John
	 * </p>
	 *
	 * @param ele XML element
	 * @param tagName name of 'tag' or child XML element of ele
	 * @return text value of tagName
	 */
	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		textVal = textVal.replace("\\n", "\n");
		return textVal;
	}

	public String getName() {
		return name;
	}
	public String getAuthor() {
		return author;
	}
}