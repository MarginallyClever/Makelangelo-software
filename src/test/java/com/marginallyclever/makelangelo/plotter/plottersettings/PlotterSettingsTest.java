package com.marginallyclever.makelangelo.plotter.plottersettings;

import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRendererFactory;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PlotterSettingsTest {
    public static final String ROBOT_TEST_UID = "123456";

    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
    }

    @Test
    public void saveAndLoadConfig() {
        // given
        PlotterSettings expected = new PlotterSettings();
        expected.setRobotUID(ROBOT_TEST_UID);
        expected.setDouble(PlotterSettings.LIMIT_TOP,2);
        expected.setDouble(PlotterSettings.LIMIT_BOTTOM,3);
        expected.setDouble(PlotterSettings.LIMIT_RIGHT,4);
        expected.setDouble(PlotterSettings.LIMIT_LEFT,5);
        expected.setInteger(PlotterSettings.STARTING_POS_INDEX,6);
        expected.setDouble(PlotterSettings.DIAMETER,7);
        expected.setDouble(PlotterSettings.PEN_ANGLE_UP_TIME,8);
        expected.setDouble(PlotterSettings.PEN_ANGLE_DOWN,9);
        expected.setDouble(PlotterSettings.PEN_ANGLE_UP,10);
        expected.setDouble(PlotterSettings.FEED_RATE_TRAVEL,11);
        expected.setColor(PlotterSettings.PEN_DOWN_COLOR_DEFAULT,new Color(12, 13, 14));
        expected.setColor(PlotterSettings.PEN_UP_COLOR,new Color(15, 16, 17));
        expected.setColor(PlotterSettings.PAPER_COLOR,new Color(18, 19, 20));
        expected.setDouble(PlotterSettings.FEED_RATE_DRAW,21);
        expected.setDouble(PlotterSettings.MAX_ACCELERATION,22);
		expected.setInteger(PlotterSettings.BLOCK_BUFFER_SIZE,23);
		expected.setInteger(PlotterSettings.SEGMENTS_PER_SECOND,24);
		expected.setDouble(PlotterSettings.MIN_SEGMENT_LENGTH,25);
		expected.setInteger(PlotterSettings.MIN_SEG_TIME,26);
		expected.setBoolean(PlotterSettings.HANDLE_SMALL_SEGMENTS,false);
		expected.setDouble(PlotterSettings.MIN_ACCELERATION,27);
		expected.setDouble(PlotterSettings.MINIMUM_PLANNER_SPEED,28);
        expected.setDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME,29);
        expected.setString(PlotterSettings.ANCESTOR,"Makelangelo 5");

        PlotterRendererFactory[] allMachines = PlotterRendererFactory.values();
        int index = (int)(Math.random()*allMachines.length);
        String styleName = allMachines[index].getName();
        expected.setString(PlotterSettings.STYLE,styleName);

        // when
        expected.save();

        // then
        PlotterSettings actual = new PlotterSettings();
        actual.load(ROBOT_TEST_UID);

        Assertions.assertEquals(expected.toString(),actual.toString());
    }

    @Test
    public void saveAndLoadZAxis() {
        saveAndLoadZAxis("A %2 %1","B %1 %2");
        saveAndLoadZAxis("C %1 %2","D %2 %1");
    }

    public void saveAndLoadZAxis(String up,String down) {
        // given
        PlotterSettings plotterSettings = new PlotterSettings();
        plotterSettings.setRobotUID(ROBOT_TEST_UID);
        plotterSettings.setString(PlotterSettings.PEN_UP_GCODE, up);
        plotterSettings.setString(PlotterSettings.PEN_DOWN_GCODE, down);
        plotterSettings.save();

        // then
        PlotterSettings plotterSettingsRead = new PlotterSettings();
        plotterSettingsRead.load(ROBOT_TEST_UID);
        Assertions.assertEquals(plotterSettings.getString(PlotterSettings.PEN_UP_GCODE),plotterSettingsRead.getString(PlotterSettings.PEN_UP_GCODE));
        Assertions.assertEquals(plotterSettings.getString(PlotterSettings.PEN_DOWN_GCODE),plotterSettingsRead.getString(PlotterSettings.PEN_DOWN_GCODE));

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