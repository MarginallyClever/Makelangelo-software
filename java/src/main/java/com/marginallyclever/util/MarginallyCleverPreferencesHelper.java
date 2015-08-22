package com.marginallyclever.util;

import com.marginallyclever.makelangelo.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created on 6/23/15.
 *
 * @author Peter Colapietro
 * @since v7.1.4
 *
 */
final class MarginallyCleverPreferencesHelper {

    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MarginallyCleverPreferencesHelper.class);

    /**
     *
     */
    private static final String PURGE_FLAG = "-p";

    /**
     *
     */
    private static final String SAVE_FILE_FLAG = "-f";
    
    /**
     *
     */
    private static final String NUKE_PREFERENCES_FLAG = "-n";

    /**
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) throws BackingStoreException {
        final boolean wereThereCommandLineArguments = args.length > 0;
        if(wereThereCommandLineArguments) {
            final boolean wasSaveFileFlagFound = wasSearchKeyFoundInArray(SAVE_FILE_FLAG, args);
            if (wasSaveFileFlagFound) {
                /*
                final File preferencesFile = MarginallyCleverJsonFilePreferencesFactory.getPreferencesFile();
                try(final OutputStream fileOutputStream = new FileOutputStream(preferencesFile)) {
                    PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MAKELANGELO_ROOT).exportSubtree(fileOutputStream);
                } catch (IOException e) {
                    LOGGER.error("{}", e);
                }
                 */
                final String userRootFilePath = System.getProperty("user.home") + File.separator + "userRoot" + ".fileprefs";
                final File preferencesFile = new File(userRootFilePath).getAbsoluteFile();
                try(final OutputStream fileOutputStream = new FileOutputStream(preferencesFile)) {
                    Preferences.userRoot().exportSubtree(fileOutputStream);
                } catch (IOException e) {
                    LOGGER.error("{}", e);
                }
            }
            final boolean wasPurgeFlagFound = wasSearchKeyFoundInArray(PURGE_FLAG, args);
            if (wasPurgeFlagFound) {
                final Preferences machinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
                LOGGER.info("node name: {}", machinesPreferenceNode.name());
                final String[] childrenPreferenceNodeNames = machinesPreferenceNode.childrenNames();
                purgeMachineNamesThatAreLessThanZero(machinesPreferenceNode, childrenPreferenceNodeNames);
            }
            final boolean wasNuclearLaunchDetected = wasSearchKeyFoundInArray(NUKE_PREFERENCES_FLAG, args);
            if (wasNuclearLaunchDetected) {
                final Preferences userRoot = Preferences.userRoot();
                Preferences makelangeloNode = userRoot.node("DrawBot");
                MarginallyCleverPreferencesHelper.removeAll(makelangeloNode);
                makelangeloNode.flush();
                userRoot.flush();
                makelangeloNode = null;
                userRoot.remove("DrawBot");
                userRoot.remove("Language");
            }
        }
    }

    /**
     *
     * @param searchKey
     * @param stringArray
     * @return
     */
    private static boolean wasSearchKeyFoundInArray(String searchKey, String[] stringArray) {
        final int searchResult = Arrays.binarySearch(stringArray, searchKey);
        return searchResult >= 0;
    }

    /**
     *
     * @param machinesPreferenceNode
     * @param childrenPreferenceNodeNames
     * @throws BackingStoreException
     */
    private static void purgeMachineNamesThatAreLessThanZero(Preferences machinesPreferenceNode, String[] childrenPreferenceNodeNames) throws BackingStoreException {
        final Set<String> lessThanZeroNames = getMachineNamesThatAreLessThanZero(childrenPreferenceNodeNames);
        for (String name: lessThanZeroNames) {
            machinesPreferenceNode.node(name).removeNode();
        }
    }

    /**
     *
     * @param childrenPreferenceNodeNames
     * @return
     */
    private static Set<String> getMachineNamesThatAreLessThanZero(String[] childrenPreferenceNodeNames) {
        final Set<String> lessThanZeroNames = new HashSet<>();
        for (String childNodeName : childrenPreferenceNodeNames) {
            LOGGER.info("child node name: {}", childNodeName);
            Long parsedMachineName = null;
            try {
                parsedMachineName = Long.parseLong(childNodeName);
            } catch (NumberFormatException e) {
                LOGGER.error("{}", e);
            }
            boolean isMachineNameAnInteger = false;
            if(parsedMachineName != null) {
                isMachineNameAnInteger = true;
            }
            if(isMachineNameAnInteger) {
                //Machine configurations numbered -1 and below should not exist.
                final boolean isMachineNameLessThanZero = parsedMachineName < 0;
                if(isMachineNameLessThanZero) {
                    lessThanZeroNames.add(parsedMachineName.toString());
                }
            }
        }
        return lessThanZeroNames;
    }
    
    /**
     *
     * Recursively clears all the preferences (key-value associations) for a given node and its children.
     *
     * @param preferenceNode Preference node that you want recursively cleared of all key value pairs.
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
     * Recursively removes a preference nodes and its children.
     *
     * @param preferenceNode Preference node that you want recursively removed.
     *
     */
    private static void removeAll(Preferences preferenceNode) throws BackingStoreException {
        final String[] childrenNames = preferenceNode.childrenNames();
        for(String childNodeName : childrenNames) {
            final Preferences childNode = preferenceNode.node(childNodeName);
            final String[] childNodesChildren = childNode.childrenNames();
            if(childNodesChildren != null) {
                final boolean hasChildren = childNodesChildren.length != 0;
                if(hasChildren) {
                    MarginallyCleverPreferencesHelper.removeAll(childNode);
                }
                childNode.removeNode();
                childNode.flush();
            }
        }
        preferenceNode.removeNode();
        preferenceNode.flush();
    }

    private MarginallyCleverPreferencesHelper() {}

}
