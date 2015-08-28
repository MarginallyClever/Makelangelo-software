package com.marginallyclever.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * @author Peter Colapietro
 * @see <a href="http://www.davidc.net/programming/java/java-preferences-using-file-backing-store">Java Preferences using a file as the backing store</a>
 * @since v7.1.4
 */
public final class MarginallyCleverPreferencesFileFactory<A extends AbstractPreferences> implements PreferencesFactory {

  /**
   *
   */
  private static final Logger LOG = LoggerFactory.getLogger(MarginallyCleverPreferencesFileFactory.class);

  /**
   *
   */
  private A rootPreferences;

  /**
   *
   */
  private static final String SYSTEM_PROPERTY_KEY_FOR_XML_FILE =
      "com.marginallyclever.util.MarginallyCleverJsonFilePreferencesFactory.xmlFile";

  /**
   *
   */
  private static final String SYSTEM_PROPERTY_KEY_FOR_PROPERTIES_FILE =
      "com.marginallyclever.util.MarginallyCleverJsonFilePreferencesFactory.propertiesFile";

  /**
   *
   */
  private static File xmlPreferencesFile;

  /**
   *
   */
  private static File propertiesPreferencesFile;

  @Override
  public Preferences systemRoot() {
    return userRoot();
  }

  @NotNull
  @Override
  public Preferences userRoot() {
    if (rootPreferences == null) {
      LOG.info("Instantiating root preferences");
      @SuppressWarnings("unchecked")
      final A castedPreferences = (A) new MarginallyCleverPreferences(null, "");
      rootPreferences = castedPreferences;
    }
    return rootPreferences;
  }

  /**
   * @return Preference file
   */
  public synchronized static File getXmlPreferencesFile() {
    return getPreferenceFile(xmlPreferencesFile, SYSTEM_PROPERTY_KEY_FOR_XML_FILE, getDefaultXmlPreferenceFilePath());
  }

  /**
   * @return Preference file
   */
  public synchronized static File getPropertiesPreferencesFile() {
    return getPreferenceFile(xmlPreferencesFile, SYSTEM_PROPERTY_KEY_FOR_PROPERTIES_FILE, getDefaultPropertiesPreferenceFilePath());
  }

  private static File getPreferenceFile(File preferencesFile, String systemPropertyKey, String defaultFilePath) {
    if (preferencesFile == null) {
      String preferenceFilePath = System.getProperty(systemPropertyKey);
      if (preferenceFilePath == null || preferenceFilePath.length() == 0) {
        preferenceFilePath = defaultFilePath;
        System.setProperty(systemPropertyKey, preferenceFilePath);
      }
      preferencesFile = new File(preferenceFilePath).getAbsoluteFile();
      if (!preferencesFile.exists()) {
        try {
          if (preferencesFile.createNewFile()) {
            LOG.info("Preferences file was created.");
          }
        } catch (IOException e) {
          LOG.error("{}", e);
        }
      }
      LOG.info("Preferences file is {}", preferencesFile);
    }
    return preferencesFile;
  }

  /**
   * @return
   */
  private static String getDefaultXmlPreferenceFilePath() {
    return System.getProperty("user.home") + File.separator + "makelangelo" + ".xml";
  }

  /**
   * @return
   */
  private static String getDefaultPropertiesPreferenceFilePath() {
    return System.getProperty("user.home") + File.separator + "makelangelo" + ".properties";
  }

  /**
   * NOOP Constructor
   *
   * @throws IllegalStateException
   */
  private MarginallyCleverPreferencesFileFactory() throws IllegalStateException {
    throw new IllegalStateException();
  }
}
