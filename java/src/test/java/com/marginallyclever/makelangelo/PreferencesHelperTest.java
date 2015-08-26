package com.marginallyclever.makelangelo;

import com.marginallyclever.util.MarginallyCleverJsonFilePreferencesFactory;
import com.marginallyclever.util.MarginallyCleverPreferences;
import com.marginallyclever.util.UnitTestHelper;
import org.json.JSONObject;
import org.json.Property;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created on 5/25/15.
 *
 * @author Peter Colapietro
 * @since v7.1.4
 */
public class PreferencesHelperTest {

    /**
     *
     */
    private final Preferences preferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MAKELANGELO_ROOT);

    /**
     *
     */
    private final MarginallyCleverPreferences marginallyCleverJsonPreferenceNode = new MarginallyCleverPreferences(preferenceNode, "JSON");

    /**
     *
     */
    private final Logger logger = LoggerFactory.getLogger(PreferencesHelperTest.class);

    /**
     *
     * @throws Exception
     */
    @SuppressWarnings("EmptyMethod")
    @org.junit.Before
    public void setUp() throws Exception {
    }

    /**
     *
     * @throws Exception
     */
    @org.junit.After
    public void tearDown() throws Exception {
        marginallyCleverJsonPreferenceNode.removeNode();
    }

    /**
     *
     */
    @SuppressWarnings({ "UnusedDeclaration", "unused" })
    public void testCopyPreferenceNode() {
        try {
            PreferencesHelper.clearAll(marginallyCleverJsonPreferenceNode);
        } catch (BackingStoreException e) {
            logger.error("{}", e);
        }
        PreferencesHelper.copyPreferenceNode(preferenceNode, marginallyCleverJsonPreferenceNode);
        final File preferencesFile = MarginallyCleverJsonFilePreferencesFactory.getPreferencesFile();
        final Properties p = new Properties();
        try(final FileInputStream inStream = new FileInputStream(preferencesFile)) {
            p.load(inStream);
        } catch (IOException e) {
            logger.error("{}", e);
        }
        final JSONObject jsonObject = Property.toJSONObject(p);
        logger.debug("{}", jsonObject);
        @SuppressWarnings("unchecked")
        final JSONObject object = new JSONObject(marginallyCleverJsonPreferenceNode.getChildren());
        logger.debug("{}", object);
    }

    @Test
    public void testMachineConfigurationNames() throws BackingStoreException {
        final String thisMethodsName = Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
        logger.info("start: {}", thisMethodsName);
        final Preferences machinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
        logger.info("node name: {}", machinesPreferenceNode.name());
        final String[] childrenPreferenceNodeNames = machinesPreferenceNode.childrenNames();
        for (String childNodeName : childrenPreferenceNodeNames) {
            logger.info("child node name: {}", childNodeName);
            final boolean isMachineNameAnInteger = UnitTestHelper.isInteger(childNodeName);
            Assert.assertTrue(isMachineNameAnInteger);
            //Machine configurations numbered -1 and below should not exist.
            final boolean isMachineNameLessThanZero = Integer.parseInt(childNodeName) < 0;
            Assert.assertFalse(isMachineNameLessThanZero);
        }
        logger.info("end: {}", thisMethodsName);
    }

    /**
     * Over engineered. There are <a href="http://stackoverflow.com/a/442773">pitfalls</a> to this method of getting a
     * {@code StackTraceElement}'s index which this method does not address. I make a best guess based upon development
     * environment testing.
     *
     * @see <a href="http://stackoverflow.com/a/8592871">Getting the name of the current executing method</a>
     */
    private static final int CLIENT_CODE_STACK_INDEX;

    static {
        /*
         Finds out the index of "this code" in the returned stack trace - funny but it differs in JDK 1.5, and 1.6.
         In my tests I had to modify to get to work in 1.7 and 1.8.
        */
        int i = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            /* original placement of increment via SO
            i++;
             */
            if (ste.getClassName().equals(PreferencesHelperTest.class.getName())) {
                break;
            }
            i++;//My placement of increment via environmental testing.
        }
        CLIENT_CODE_STACK_INDEX = i;
    }
}
