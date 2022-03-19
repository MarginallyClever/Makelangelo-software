package com.marginallyclever.makelangelo;

import com.marginallyclever.util.PropertiesFileHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MakelangeloTest {
    private static final Logger logger = LoggerFactory.getLogger(MakelangeloTest.class);

    @Test
    public void checkVersion() throws IllegalStateException {
        String version = PropertiesFileHelper.getMakelangeloVersion();
        logger.debug("version {}", version);
        String[] toks = version.split("\\.");
        Assertions.assertEquals( 3, toks.length, "Version must be major.minor.tiny.");
        Assertions.assertNotNull(Integer.valueOf(toks[0]));
    }

    @Test
    public void checkGitVersion() throws IllegalStateException {
        String version = PropertiesFileHelper.getMakelangeloGitVersion();
        logger.debug("version {}", version);
        Assertions.assertFalse(version.isBlank());
    }
}
