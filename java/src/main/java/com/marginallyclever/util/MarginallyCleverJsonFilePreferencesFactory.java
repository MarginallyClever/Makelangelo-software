package com.marginallyclever.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * Created on 6/7/15.
 *
 * @author Peter Colapietro
 * @since v7.1.3
 *
 * @see <a href="http://www.davidc.net/programming/java/java-preferences-using-file-backing-store">Java Preferences using a file as the backing store</a>
 *
 */
public final class MarginallyCleverJsonFilePreferencesFactory implements PreferencesFactory {

    /**
     *
     */
    private static final Logger logger = LoggerFactory.getLogger(MarginallyCleverJsonFilePreferencesFactory.class);

    /**
     *
     */
    private Preferences rootPreferences;

    /**
     *
     */
    private static final String SYSTEM_PROPERTY_FILE =
            "com.marginallyclever.util.MarginallyCleverJsonFilePreferencesFactory.file";

    /**
     *
     */
    private static File preferencesFile;


    @Override
    public Preferences systemRoot() {
        return userRoot();
    }

    @NotNull
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
     * @return Preference file
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

}
