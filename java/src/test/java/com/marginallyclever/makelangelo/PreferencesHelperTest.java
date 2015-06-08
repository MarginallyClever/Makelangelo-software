package com.marginallyclever.makelangelo;

import com.marginallyclever.util.MarginallyCleverJsonFilePreferencesFactory;
import com.marginallyclever.util.MarginallyCleverPreferences;
import org.json.JSONObject;
import org.json.XML;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created on 5/25/15.
 *
 * @author Peter Colapietro
 * @since v7.1.3
 */
public class PreferencesHelperTest {

    /**
     *
     */
    private final Preferences preferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MAKELANGELO_ROOT);


    /**
     *
     */
    private final Logger logger = LoggerFactory.getLogger(PreferencesHelperTest.class);

    /**
     *
     * @throws Exception
     */
    @org.junit.Before
    public void setUp() throws Exception {
    }

    /**
     *
     * @throws Exception
     */
    @org.junit.After
    public void tearDown() throws Exception {

    }

    /**
     *
     */
    @Test
    public void logPreferences() {
        logPreferenceNode(preferenceNode);
    }

    @Test
    public void testCopyPreferenceNode() {
        final MarginallyCleverPreferences marginallyCleverJsonPreferenceNode = new MarginallyCleverPreferences((AbstractPreferences) preferenceNode, "JSON");
        try {
            clearAll(marginallyCleverJsonPreferenceNode);
        } catch (BackingStoreException e) {
            logger.error("{}", e);
        }
        copyPreferenceNode(preferenceNode, marginallyCleverJsonPreferenceNode);
        final File preferencesFile = MarginallyCleverJsonFilePreferencesFactory.getXmlPreferenceFile();
        final String path = preferencesFile.toString();
        try {
            final byte[] encoded = Files.readAllBytes(Paths.get(path));
            final JSONObject jsonObject = XML.toJSONObject(new String(encoded, StandardCharsets.UTF_8));
            logger.info("JSONObject: {}", jsonObject);
        } catch (IOException e) {
            logger.error("{}", e);
        }
    }

    private void copyPreferenceNode(Preferences sourcePreferenceNode, AbstractPreferences destinationPreferenceNode) {
        try {
            final String[] keys = sourcePreferenceNode.keys();
            for (String key: keys) {
                final String value = sourcePreferenceNode.get(key, null);
                destinationPreferenceNode.put(key, value);
            }
            final String[] childNames = sourcePreferenceNode.childrenNames();
            for (String childName: childNames) {
                final Preferences destinationChildNode = destinationPreferenceNode.node(childName);
                copyPreferenceNode(sourcePreferenceNode.node(childName), (AbstractPreferences) destinationChildNode);
            }
        } catch (BackingStoreException e) {
            logger.error("{}", e);
        }
    }

    /**
     *
     * @param preferenceNode
     */
    private void logPreferenceNode(Preferences preferenceNode) {
        try {
            logger.info("node name:{}", preferenceNode);
            final String[] keys = preferenceNode.keys();
            logKeyValuesForPreferenceNode(preferenceNode, keys);
            final String[] childrenPreferenceNodeNames = preferenceNode.childrenNames();
            for (String childNodeName : childrenPreferenceNodeNames) {
                final Preferences childNode = preferenceNode.node(childNodeName);
                logPreferenceNode(childNode);
            }
        } catch (BackingStoreException e) {
            logger.error("{}",e);
        }
    }

    /**
     *
     * @param preferenceNode
     * @param keys
     */
    private void logKeyValuesForPreferenceNode(Preferences preferenceNode, String[] keys) {
        for (String key : keys) {
            logger.info("key:{} value:{}", key, preferenceNode.get(key, null));
        }
    }

    /**
     *
     * @param preferenceNode
     *
     * @see <a href="http://stackoverflow.com/a/6411855"></a>
     */
    private static void clearAll(Preferences preferenceNode) throws BackingStoreException {
        final String[] childrenNames = preferenceNode.childrenNames();
        for(String childNodeName : childrenNames) {
            final Preferences childNode = preferenceNode.node(childNodeName);
            final String[] childNodesChildren = childNode.childrenNames();
            if(childNodesChildren != null) {
                final boolean hasChildren = childNodesChildren.length != 0;
                if(hasChildren) {
                    clearAll(childNode);
                }
                childNode.clear();
            }
        }
        preferenceNode.clear();
    }

    /**
     *
     */
    private void shallowClearPreferences(Preferences preferenceNode) {
        try {
            preferenceNode.clear();
        } catch (BackingStoreException e) {
            logger.error("{}", e);
        }
    }

    /**
     *
     */
    private void deepClearPreferences(Preferences preferenceNode) {
        try {
            preferenceNode.clear();
            final String[] childrenPreferenceNodeNames = preferenceNode.childrenNames();
            for (String childNodeName : childrenPreferenceNodeNames) {
                final Preferences childNode = preferenceNode.node(childNodeName);
                childNode.clear();
            }
        } catch (BackingStoreException e) {
            logger.error("{}", e);
        }
    }
}