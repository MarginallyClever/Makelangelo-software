package com.marginallyclever.makelangelo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TranslatorLanguage {

	private static final Logger logger = LoggerFactory.getLogger(TranslatorLanguage.class);
	
	private String src = "";// To be abble to have usefull information for debug (the url or path of the file where comms this traductions)
	private String name = "";
	private String author = "";
	private Map<String, String> strings = new HashMap<String, String>();


	/**
	 * @param languageFile
	 */
	public void loadFromString(String languageFile) {
	    this.src = languageFile;
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
	 * @param src to be able to idetify the origine (url or file) of this traduction to help debug.
	 */
	public void loadFromInputStream(InputStream inputStream, String src) {
		this.src = src;
		final DocumentBuilder db = getDocumentBuilder();
		if (db == null) {
			return;
		}
		try {
		    // TODO how do i specife the dtd path if in the jar ? as it s
		    //https://stackoverflow.com/questions/8699620/how-to-validate-xml-with-dtd-using-java
			db.setEntityResolver(new EntityResolver() {
			@Override
			public InputSource resolveEntity(String string, String string1) throws SAXException, IOException {
			   logger.debug(String.format("TODO : resolveEntity (\"%s\",\"%s\");",string,string1));
			     URL resource = getClass()./*getClassLoader().*/getResource("/languages/language_v0.dtd");
			     logger.debug(String.format("TODO : %s",resource.toString()));
			     Objects.requireNonNull(resource);
			    return new InputSource(resource.toString());
			    //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
			}
			});
		    
			Document dom = db.parse(inputStream);
			load(dom);
		} catch (SAXException | IOException e) {
			logger.error("Failed to parse language file", e);
		}
	}

	private void load(Document dom) {
		final Element docEle = dom.getDocumentElement();

		// PPAC37 : ??? so we have read the file (so it is normaly xml well formed. but it is not validated.) and as we do not use DTD or XSD the current kind of validation is non exception throw ( java.lang.NullPointerException )
		name = docEle.getElementsByTagName(XML_TAG_NAME).item(0).getFirstChild().getNodeValue();
		author = docEle.getElementsByTagName(XML_TAG_AUTHOR).item(0).getFirstChild().getNodeValue();

		NodeList nl = docEle.getElementsByTagName(XML_TAG_STRING);
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				//get the element
				Element el = (Element) nl.item(i);
				String key = getTextValue(el, XML_TAG_KEY);
				String value = getTextValue(el, XML_TAG_VALUE);

				// store key/value pairs into a map
				//logger.debug(language_file +"\t"+key+"\t=\t"+value);// KO language_file no in this scoop
				//logger.debug(src +"\t"+key+"\t=\t"+value);// OK
				
				if ( strings.containsKey(key)){
				    // This sould not occure.
				    // TO REVIEW : this is due to the fact that the xml is manualy edited.
				    // so ther is posibly multiple identical Key (that should be unique ).
				//Key Unicity can be done with xml validation ... but this mean using the key as an id attribut 
				//and adding a DTD to do .xml language DOCTYPE validation.
				
				// but in xml id value have some limitation (should be a NCName ... )
				
				    // ? ligne / pos in the file / stream ?
				    
				    if ( value.equals(strings.get(key))){
					logger.debug(String.format("in %s SAME Key SAME value key:\"%s\" value:\"%s\"",src,key,value));
				    }else{
					logger.error(String.format("in %s SAME Key DIFF value key:\"%s\" value:\"%s\" oldvalue:\"%s\"",src,key,value,strings.get(key)));
				    }
				}
				strings.put(key, value);
			}
		}
	}
	// PPAC37: To avoid confusion. (and maybe can be reused to generate .xml ...)
	public static final String XML_TAG_NAME = "name";
	public static final String XML_TAG_AUTHOR = "author";
	//
	public static final String XML_TAG_STRING = "string";
	//
	public static final String XML_TAG_KEY = "key";
	public static final String XML_TAG_VALUE = "value";
	public static final String XML_TAG_HINT = "hint";
    

	boolean xmlValidation = false; // so if a xml have no DTD this wil throw a lot of exceptions ...
	
	private DocumentBuilder getDocumentBuilder() {
		DocumentBuilder db = null;
		try {
		    final DocumentBuilderFactory domFactory = buildDocumentBuilder();
			if ( xmlValidation){
			    domFactory.setValidating(true);
			}
		    
			
			db = domFactory.newDocumentBuilder();
			   if (xmlValidation) {
				db.setErrorHandler(new ErrorHandler() {
				    @Override
				    public void error(SAXParseException exception) throws SAXException {
					// do something more useful in each of these handlers
					exception.printStackTrace();
				    }

				    @Override
				    public void fatalError(SAXParseException exception) throws SAXException {
					exception.printStackTrace();
				    }

				    @Override
				    public void warning(SAXParseException exception) throws SAXException {
					exception.printStackTrace();
				    }
				});
			}
			
		} catch (ParserConfigurationException e) {
			logger.error("Failed to create a new document", e);
		}
		return db;
	}

	private DocumentBuilderFactory buildDocumentBuilder() {
		return DocumentBuilderFactory.newInstance();
	}

	/**
	 * To have a way to remember missing key asked ... to later create a partial missing key .xml file.
	 */
	public SortedSet<String> missingKeys = new TreeSet<>();
	
	public String get(String key) {
		if(strings.containsKey(key)) {
			return strings.get(key);
		} else {
			// a sorted set of all the missing key to eventualy later generat a partial language .xml 
			missingKeys.add(key);		    
			return "Missing:"+key;
		}
	}

	/**
	 * PPAC37 : TODO to help adding missing key ...
	 * 
	 * https://examples.javacodegeeks.com/core-java/xml/parsers/documentbuilderfactory/create-xml-file-in-java-using-dom-parser-example/
	 *
	 * https://mkyong.com/java/how-to-create-xml-file-in-java-dom/
	 *
	 */
	public void generatePartialXmlFileWithMissingKey() {
	    try {
		//FileWriter fw = new FileWriter("missing_language.xml");
		Iterator<String> it = missingKeys.iterator();
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		Document doc = documentBuilder.newDocument();
		// root element
		Element root = doc.createElement("language");
		final long currentTimeMillis = System.currentTimeMillis();
		Date dateGenerated = new Date(currentTimeMillis);
		     root.setAttribute("timestamp", ""+currentTimeMillis);
		     SimpleDateFormat sdf = new SimpleDateFormat();
		     root.setAttribute("date", sdf.format(dateGenerated));
		     
		doc.appendChild(root);
		
		Element elemMeta = doc.createElement("meta");
		root.appendChild(elemMeta);
		
		Element elemLanguageName = doc.createElement(XML_TAG_NAME);
		elemMeta.appendChild(elemLanguageName);
		elemLanguageName.setTextContent(name+"_");// The "_" at the end is to avoid having the same language name. (or this will replace all traduction ... when loaded)
		
		Element elemAuthor = doc.createElement(XML_TAG_AUTHOR);
		elemMeta.appendChild(elemAuthor);
		elemAuthor.setTextContent(author);
		
		// add xml comment
		Comment comment = doc.createComment("for special characters like < &, need CDATA");
		elemMeta.appendChild(comment);
		while (it.hasNext()) {
		    String k = it.next();
		    //
		    Element elemString = doc.createElement(XML_TAG_STRING);
		    Element elemKey = doc.createElement(XML_TAG_KEY);
		    elemKey.appendChild(doc.createTextNode(k));
		    Element elemValue = doc.createElement(XML_TAG_VALUE);
    //		     // add xml CDATA
    //		    CDATASection cdataSection = doc.createCDATASection("HTML tag <code>testing</code>");
    //		    elemValue.appendChild(cdataSection);
		    elemValue.appendChild(doc.createTextNode("todo:" + k));
		    //
		    Element elemHint = doc.createElement(XML_TAG_HINT);
		    elemHint.appendChild(doc.createTextNode("todo"));
		    elemString.appendChild(elemKey);
		    elemString.appendChild(elemValue);
		    elemString.appendChild(elemHint);
		    root.appendChild(elemString);
		}
		// create the xml file
		//transform the DOM Object to an XML File
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		// pretty print XML
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource domSource = new DOMSource(doc);
		String xmlFilePath = "missing_language.xml";
		final File file = new File(xmlFilePath);
		StreamResult streamResult = new StreamResult(file);
		// If you use
		// StreamResult result = new StreamResult(System.out);
		// the output will be pushed to the standard output ...
		// You can use that for debugging 
		transformer.transform(domSource, streamResult);
		System.out.println("Done creating XML File: " + file.getAbsolutePath());
	    } catch (TransformerException ex) {
		java.util.logging.Logger.getLogger(TranslatorLanguage.class.getName()).log(Level.SEVERE, null, ex);
	    } catch (ParserConfigurationException ex) {
		java.util.logging.Logger.getLogger(TranslatorLanguage.class.getName()).log(Level.SEVERE, null, ex);
	    }
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
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
			if ( nl.getLength()>1){
			    // This could be avoid if we validate the .xml to verify it befor commiting it or when building the projet.
			    logger.debug(String.format("in %s Using FirstChild found but have other ... %s value %s",src,tagName,textVal));
			    for ( int i =1 ; i<nl.getLength() ; i++){
				logger.debug(String.format("Ignored ... value %s",nl.item(i).getFirstChild().getNodeValue()));
				
			    }
			}
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
