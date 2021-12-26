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

	@BeforeAll
	public static void beforeAll() {
		PreferencesHelper.start();
	}

	@Test
	public void testMachineConfigurationNames() throws BackingStoreException {
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
	}
}
