/*
 * Copyright (C) 2022 Marginally Clever Robots, Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.marginallyclever.util;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Iterator;
import java.util.SortedMap;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 *
 * @author q6
 */
public class FindAllTraductionXMLGenerator {
     private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FindAllTraductionXMLGenerator.class);

    public static final String XML_TAG_LANGUAGE = "language";//"language"
	
    // FROM PR 455 TODO Get them from TranslatorLanguage.java if PR 455 is merdged.
    public static final String XML_TAG_NAME = "name";
	public static final String XML_TAG_AUTHOR = "author";
	//
	public static final String XML_TAG_STRING = "string";
	//
	public static final String XML_TAG_KEY = "key";
	public static final String XML_TAG_VALUE = "value";
	public static final String XML_TAG_HINT = "hint";
	
	
	/**
	 * PPAC37 : TODO to help adding missing key ...
	 * 
	 * https://examples.javacodegeeks.com/core-java/xml/parsers/documentbuilderfactory/create-xml-file-in-java-using-dom-parser-example/
	 *
	 * https://mkyong.com/java/how-to-create-xml-file-in-java-dom/
	 *
	 */
	public static void generatePartialXmlFileWithMissingKey(SortedMap<String, ArrayList<FindAllTraductionResult>> groupIdenticalMissingKey) {
	    try {
		//SortedSet<String> missingKeys = new TreeSet<>();
		String name = "auto_generated_missing_key";//LanguageName
		String author = "none";//
		String textTraductionValueTODO = "";//TODO:";
		String textTraductionHintTODO = " ";//TODO";// avoid empty string or you get self closing element that do not help edition...
		
		Iterator<String> it = groupIdenticalMissingKey.keySet().iterator();
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		
		Document doc = documentBuilder.newDocument();
		// root element
		Element root = doc.createElement(XML_TAG_LANGUAGE);
		final long currentTimeMillis = System.currentTimeMillis();
		Date dateGenerated = new Date(currentTimeMillis);
//		     root.setAttribute("timestamp", ""+currentTimeMillis);
		     SimpleDateFormat sdf = new SimpleDateFormat();
//		     root.setAttribute("date", sdf.format(dateGenerated));

		doc.appendChild(root);

		Element elemMeta = doc.createElement("meta");
		root.appendChild(elemMeta);

		Element elemLanguageName = doc.createElement(XML_TAG_NAME);
		elemMeta.appendChild(elemLanguageName);
		elemLanguageName.setTextContent(name+"_"+sdf.format(dateGenerated));// The "_" at the end is to avoid having the same language name. (or this will replace all traduction ... when loaded)

		Element elemAuthor = doc.createElement(XML_TAG_AUTHOR);
		elemMeta.appendChild(elemAuthor);
		elemAuthor.setTextContent(author);

		// add xml comment
		Comment comment = doc.createComment("for special characters like '<', '&', need <![CDATA[...]]> or encoding like \"&lt;\" ");
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
		    elemValue.appendChild(doc.createTextNode(textTraductionValueTODO + k));
		    //
		    Element elemHint = doc.createElement(XML_TAG_HINT);
		    elemHint.appendChild(doc.createTextNode(textTraductionHintTODO));
		    
		  
	    
		    elemString.appendChild(elemKey);
		    elemString.appendChild(elemValue);
		    elemString.appendChild(elemHint);
		    for (FindAllTraductionResult tr : groupIdenticalMissingKey.get(k)) {
		//logger.error(" used in \"{}\" line {}", tr.pSrc, tr.lineInFile);
		elemString.appendChild(doc.createComment(String.format("used in \"%s\" line %d", tr.pSrc, tr.lineInFile)));
	    }
		    root.appendChild(elemString);
		}
		// create the xml file
		//transform the DOM Object to an XML File
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		// pretty print XML
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		//transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		
		// This set the texte content of the element valeur in a <![CDATA[...]]>
		//transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "value");
		
		//transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
		//transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "language_v0.dtd");
		
		DOMSource domSource = new DOMSource(doc);
		
//		String xmlFilePath = "missing_language.xml";
//		final File file = new File(xmlFilePath);
//		StreamResult streamResult = new StreamResult(file);
		// If you use
		 StreamResult streamResult = new StreamResult(System.out);
		// the output will be pushed to the standard output ...
		// You can use that for debugging 
		transformer.transform(domSource, streamResult);//new StAXResult() ???
//		System.out.println("Done creating XML File: " + file.getAbsolutePath());
	    } catch (TransformerException ex) {
		logger.error("{} {}",ex.getMessage(), ex);
	    } catch (ParserConfigurationException ex) {
		logger.error("{} {}",ex.getMessage(), ex);
	    }
	}
}
