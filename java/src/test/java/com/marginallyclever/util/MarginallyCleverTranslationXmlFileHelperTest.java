package com.marginallyclever.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MarginallyCleverTranslationXmlFileHelperTest {

    /**
     *
     */
    public static final boolean LOG_MISSING_KEYS = true;

    @Test
    public void testAreLanguageFilesMissingKeys() {
        final boolean areLanguageFilesMissingKeys = MarginallyCleverTranslationXmlFileHelper.areLanguageFilesMissingKeys(LOG_MISSING_KEYS);
        Assert.assertFalse(areLanguageFilesMissingKeys); // Make sure the language files are not missing any translation keys.
    }

}
