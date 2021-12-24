package com.marginallyclever.util;

import org.json.JSONObject;
import org.json.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Helper class to be used when accessing preferences.
 * @author Peter Colapietro
 * @since v7.1.4
 */
public final class PreferencesHelper {

	private static final Logger logger = LoggerFactory.getLogger(PreferencesHelper.class);
	
	/**
	 * Internal mapping of all Makelangelo preference nodes.
	 */
	private static Map<MakelangeloPreferenceKey, Preferences> CLASS_TO_PREFERENCE_NODE_MAP;

	/**
	 * Future Makelagelo root preference node path name.
	 */
	@SuppressWarnings("unused")
	private static final String MAKELANGELO_ROOT_PATH_NAME = PreferencesHelper.class.getPackage().getName();

	/**
	 * Legacy preference node path.
	 */
	@Deprecated
	private static final String LEGACY_MAKELANGELO_ROOT_PATH_NAME = "DrawBot";

	/**
	 * Graphics preference node path, used to store things such as {@code "Draw all while running"} used when OpenGL
	 * renders the application.
	 */
	private static final String GRAPHICS_PATH_NAME = "Graphics";

	/**
	 * Machine preference node path, used to store things such as paper height and width, invert left and right motors,
	 * etc.
	 * See com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings
	 */
	private static final String MACHINES_PATH_NAME = "Machines";
	private static final String LANGUAGE_PATH_NAME = "Language";
	private static final String SOUND_PATH_NAME = "Sound";
	private static final String METRICS_PATH_NAME = "Metrics";
	private static final String FILE_PATH_NAME = "File";
	private static final String PAPER_PATH_NAME = "Paper";

	/**
	 * Initializes {@link CLASS_TO_PREFERENCE_NODE_MAP}.
	 */
	@SuppressWarnings("unchecked")
	static public void start() {
		Map<MakelangeloPreferenceKey, ? super Preferences> initialMap = new HashMap<MakelangeloPreferenceKey, Preferences>();
		Preferences userRootPreferencesNode = MarginallyCleverPreferences.userRoot();
		//FIXME write unit test/tool to view import/export machine configurations.
		//final Preferences makelangeloPreferenceNode = userRootPreferencesNode.node(MAKELANGELO_ROOT_PATH_NAME);
		//new MarginallyCleverPreferences((AbstractPreferences) userRootPreferencesNode.node(thisPackageName), userRootPreferencesNode.node(thisPackageName).name()); 
		final Preferences legacyMakelangeloPreferenceNode = userRootPreferencesNode.node(LEGACY_MAKELANGELO_ROOT_PATH_NAME);//new MarginallyCleverPreferences((AbstractPreferences) userRootPreferencesNode.node(LEGACY_MAKELANGELO_ROOT_PATH_NAME), userRootPreferencesNode.node(LEGACY_MAKELANGELO_ROOT_PATH_NAME).name());
		try {
			legacyMakelangeloPreferenceNode.sync();
		} catch (BackingStoreException e) {
			logger.error("Failed to sync pref", e);
		}
		//initialMap.put(MAKELANGELO_ROOT, makelangeloPreferenceNode);
		initialMap.put(MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT, legacyMakelangeloPreferenceNode);
		initialMap.put(MakelangeloPreferenceKey.GRAPHICS, legacyMakelangeloPreferenceNode.node(GRAPHICS_PATH_NAME));
		initialMap.put(MakelangeloPreferenceKey.MACHINES, legacyMakelangeloPreferenceNode.node(MACHINES_PATH_NAME));
		initialMap.put(MakelangeloPreferenceKey.LANGUAGE, legacyMakelangeloPreferenceNode.node(LANGUAGE_PATH_NAME));
		initialMap.put(MakelangeloPreferenceKey.SOUND, legacyMakelangeloPreferenceNode.node(SOUND_PATH_NAME));
		initialMap.put(MakelangeloPreferenceKey.METRICS, legacyMakelangeloPreferenceNode.node(METRICS_PATH_NAME));
		initialMap.put(MakelangeloPreferenceKey.PAPER, legacyMakelangeloPreferenceNode.node(PAPER_PATH_NAME));
		initialMap.put(MakelangeloPreferenceKey.FILE, legacyMakelangeloPreferenceNode.node(FILE_PATH_NAME));
		
		Map<? extends MakelangeloPreferenceKey, ? extends Preferences> castedMap = (Map<? extends MakelangeloPreferenceKey, ? extends Preferences>) initialMap;
		CLASS_TO_PREFERENCE_NODE_MAP = Collections.unmodifiableMap(castedMap);
	}

	/**
	 * Enumeration used when getting a specific preference node.
	 * See #getPreferenceNode(MakelangeloPreferenceKey)
	 */
	public enum MakelangeloPreferenceKey {
		GRAPHICS,
		MACHINES,
		LANGUAGE,
		SOUND,
		FILE,
		METRICS,
		PAPER,
		@Deprecated
		LEGACY_MAKELANGELO_ROOT,
		//MAKELANGELO_ROOT
	}

	/**
	 * NOOP Constructor.
	 *
	 * @throws IllegalStateException
	 */
	private PreferencesHelper() throws IllegalStateException {
		throw new IllegalStateException();
	}

	/**
	 * @param key enumeration key used to look up a Makelangelo preference value.
	 * @return preference node associated with the given key.
	 */
	@SuppressWarnings("unchecked")
	public static <P extends Preferences> P getPreferenceNode(MakelangeloPreferenceKey key) {
		return (P) CLASS_TO_PREFERENCE_NODE_MAP.get(key);
	}

