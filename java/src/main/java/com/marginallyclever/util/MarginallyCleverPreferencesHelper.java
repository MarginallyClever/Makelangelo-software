package com.marginallyclever.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.PreferencesHelper;

/**
 * @author Peter Colapietro
 * @since v7.1.4
 */
final class MarginallyCleverPreferencesHelper {

  /**
   *
   */
  private static final String PURGE_FLAG = "-p";

  /**
   *
   */
  private static final String SAVE_FILE_FLAG = "-f";

  /**
   * @param args command line arguments.
   */
  @SuppressWarnings("deprecation")
  public static void main(String[] args) throws BackingStoreException {
    final Preferences machinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
    Log.message("node name: "+ machinesPreferenceNode.name());
    final boolean wereThereCommandLineArguments = args.length > 0;
    if (wereThereCommandLineArguments) {
      final boolean wasSaveFileFlagFound = wasSearchKeyFoundInArray(SAVE_FILE_FLAG, args);
      if (wasSaveFileFlagFound) {
        final File preferencesFile = MarginallyCleverPreferencesFileFactory.getXmlPreferencesFile();
        try (final OutputStream fileOutputStream = new FileOutputStream(preferencesFile)) {
          PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT).exportSubtree(fileOutputStream);
        } catch (IOException e) {
          Log.error(e.getMessage());
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
   * @param searchKey
   * @param stringArray
   * @return
   */
  private static boolean wasSearchKeyFoundInArray(String searchKey, String[] stringArray) {
    final int searchResult = Arrays.binarySearch(stringArray, searchKey);
    return searchResult >= 0;
  }

  /**
   * @param machinesPreferenceNode
   * @param childrenPreferenceNodeNames
   * @throws BackingStoreException
   */
  private static void purgeMachineNamesThatAreLessThanZero(Preferences machinesPreferenceNode, String[] childrenPreferenceNodeNames) throws BackingStoreException {
    final Set<String> lessThanZeroNames = getMachineNamesThatAreLessThanZero(childrenPreferenceNodeNames);
    for (String name : lessThanZeroNames) {
      machinesPreferenceNode.node(name).removeNode();
    }
  }

  /**
   * @param childrenPreferenceNodeNames
   * @return
   */
  private static Set<String> getMachineNamesThatAreLessThanZero(String[] childrenPreferenceNodeNames) {
    final Set<String> lessThanZeroNames = new HashSet<>();
    for (String childNodeName : childrenPreferenceNodeNames) {
      Log.message("child node name: "+ childNodeName);
      Long parsedMachineName = null;
      try {
        parsedMachineName = Long.parseLong(childNodeName);
      } catch (NumberFormatException e) {
        Log.error(e.getMessage());
      }
      boolean isMachineNameAnInteger = false;
      if (parsedMachineName != null) {
        isMachineNameAnInteger = true;
      }
      if (isMachineNameAnInteger) {
        //Machine configurations numbered -1 and below should not exist.
        final boolean isMachineNameLessThanZero = parsedMachineName < 0;
        if (isMachineNameLessThanZero) {
          lessThanZeroNames.add(parsedMachineName.toString());
        }
      }
    }
    return lessThanZeroNames;
  }

  private MarginallyCleverPreferencesHelper() throws IllegalStateException {
    throw new IllegalStateException();
  }

}
