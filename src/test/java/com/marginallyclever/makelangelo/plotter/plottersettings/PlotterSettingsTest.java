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
        plotterSettings.setLimitTop(2);
        plotterSettings.setLimitBottom(3);
        plotterSettings.setLimitRight(4);
        plotterSettings.setLimitLeft(5);
        plotterSettings.setStartingPositionIndex(6);
        plotterSettings.setPenDiameter(7);
        plotterSettings.setPenLiftTime(8);
        plotterSettings.setPenDownAngle(9);
        plotterSettings.setPenUpAngle(10);
        plotterSettings.setTravelFeedRate(11);
        plotterSettings.setPenDownColorDefault(new ColorRGB(12, 13, 14));
        plotterSettings.setPenUpColor(new ColorRGB(15, 16, 17));
        plotterSettings.setPaperColor(new ColorRGB(18, 19, 20));
        plotterSettings.setDrawFeedRate(21);
        plotterSettings.setAcceleration(22);
        plotterSettings.setHardwareName("TestRobot");
		plotterSettings.setBlockBufferSize(23);
		plotterSettings.setSegmentsPerSecond(24);
		plotterSettings.setMinSegmentLength(25);
		plotterSettings.setMinSegmentTime(26);
		plotterSettings.setHandleSmallSegments(false);
		plotterSettings.setMinAcceleration(27);
		plotterSettings.setMinPlannerSpeed(28);
        plotterSettings.setPenLowerTime(29);

        PlotterRendererFactory[] allMachines = PlotterRendererFactory.values();
        int index = (int)(Math.random()*allMachines.length);
        String styleName = allMachines[index].getName();
        plotterSettings.setStyle(styleName);

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
        plotterSettings.setZMotorType(type);

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