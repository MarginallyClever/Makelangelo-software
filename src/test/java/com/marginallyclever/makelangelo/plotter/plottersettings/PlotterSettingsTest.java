package com.marginallyclever.makelangelo.plotter.plottersettings;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRendererFactory;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PlotterSettingsTest {
    private static final String ROBOT_TEST_UID = "123456";

    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
    }

    @Test
    public void saveAndLoadConfig() {
        // given
        PlotterSettings plotterSettings = new PlotterSettings();
        plotterSettings.setRobotUID(ROBOT_TEST_UID);
        plotterSettings.setDouble(PlotterSettings.LIMIT_TOP,2);
        plotterSettings.setDouble(PlotterSettings.LIMIT_BOTTOM,3);
        plotterSettings.setDouble(PlotterSettings.LIMIT_RIGHT,4);
        plotterSettings.setDouble(PlotterSettings.LIMIT_LEFT,5);
        plotterSettings.setInteger(PlotterSettings.STARTING_POS_INDEX,6);
        plotterSettings.setDouble(PlotterSettings.DIAMETER,7);
        plotterSettings.setDouble(PlotterSettings.PEN_ANGLE_UP_TIME,8);
        plotterSettings.setDouble(PlotterSettings.PEN_ANGLE_DOWN,9);
        plotterSettings.setDouble(PlotterSettings.PEN_ANGLE_UP,10);
        plotterSettings.setDouble(PlotterSettings.FEED_RATE_TRAVEL,11);
        plotterSettings.setColor(PlotterSettings.PEN_DOWN_COLOR_DEFAULT,new ColorRGB(12, 13, 14));
        plotterSettings.setColor(PlotterSettings.PEN_UP_COLOR,new ColorRGB(15, 16, 17));
        plotterSettings.setColor(PlotterSettings.PAPER_COLOR,new ColorRGB(18, 19, 20));
        plotterSettings.setDouble(PlotterSettings.FEED_RATE_DRAW,21);
        plotterSettings.setDouble(PlotterSettings.ACCELERATION,22);
        plotterSettings.setString(PlotterSettings.HARDWARE_VERSION,"TestRobot");
		plotterSettings.setInteger(PlotterSettings.BLOCK_BUFFER_SIZE,23);
		plotterSettings.setInteger(PlotterSettings.SEGMENTS_PER_SECOND,24);
		plotterSettings.setDouble(PlotterSettings.MIN_SEGMENT_LENGTH,25);
		plotterSettings.setInteger(PlotterSettings.MIN_SEG_TIME,26);
		plotterSettings.setBoolean(PlotterSettings.HANDLE_SMALL_SEGMENTS,false);
		plotterSettings.setDouble(PlotterSettings.MIN_ACCELERATION,27);
		plotterSettings.setDouble(PlotterSettings.MINIMUM_PLANNER_SPEED,28);
        plotterSettings.setDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME,29);

        PlotterRendererFactory[] allMachines = PlotterRendererFactory.values();
        int index = (int)(Math.random()*allMachines.length);
        String styleName = allMachines[index].getName();
        plotterSettings.setString(PlotterSettings.STYLE,styleName);

        // when
        plotterSettings.save();

        // then
        PlotterSettings plotterSettingsRead = new PlotterSettings();
        plotterSettingsRead.load(ROBOT_TEST_UID);

        Assertions.assertEquals(plotterSettings.toString(),plotterSettingsRead.toString());
    }

    @Test
    public void saveAndLoadZAxis() {
        saveAndLoadZAxis(PlotterSettings.Z_MOTOR_TYPE_SERVO);
        saveAndLoadZAxis(PlotterSettings.Z_MOTOR_TYPE_STEPPER);
    }

    public void saveAndLoadZAxis(int type) {
        // given
        PlotterSettings plotterSettings = new PlotterSettings();
        plotterSettings.setRobotUID(ROBOT_TEST_UID);
        plotterSettings.setInteger(PlotterSettings.Z_MOTOR_TYPE,type);

        plotterSettings.save();

        // then
        PlotterSettings plotterSettingsRead = new PlotterSettings();
        plotterSettingsRead.load(ROBOT_TEST_UID);
        Assertions.assertEquals(plotterSettings.getInteger(PlotterSettings.Z_MOTOR_TYPE),plotterSettingsRead.getInteger(PlotterSettings.Z_MOTOR_TYPE));

    }

    @AfterEach
    public void clean() {
        Preferences topLevelMachinesPreferenceNode = PreferencesHelper
                .getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
        Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(ROBOT_TEST_UID);
        if (uniqueMachinePreferencesNode != null) {
            try {
                uniqueMachinePreferencesNode.removeNode();
            } catch (BackingStoreException ignored) {}
        }
    }
}