package com.marginallyclever.makelangelo.plotter.plottersettings;

import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;

public class PlotterSettingsManagerTest {
    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
    }

    @Test
    public void testDefaultProfilesExist() {
        PlotterSettingsManager plotterSettingsManager = new PlotterSettingsManager();
        Collection<String> list = plotterSettingsManager.getProfileNames();
        assert(list.contains("Makelangelo 5"));
        assert(list.contains("Makelangelo Huge"));
    }
}
