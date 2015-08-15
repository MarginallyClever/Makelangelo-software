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
    private static final Logger logger = LoggerFactory.getLogger(MarginallyCleverPreferencesHelper.class);

    /**
     *
     */
    public static final String PURGE_FLAG = "-p";

    /**
     *
     */
    public static final String SAVE_FILE_FLAG = "-f";

    /**
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) throws BackingStoreException {
        final Preferences machinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
        logger.info("node name: {}", machinesPreferenceNode.name());
        final boolean wereThereCommandLineArguments = args.length > 0;
        if(wereThereCommandLineArguments) {
            final boolean wasSaveFileFlagFound = wasSearchKeyFoundInArray(SAVE_FILE_FLAG, args);
            if (wasSaveFileFlagFound) {
                final File preferencesFile = MarginallyCleverJsonFilePreferencesFactory.getPreferencesFile();
                try(final OutputStream fileOutputStream = new FileOutputStream(preferencesFile)) {
                    PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MAKELANGELO_ROOT).exportSubtree(fileOutputStream);
                } catch (IOException e) {
                    logger.error("{}", e);
                }
            }
            final boolean wasPurgeFlagFound = wasSearchKeyFoundInArray(PURGE_FLAG, args);
            if (wasPurgeFlagFound) {
                final String[] childrenPreferenceNodeNames = machinesPreferenceNode.childrenNames();
                purgeMachineNamesThatAreLessThanZero(machinesPreferenceNode, childrenPreferenceNodeNames);
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
            logger.info("child node name: {}", childNodeName);
            Long parsedMachineName = null;
            try {
                parsedMachineName = Long.parseLong(childNodeName);
            } catch (NumberFormatException e) {
                logger.error("{}", e);
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

    private MarginallyCleverPreferencesHelper() {}

}
