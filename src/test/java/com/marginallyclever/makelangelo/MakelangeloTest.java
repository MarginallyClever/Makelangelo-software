package com.marginallyclever.makelangelo;

import com.marginallyclever.util.PropertiesFileHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MakelangeloTest {
    @Test
    public void checkVersion() throws IllegalStateException {
        String version = PropertiesFileHelper.getMakelangeloVersionPropertyValue();
        System.out.println("version " + version);
        String[] toks = version.split("\\.");
        Assertions.assertEquals( 3, toks.length, "Version must be major.minor.tiny.");
        Assertions.assertNotNull(Integer.valueOf(toks[0]));
    }
}
