package com.marginallyclever.makelangelo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created on 5/10/15.
 *
 * @author Peter Colapietro
 * @since v7.1.2
 */
public final class PropertiesFileHelper {

    /**
     *
     */
    private static final Logger logger = LoggerFactory.getLogger(PropertiesFileHelper.class);

    /**
     *
     */
    private static final String VERSION_PROPERTIES_FILENAME = "version.properties";

    /**
     *
     * @return
     */
    public static String getMakelangeloVersionPropertyValue() {
        final Properties prop = new Properties();
        String makelangeloVersionPropertyValue = "";
        try (final InputStream input = MainGUI.class.getClassLoader().getResourceAsStream(VERSION_PROPERTIES_FILENAME)) {
            if( input == null ){
                final String unableToFilePropertiesFileErrorMessage = "Sorry, unable to find " + VERSION_PROPERTIES_FILENAME;
                throw new IllegalStateException(unableToFilePropertiesFileErrorMessage);
            }
            //load a properties file from class path, inside static method
            prop.load(input);

            //get the property value and print it out
            makelangeloVersionPropertyValue = prop.getProperty("makelangelo.version");
            logger.info(makelangeloVersionPropertyValue);

        } catch (IllegalStateException | IOException ex) {
            logger.error("{}", ex);
        }
        return makelangeloVersionPropertyValue;
    }
}
