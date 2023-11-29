package com.marginallyclever.makelangelo.plotter.plottersettings;

import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    @Test
    public void ancestryTest() {
        PlotterSettingsManager plotterSettingsManager = new PlotterSettingsManager();
        PlotterSettings m5 = new PlotterSettings("Makelangelo 5");
        // get a unique name
        String name1 = m5.getUID();
        List<String> names = new ArrayList<>(plotterSettingsManager.getProfileNames());
        String name2 = getUniqueName(names,name1);
        names.add(name2);
        String name3 = getUniqueName(names,name1);

        plotterSettingsManager.saveAs(m5.getUID(),name2);
        plotterSettingsManager.saveAs(name2,name3);
        PlotterSettings a2 = new PlotterSettings(name2);
        PlotterSettings a3 = new PlotterSettings(name2);
        String name4 = a2.getProgenitor();
        boolean b2 = a2.getProgenitor().equals(m5.getProgenitor());
        boolean b3 = a3.getProgenitor().equals(m5.getProgenitor());
        plotterSettingsManager.deleteProfile(name2);
        plotterSettingsManager.deleteProfile(name3);
        assert(!name4.isEmpty());
        assert(b2);
        assert(b3);
    }

    private String getUniqueName(List<String> names,String name1) {
        String name2;
        int i=1;
        do {
            name2 = name1 + i;
            i++;
        } while(names.contains(name2));
        return name2;
    }
}
