package com.marginallyClever.util;

import com.marginallyClever.makelangelo.Makelangelo;
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

    private static final Logger logger = LoggerFactory.getLogger(PropertiesFileHelper.class);

    private static final String MAKELANGELO_PROPERTIES_FILENAME = "makelangelo.properties";
    private static final String GIT_PROPERTIES_FILENAME = "git.properties";

    /**
     * @return version number in the form of vX.Y.Z where X is MAJOR, Y is MINOR version, and Z is PATCH
     * See <a href="http://semver.org/">Semantic Versioning 2.0.0</a>
     */
    public static String getMakelangeloVersion() {
        final Properties prop = loadProperties(MAKELANGELO_PROPERTIES_FILENAME, false);
        String version = prop.getProperty("makelangelo.version");
        logger.debug("version: {}", version);
        return version;
    }

    /**
     * returns version git read from the file git.properties produced by the maven plugin git-commit-id-maven-plugin
     *
     * @return version git
     */
    public static String getMakelangeloGitVersion() {
        final Properties prop = loadProperties(GIT_PROPERTIES_FILENAME, true);
        String fullGitRevision = "dirty";
        if (prop.getProperty("git.branch") != null) {
            fullGitRevision = prop.getProperty("git.branch") + "-" + prop.getProperty("git.commit.id.abbrev");
            if ("true".equals(prop.getProperty("git.dirty"))) {
                fullGitRevision += "-dirty";
            }
        }

        logger.debug("fullGitRevision: {}", fullGitRevision);
        return fullGitRevision;
    }


    private static Properties loadProperties(String filename, boolean optionnal) throws IllegalStateException {
        final Properties prop = new Properties();
        try (final InputStream input = Makelangelo.class.getClassLoader().getResourceAsStream(filename)) {
            if (!optionnal && input == null) {
                throw new IllegalStateException("unable to find " + filename);
            }
            if (input != null) {
                //load a properties file from class path
                prop.load(input);
            }

        } catch (IllegalStateException | IOException ex) {
            logger.error("Failed to load {}", GIT_PROPERTIES_FILENAME, ex);
        }
        return prop;
    }
}
