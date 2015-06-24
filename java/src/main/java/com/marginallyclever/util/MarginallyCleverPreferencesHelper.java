package com.marginallyclever.util;

import com.marginallyclever.makelangelo.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger logger = LoggerFactory.getLogger(MarginallyCleverPreferencesHelper.class);

    /**
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) throws BackingStoreException {
        final Preferences machinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
        logger.info("node name: {}", machinesPreferenceNode.name());
        final String[] childrenPreferenceNodeNames = machinesPreferenceNode.childrenNames();
        final Set<String> lessThanZeroNames = getMachineNamesThatAreLessThanZero(childrenPreferenceNodeNames);
        for (String name: lessThanZeroNames) {
            machinesPreferenceNode.node(name).removeNode();
        }
    }

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

}