	/**
	 * @param preferenceNode Preference node whose name, and key values,
	 *                       as well as those of its children's are to be logged.
	 */
	public static <P extends Preferences> void logPreferenceNode(P preferenceNode) {
		try {
			logger.debug("node name:"+preferenceNode);
			logKeyValuesForPreferenceNode(preferenceNode);
			final String[] childrenPreferenceNodeNames = preferenceNode.childrenNames();
			for (String childNodeName : childrenPreferenceNodeNames) {
				final Preferences childNode = preferenceNode.node(childNodeName);
				logPreferenceNode(childNode);
			}
		} catch (BackingStoreException e) {
			logger.error("Failed to load preference", e);
		}
	}


	/**
	 * @param preferenceNode Preference node to log key value pairs for.
	 */
	public static <P extends Preferences> void logKeyValuesForPreferenceNode(P preferenceNode) throws BackingStoreException {
		final String[] keys = preferenceNode.keys();
		for (String key : keys) {
			logger.debug("key:{} value:{}", key, preferenceNode.get(key, null));
		}
	}


	/**
	 * @param sourcePreferenceNode      Preference node to be copied from.
	 * @param destinationPreferenceNode Preference node to be copied to.
	 */
	public static <P extends Preferences> void copyPreferenceNode(P sourcePreferenceNode, P destinationPreferenceNode) {
		try {
			final String[] keys = sourcePreferenceNode.keys();
			for (String key : keys) {
				final String value = sourcePreferenceNode.get(key, null);
				destinationPreferenceNode.put(key, value);
			}
			final String[] childNames = sourcePreferenceNode.childrenNames();
			for (String childName : childNames) {
				final Preferences destinationChildNode = destinationPreferenceNode.node(childName);
				copyPreferenceNode(sourcePreferenceNode.node(childName), destinationChildNode);
			}
		} catch (BackingStoreException e) {
			logger.error("Failed to copy preference", e);
		}
	}


	/**
	 * Recursively clears all the preferences (key-value associations) for a given node and its children.
	 *
	 * @param preferenceNode Preference node that you want recursively cleared of all key value pairs.
	 * See <a href="http://stackoverflow.com/a/6411855"></a>
	 */
	private static <P extends Preferences> void clearAll(P preferenceNode) throws BackingStoreException {
		final String[] childrenNames = preferenceNode.childrenNames();
		for (String childNodeName : childrenNames) {
			final Preferences childNode = preferenceNode.node(childNodeName);
			final String[] childNodesChildren = childNode.childrenNames();
			if (childNodesChildren != null) {
				final boolean hasChildren = childNodesChildren.length != 0;
				if (hasChildren) {
					clearAll(childNode);
				}
				childNode.clear();
			}
		}
		preferenceNode.clear();
	}

	/**
	 * Removes all of the preferences (key-value associations) in this
	 * preference node with no effect on any descendants
	 * of this node.
	 */
	@SuppressWarnings("unused")
	private static <P extends Preferences> void shallowClearPreferences(P preferenceNode) {
		try {
			preferenceNode.clear();
		} catch (BackingStoreException e) {
			logger.error("Failed to clear preference", e);
		}
	}

	/**
	 * Removes all of the preferences (key-value associations) in this
	 * preference node and any descendants of this node.
	 */
	@SuppressWarnings("unused")
	private static <P extends Preferences> void deepClearPreferences(P preferenceNode) {
		try {
			preferenceNode.clear();
			final String[] childrenPreferenceNodeNames = preferenceNode.childrenNames();
			for (String childNodeName : childrenPreferenceNodeNames) {
				final Preferences childNode = preferenceNode.node(childNodeName);
				childNode.clear();
			}
		} catch (BackingStoreException e) {
			logger.error("Failed to deep clear preference", e);
		}
	}

	/**
	 * @param sourcePreferenceNode      Preference node to be copied from.
	 * @param destinationPreferenceNode Preference node to be copied to and logged.
	 * <br>
	 *   {@link #copyPreferenceNode(Preferences, Preferences)}
	 * <br>
	 *   {@link #logPropertiesNode(Properties)}
	 * <br>
	 *   {@link #logAncestryable(Ancestryable)}
	 */
	@SuppressWarnings("unused")
	private void copyAndLogPreferenceNode(AbstractPreferences sourcePreferenceNode, MarginallyCleverPreferences destinationPreferenceNode) {
		try {
			PreferencesHelper.clearAll(destinationPreferenceNode);
		} catch (BackingStoreException e) {
			logger.error(e.getMessage());
		}
		PreferencesHelper.copyPreferenceNode(sourcePreferenceNode, destinationPreferenceNode);
		final File preferencesFile = MarginallyCleverPreferencesFileFactory.getPropertiesPreferencesFile();
		final Properties p = new Properties();
		try (final FileInputStream inStream = new FileInputStream(preferencesFile)) {
			p.load(inStream);
		} catch (IOException e) {
			logger.error("Failed to copy preference file {}", preferencesFile, e);
		}
		logPropertiesNode(p);
		logAncestryable(destinationPreferenceNode);
	}

	/**
	 * @param preferenceNode preference node to be logged.
	 */
	public static void logAncestryable(Ancestryable preferenceNode) {
		final JSONObject object = new JSONObject(preferenceNode.getChildren());
		logger.debug( object.toString());
	}

	/**
	 * @param properties Properties to be logged.
	 */
	public static <P extends Properties> void logPropertiesNode(P properties) {
		final JSONObject jsonObject = Property.toJSONObject(properties);
		logger.debug( jsonObject.toString());
	}

}
