package com.marginallyclever.makelangelo.plotter.plottersettings;

import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Manages the list of available machine configurations, both default and custom.
 * @author Dan Royer
 * @since 7.33.2
 */
public class PlotterSettingsManager {
    private static final Logger logger = LoggerFactory.getLogger(PlotterSettingsManager.class);
    public static final String KEY_MACHINE_STYLE = "machineStyle";
    public static final String KEY_MACHINE_LAST_SELECTED = "lastLoadedMachine";
    private final List<String> profileNames = new ArrayList<>();
    private final Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);

    public PlotterSettingsManager() {
        super();
        writeMakelangelo5Profile();
        writeMakelangeloHugeProfile();

        loadAllProfiles();
    }

    private void writeMakelangelo5Profile() {
        PlotterSettings profile = new PlotterSettings();
        profile.setRobotUID("Makelangelo 5");
        profile.setMachineSize(650,1000);
        profile.saveConfig();
    }

    private void writeMakelangeloHugeProfile() {
        PlotterSettings profile = new PlotterSettings();
        profile.setRobotUID("Makelangelo Huge");
        profile.setMachineSize(1336,2000);
        profile.saveConfig();
    }

    /**
     * Load all profiles from the preferences tree.  Subsequent calls will reload the list.
     */
    public void loadAllProfiles() {
        profileNames.clear();

        try {
            profileNames.addAll( List.of( topLevelMachinesPreferenceNode.childrenNames() ) );
        } catch (Exception e) {
            logger.error("Failed to load preferences", e);
            profileNames.add("Default");
        }
    }

    public Collection<String> getProfileNames() {
        return profileNames;
    }

    public PlotterSettings loadProfile(String name) {
        PlotterSettings plotterSettings = new PlotterSettings();
        plotterSettings.loadConfig(name);
        return plotterSettings;
    }

    /**
     *
     * @return a new instance of the last selected {@link PlotterSettings}.
     */
    public PlotterSettings getLastSelectedProfile() {
        String uid = topLevelMachinesPreferenceNode.get(KEY_MACHINE_LAST_SELECTED, "0");
        return loadProfile(uid);
    }

    public void setLastSelectedProfile(String robotUID) {
        topLevelMachinesPreferenceNode.put(KEY_MACHINE_LAST_SELECTED, robotUID);
    }

    /**
     *
     * @param robotUID
     * @return true if there was a problem deleting the profile.
     */
    public boolean deleteProfile(String robotUID) {
        if(robotUID == null) return true;
        try {
            Preferences thisMachineNode = topLevelMachinesPreferenceNode.node(robotUID);
            thisMachineNode.removeNode();
            profileNames.remove(robotUID);
        }
        catch (Exception e) {
            logger.error("Failed to delete profile {}. {}", robotUID, e);
            return true;
        }
        return false;
    }

}
