package com.marginallyclever.makelangelo.plotter.plottersettings;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRendererFactory;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Manages the list of all existing {@link PlotterSettings}.
 * @author Dan Royer
 * @since 7.33.2
 */
public class PlotterSettingsManager {
    private static final Logger logger = LoggerFactory.getLogger(PlotterSettingsManager.class);
    public static final String KEY_MACHINE_LAST_SELECTED = "lastLoadedMachine";
    private final List<String> profileNames = new ArrayList<>();
    private final Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);

    public PlotterSettingsManager() {
        super();
        writeMakelangelo5();
        writeMakelangeloHugeProfile();
        loadAllProfiles();
    }

    private void writeMakelangelo5() {
        PlotterSettings profile = buildMakelangelo5();
        profile.save();
    }

    private void writeMakelangeloHugeProfile() {
        PlotterSettings profile = buildMakelangelo5();
        profile.setRobotUID("Makelangelo Huge");
        profile.setMachineSize(1336,2000);
        profile.setString(PlotterSettings.STYLE,"MAKELANGELO_5_HUGE");
        profile.save();
    }

    public static PlotterSettings buildMakelangelo5() {
        PlotterSettings profile = new PlotterSettings();
        profile.setRobotUID("Makelangelo 5");

        profile.setBoolean(PlotterSettings.IS_REGISTERED, false);
        profile.setBoolean(PlotterSettings.HANDLE_SMALL_SEGMENTS, false);

        profile.setMachineSize(650,1000);
        profile.setDouble(PlotterSettings.MIN_SEGMENT_LENGTH, 0.5);	// mm
        profile.setDouble(PlotterSettings.MAX_ACCELERATION, 100);	// mm/s/s
        profile.setDouble(PlotterSettings.MIN_ACCELERATION, 0.0);	// mm/s/s
        profile.setDouble(PlotterSettings.MINIMUM_PLANNER_SPEED, 0.05);	// mm/s
        profile.setDouble(PlotterSettings.DIAMETER, 0.8);	// mm, >0
        profile.setDouble(PlotterSettings.PEN_ANGLE_UP, 90);	// servo angle (degrees,0...180)
        profile.setDouble(PlotterSettings.PEN_ANGLE_DOWN, 25);	// servo angle (degrees,0...180)
        profile.setDouble(PlotterSettings.PEN_ANGLE_UP_TIME, 50);
        profile.setDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME, 50);
        profile.setDouble(PlotterSettings.FEED_RATE_TRAVEL, 3000);	// mm/min.  3000 = 50 mm/s
        profile.setDouble(PlotterSettings.FEED_RATE_DRAW, 3000);	// mm/min.  3000 = 50 mm/s

        profile.setInteger(PlotterSettings.BLOCK_BUFFER_SIZE, 16);
        profile.setInteger(PlotterSettings.SEGMENTS_PER_SECOND, 5);
        profile.setInteger(PlotterSettings.MIN_SEG_TIME, 20000);		// us
        profile.setInteger(PlotterSettings.STARTING_POS_INDEX, 4);
        profile.setInteger(PlotterSettings.Z_MOTOR_TYPE, PlotterSettings.Z_MOTOR_TYPE_SERVO);

        profile.setString(PlotterSettings.USER_GENERAL_START_GCODE, "");
        profile.setString(PlotterSettings.USER_GENERAL_END_GCODE, "");
        profile.setString(PlotterSettings.STYLE, PlotterRendererFactory.MAKELANGELO_5.getName());

        profile.setColor(PlotterSettings.PAPER_COLOR,new ColorRGB(255, 255, 255));
        profile.setColor(PlotterSettings.PEN_DOWN_COLOR_DEFAULT,new ColorRGB(0, 0, 0));
        profile.setColor(PlotterSettings.PEN_DOWN_COLOR,new ColorRGB(0, 0, 0));
        profile.setColor(PlotterSettings.PEN_UP_COLOR,new ColorRGB(0, 255, 0));

        profile.setDoubleArray(PlotterSettings.MAX_JERK,new double []{ 10, 10, 0.3 });
        return profile;
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
        plotterSettings.load(name);
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
