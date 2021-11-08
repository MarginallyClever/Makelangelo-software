package com.marginallyclever.makelangelo;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.util.PreferencesHelper;
import com.marginallyclever.util.PropertiesFileHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MakelangeloGUITest {
    @Test
    public void checkVersion() throws IllegalStateException {
        System.out.println("Log.start");
        Log.start();
        System.out.println("PreferencesHelper.start");
        PreferencesHelper.start();
        System.out.println("Translator.start");
        Translator.start();
        String version = PropertiesFileHelper.getMakelangeloVersionPropertyValue();
        System.out.println("version " + version);
        String[] toks = version.split("\\.");
        Assertions.assertEquals( 3, toks.length, "Version must be major.minor.tiny.");
        Assertions.assertNotNull(Integer.valueOf(toks[0]));
        Log.end();
    }
}
