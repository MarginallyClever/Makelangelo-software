package com.marginallyclever.makelangelo;

import com.marginallyclever.util.MarginallyCleverPreferences;
import com.marginallyclever.util.PreferencesHelper;
import com.marginallyclever.util.UnitTestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created on 5/25/15.
 *
 * @author Peter Colapietro
 * @since v7.1.4
 */
public class PreferencesHelperTest {
	private static final Logger logger = LoggerFactory.getLogger(PreferencesHelperTest.class);
	private static AbstractPreferences preferenceNode;
	private static MarginallyCleverPreferences marginallyCleverJsonPreferenceNode;


	/**
	 * Over engineered. There are <a href="http://stackoverflow.com/a/442773">pitfalls</a> to this method of getting a
	 * {@code StackTraceElement}'s index which this method does not address. I make a best guess based upon development
	 * environment testing.
	 *
	 * See <a href="http://stackoverflow.com/a/8592871">Getting the name of the current executing method</a>
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
	
	@SuppressWarnings("deprecation")
	@BeforeAll
	public static void beforeAll() {
		PreferencesHelper.start();
		preferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
		marginallyCleverJsonPreferenceNode = new MarginallyCleverPreferences(preferenceNode, "JSON");
	}
	
	/**
	 * @throws Exception
	 */
	@AfterAll
	public static void afterAll() throws Exception {
		marginallyCleverJsonPreferenceNode.removeNode();
	}

	@Test
	public void testMachineConfigurationNames() throws BackingStoreException {
		final String thisMethodsName = Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
		logger.debug("start: {}#{}", PreferencesHelperTest.class.getName(), thisMethodsName);
		final Preferences machinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		logger.debug("node name: {}", machinesPreferenceNode.name());
		final String[] childrenPreferenceNodeNames = machinesPreferenceNode.childrenNames();
		for (String childNodeName : childrenPreferenceNodeNames) {
			logger.debug("child node name: {}", childNodeName);
			final boolean isMachineNameAnInteger = UnitTestHelper.isInteger(childNodeName);
			assertTrue(isMachineNameAnInteger);
			//Machine configurations numbered -1 and below should not exist.
			final boolean isMachineNameLessThanZero = Integer.parseInt(childNodeName) < 0;
			//Assert.assertFalse(isMachineNameLessThanZero);
			if(isMachineNameLessThanZero) {
				logger.debug("REMOVED");
				machinesPreferenceNode.remove(childNodeName);
			}
		}
		machinesPreferenceNode.flush();
		logger.debug("end: {}", thisMethodsName);
	}
}
