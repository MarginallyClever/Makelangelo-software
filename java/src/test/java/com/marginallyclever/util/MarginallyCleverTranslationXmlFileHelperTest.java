package com.marginallyclever.util;

import org.junit.Assert;
import org.junit.Test;

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
