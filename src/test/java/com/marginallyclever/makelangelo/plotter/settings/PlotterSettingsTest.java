package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

public class PlotterSettingsTest {

    private static final long ROBOT_UID = 123456;

    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
    }

    @Test
    void saveAndLoadConfig() {
        // given
        PlotterSettings plotterSettings = new PlotterSettings();
        plotterSettings.setRobotUID(ROBOT_UID);
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

        // when
        plotterSettings.saveConfig();

        // then
        PlotterSettings plotterSettingsRead = new PlotterSettings();
        plotterSettingsRead.loadConfig(ROBOT_UID);
        assertEquals(2, plotterSettingsRead.getLimitTop());
        assertEquals(3, plotterSettingsRead.getLimitBottom());
        assertEquals(4, plotterSettingsRead.getLimitRight());
        assertEquals(5, plotterSettingsRead.getLimitLeft());
        assertEquals(6, plotterSettingsRead.getStartingPositionIndex());
        assertEquals(7, plotterSettingsRead.getPenDiameter());
        assertEquals(8, plotterSettingsRead.getPenLiftTime());
        assertEquals(9, plotterSettingsRead.getPenDownAngle());
        assertEquals(10, plotterSettingsRead.getPenUpAngle());
        assertEquals(11, plotterSettingsRead.getTravelFeedRate());
        assertEquals(new ColorRGB(12, 13, 14), plotterSettingsRead.getPenDownColorDefault());
        assertEquals(new ColorRGB(15, 16, 17), plotterSettingsRead.getPenUpColor());
        assertEquals(new ColorRGB(18, 19, 20), plotterSettingsRead.getPaperColor());
        assertEquals(21, plotterSettingsRead.getDrawFeedRate());
        assertEquals(22, plotterSettingsRead.getMaxAcceleration());
        assertEquals("TestRobot", plotterSettingsRead.getHardwareName());
    }

    @AfterEach
    public void clean() {
        Preferences topLevelMachinesPreferenceNode = PreferencesHelper
                .getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
        Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(ROBOT_UID));
        if (uniqueMachinePreferencesNode != null) {
            try {
                uniqueMachinePreferencesNode.removeNode();
            } catch (BackingStoreException e) {
                // Nothing to do
            }
        }
    }
}