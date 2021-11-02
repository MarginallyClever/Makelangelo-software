package com.marginallyclever.makelangelo;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.util.PreferencesHelper;
import com.marginallyclever.util.PropertiesFileHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MakelangeloTests {
    @Test
    @Disabled("for the CI")
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
        if (toks.length != 3) {
            throw new IllegalStateException("Version must be major.minor.tiny.");
        }
        try {
            Integer.parseInt(toks[0]);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Version must start with a number, not a letter.");
        }
        System.out.println("Log.end");
        Log.end();
    }
}
