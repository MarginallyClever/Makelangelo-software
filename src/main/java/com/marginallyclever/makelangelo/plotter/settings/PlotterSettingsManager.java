package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.BackingStoreException;
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
    private final List<String> configurationNames = new ArrayList<>();
    private final Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);

    public PlotterSettingsManager() {
        super();

        loadAllConfigurations();
    }

    public void loadAllConfigurations() {
        configurationNames.clear();

        try {
            configurationNames.addAll( List.of( topLevelMachinesPreferenceNode.childrenNames() ) );
        } catch (Exception e) {
            logger.error("Failed to load preferences", e);
            configurationNames.add("Default");
        }
    }

    public Collection<String> getProfileNames() {
        return configurationNames;
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

    public void deleteProfile(String robotUID) throws BackingStoreException {
        Preferences thisMachineNode = topLevelMachinesPreferenceNode.node(robotUID);

        thisMachineNode.removeNode();
    }
}
