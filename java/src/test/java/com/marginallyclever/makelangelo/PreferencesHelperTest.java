package com.marginallyclever.makelangelo;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created on 5/25/15.
 *
 * @author Peter Colapietro
 * @since v7.1.3
 */
public class PreferencesHelperTest {

    private final Logger logger = LoggerFactory.getLogger(PreferencesHelperTest.class);

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.After
    public void tearDown() throws Exception {

    }

    @Test
    public void logPreferences() {
        final Preferences preferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MAKELANGELO_ROOT);
        logPreferenceNode(preferenceNode);
    }

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

    private void shallowClearPreferences() {
        final Preferences preferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MAKELANGELO_ROOT);
        try {
            preferenceNode.clear();
        } catch (BackingStoreException e) {
            logger.error("{}", e);
        }
    }

    private void deepClearPreferences() {
        final Preferences preferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MAKELANGELO_ROOT);
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