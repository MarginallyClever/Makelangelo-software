package com.marginallyclever.makelangelo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created on 5/17/15. FIXME Write Javadocs.
 *
 * @author Peter Colapietro
 * @since v7.1.4
 */
public final class PreferencesHelper {

    /**
     *
     */
    private static final Map<MakelangeloPreferenceKey, Preferences> CLASS_TO_PREFERENCE_NODE_MAP;

    /**
     * @see <a href="http://stackoverflow.com/a/507658">How can I Initialize a static Map?</a>
     */
    static {
        final Map<MakelangeloPreferenceKey, Preferences> initialMap = new HashMap<>();
        final Preferences userRootPreferencesNode = Preferences.userRoot();
        //final String thisPackageName = PreferencesHelper.class.getPackage().getName();
        final Preferences makelangeloPreferenceNode = userRootPreferencesNode.node("DrawBot");// thisPackageName); FIXME write unit test/tool to view import/export machine configurations.
        initialMap.put(MakelangeloPreferenceKey.MAKELANGELO_ROOT, makelangeloPreferenceNode);
        initialMap.put(MakelangeloPreferenceKey.GRAPHICS, makelangeloPreferenceNode.node("Graphics"));
        initialMap.put(MakelangeloPreferenceKey.MACHINES, makelangeloPreferenceNode.node("Machines"));
        initialMap.put(MakelangeloPreferenceKey.LANGUAGE, makelangeloPreferenceNode.node("Language"));
        CLASS_TO_PREFERENCE_NODE_MAP = Collections.unmodifiableMap(initialMap);
    }

    /**
     *
     */
    private PreferencesHelper() {}

    /**
     *
     * @param key enumeration key used to look up a Makelangelo preference value.
     * @return
     */
    public static Preferences getPreferenceNode(MakelangeloPreferenceKey key) {
        return CLASS_TO_PREFERENCE_NODE_MAP.get(key);
    }

    public enum MakelangeloPreferenceKey {
        GRAPHICS,
        MACHINES,
        LANGUAGE,
        MAKELANGELO_ROOT
    }
}
