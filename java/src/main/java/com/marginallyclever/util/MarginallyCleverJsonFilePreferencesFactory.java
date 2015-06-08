package com.marginallyclever.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * Created on 6/7/15.
 *
 * @author Peter Colapietro
 * @since 0.1.0
 */
public final class MarginallyCleverJsonFilePreferencesFactory implements PreferencesFactory {

    /**
     *
     */
    private static Logger logger = LoggerFactory.getLogger(MarginallyCleverJsonFilePreferencesFactory.class);

    /**
     *
     */
    private Preferences rootPreferences;

    /**
     *
     */
    public static final String SYSTEM_PROPERTY_FILE =
            "com.marginallyclever.util.MarginallyCleverJsonFilePreferencesFactory.file";

    /**
     *
     */
    private static File preferencesFile;

    /**
     *
     */
    private static File xmlPreferenceFile;


    @Override
    public Preferences systemRoot() {
        return userRoot();
    }

    @Override
    public Preferences userRoot() {
        if (rootPreferences == null) {
            logger.info("Instantiating root preferences");
            rootPreferences = new MarginallyCleverPreferences(null, "");
        }
        return rootPreferences;
    }

    /**
     *
     * @return
     */
    public static File getPreferencesFile()
    {
        if (preferencesFile == null) {
            String prefsFile = System.getProperty(SYSTEM_PROPERTY_FILE);
            if (prefsFile == null || prefsFile.length() == 0) {
                prefsFile = System.getProperty("user.home") + File.separator + ".fileprefs";
            }
            preferencesFile = new File(prefsFile).getAbsoluteFile();
            logger.info("Preferences file is {}", preferencesFile);
        }
        return preferencesFile;
    }

    /**
     *
     * @return
     */
    public static File getXmlPreferenceFile() {
        if (xmlPreferenceFile == null) {
            String prefsFile = System.getProperty(SYSTEM_PROPERTY_FILE);
            if (prefsFile == null || prefsFile.length() == 0) {
                prefsFile = System.getProperty("user.home") + File.separator + ".fileprefs.xml";
            }
            xmlPreferenceFile = new File(prefsFile).getAbsoluteFile();
            logger.info("Preferences file is {}", xmlPreferenceFile);
        }
        return xmlPreferenceFile;
    }

}
