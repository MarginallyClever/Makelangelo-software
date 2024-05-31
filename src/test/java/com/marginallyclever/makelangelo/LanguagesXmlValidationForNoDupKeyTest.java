package com.marginallyclever.makelangelo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.marginallyclever.makelangelo.TranslationsMissingTest.listFiles;
import static org.junit.jupiter.api.Assertions.*;

public class LanguagesXmlValidationForNoDupKeyTest {

	private static final Logger logger = LoggerFactory.getLogger(LanguagesXmlValidationForNoDupKeyTest.class);

	public final static String ressourceStringForXmlShemaFile = "/translator/language_no_dup_key.xsd";

	@Test
	public void validateLaguagesXmlFiles() throws IOException {

		List<String> xmlFilesWithValidationDefect = new ArrayList<>();
		File srcDir = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + "languages");

		// Pre requi	
//	//URL schemaFile = new File("src/test/resources/translator/language_no_dup_key.xsd").toURI().toURL();
		URL schemaFile = LanguagesXmlValidationForNoDupKeyTest.class.getResource(ressourceStringForXmlShemaFile);

		//  Pre requi schema xsd file
		assertNotNull(schemaFile, "The test need a redable schema xsd file (" + ressourceStringForXmlShemaFile + ") to validate the language files");

		//  Pre requi some language xml file to test
		List<File> files = listFiles(srcDir.toPath(), ".xml");

		// assert some files
		// ?TODO 
		// valide the files found
		files.forEach(file -> {
			boolean isValide = xmlValidationWithExternalXsdShema(file, schemaFile);
			if (!isValide) {
				xmlFilesWithValidationDefect.add(file.toString());
			}
		});

		StringBuilder sb = new StringBuilder();
		if (!xmlFilesWithValidationDefect.isEmpty()) {
			logger.info("invalide with " + ressourceStringForXmlShemaFile + " :");
			for (String result : xmlFilesWithValidationDefect) {
				sb.append("  ");
				sb.append(result);
				sb.append("\n");
				logger.info("  {}", result);
			}
		}
		assertEquals(0, xmlFilesWithValidationDefect.size(), "Some language xml file do not validate with " + ressourceStringForXmlShemaFile + "\n" + sb.toString() + ", see previous logs for details");
	}

	@Test
	public void verifyThatValidationWorks_notvalide() throws IOException {

		URL schemaFile = LanguagesXmlValidationForNoDupKeyTest.class.getResource(ressourceStringForXmlShemaFile);
		assertNotNull(schemaFile, "The test need a redable schema xsd file (" + ressourceStringForXmlShemaFile + ") to validate the language files");

		String myTestFileWithWithDupKeyInRessources = "/translator/english_with_dup_key.xml";
		URL resource = LanguagesXmlValidationForNoDupKeyTest.class.getResource(myTestFileWithWithDupKeyInRessources);
		assertNotNull(resource, "The test need a redable xml file (" + myTestFileWithWithDupKeyInRessources + ") to test itself");

		// english_with_dup_key.xml contains some duplicated an en empty key //language/string/key
		boolean isValide = xmlValidationWithExternalXsdShema(new File(resource.getFile()), schemaFile);

        assertFalse(isValide);
	}

	@Test
	public void verifyThatValidationWorks_valide() throws IOException {

		URL schemaFile = LanguagesXmlValidationForNoDupKeyTest.class.getResource(ressourceStringForXmlShemaFile);
		assertNotNull(schemaFile, "The test need a redable schema xsd file (" + ressourceStringForXmlShemaFile + ") to validate the language files");

		String myTestFileWithNoDupKeyInRessources = "/translator/english_with_no_dup_key.xml";
		URL resource = LanguagesXmlValidationForNoDupKeyTest.class.getResource(myTestFileWithNoDupKeyInRessources);
		assertNotNull(resource, "The test need a redable xml file (" + myTestFileWithNoDupKeyInRessources + ") to test itself");

		// english_with_no_dup_key.xml do not containe duplicated or empty //language/string/key        
		boolean isValide = xmlValidationWithExternalXsdShema(new File(resource.getFile()), schemaFile);

		assertTrue(isValide);
	}

	/**
	 * Validate an xml file with an external xsd Schema. src inspiration :
	 * https://stackoverflow.com/questions/15732/whats-the-best-way-to-validate-an-xml-file-against-an-xsd-file
	 *
	 * @param xmlFileToValidate the xml file to validate.
	 * @param schemaFile the xsd schema file to use to validate the xml file.
	 * @return true if the validation is a succes ( no warm, error or fatal
	 * error ) false otherwise and if other exception ( cant found file xml, or
	 * xsd, or xsd not valid) occure.
	 */
	public static boolean xmlValidationWithExternalXsdShema(File xmlFileToValidate, URL schemaFile) {
		Source xmlStreamSource = new StreamSource(xmlFileToValidate);
		SchemaFactory schemaFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = schemaFactory.newSchema(schemaFile);
			Validator validator = schema.newValidator();
			// a list to store the validation errors / warms used to find if finnaly a validated (if this list empty) xml file or not latter.
			List<String> errorHandlerException = new ArrayList<>();
			// to get full error list, we set a ErrorEandler
			validator.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException e) throws SAXException {
					logger.info(String.format("[ %7s ] %s:%-4d %s", "warning", e.getSystemId(), e.getLineNumber(), e.getMessage()));
					errorHandlerException.add(e.toString());
				}

				@Override
				public void error(SAXParseException e) throws SAXException {
					logger.info(String.format("[ %7s ] %s:%-4d %s", "error", e.getSystemId(), e.getLineNumber(), e.getMessage()));
					errorHandlerException.add(e.toString());
				}

				@Override
				public void fatalError(SAXParseException e) throws SAXException {
					logger.info(String.format("[ %7s ] %s:%-4d %s", "fatalError", e.getSystemId(), e.getLineNumber(), e.getMessage()));
					errorHandlerException.add(e.toString());
				}
			});
			// the validation.
			validator.validate(xmlStreamSource);
			return errorHandlerException.isEmpty();
		} catch (SAXException e) {
			// avec le setErrorHandler on ne devrait plus passer ici.
			logger.info(String.format("[ %7s ] %s\n%s", "invalid", xmlStreamSource.getSystemId(), e));
		} catch (IOException ex) {
			logger.info(String.format("[ %7s ] %s\n%s", "error", xmlStreamSource.getSystemId(), ex));
		} catch (Exception ex) {
			// le shema n'existe pas ?
			logger.info(String.format("[ %7s ] %s\n%s", "error", xmlStreamSource.getSystemId(), ex));
		}
		return false;
	}

}
