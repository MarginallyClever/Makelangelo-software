package com.marginallyclever.makelangelo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created on 5/17/15.
 *
 * @author Peter Colapietro
 * @since v7.1.3
 */
public final class PreferencesHelper {

    /**
     *
     */
    private static final Map<Class, Preferences> CLASS_TO_PREFERENCE_NODE_MAP;

    /**
     * @see <a href="http://stackoverflow.com/a/507658">How can I Initialize a static Map?</a>
     */
    static {
        Map initialMap = new HashMap<>();
        final Preferences userRootPreferencesNode = Preferences.userRoot();
        final Preferences drawBotPreferenceNode = userRootPreferencesNode.node("DrawBot");
        initialMap.put(DrawPanel.class, drawBotPreferenceNode.node("Graphics"));
        initialMap.put(MachineConfiguration.class, drawBotPreferenceNode.node("Machines"));
        initialMap.put(MainGUI.class, drawBotPreferenceNode);
        initialMap.put(MultilingualSupport.class, userRootPreferencesNode.node("Language"));
        initialMap.put(RecentFiles.class, drawBotPreferenceNode);
        CLASS_TO_PREFERENCE_NODE_MAP = Collections.unmodifiableMap(initialMap);
    }

    /**
     *
     */
    private PreferencesHelper() {}

    /**
     *
     * @param marginallyCleverClass
     * @return
     */
    public static Preferences getPreferenceNode(Class<? extends Object> marginallyCleverClass) {
        return CLASS_TO_PREFERENCE_NODE_MAP.get(marginallyCleverClass);
    }
}
