package com.marginallyclever.makelangelo;

import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PreferencesHelperTest {
	@BeforeAll
	public static void beforeAll() {
		PreferencesHelper.start();
	}

	@BeforeEach
	public void prepareData() throws BackingStoreException {
		Preferences machinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences unitTestPreferenceNode = machinesPreferenceNode.node("UnitTest");
		unitTestPreferenceNode.put("key1", "value1");
		unitTestPreferenceNode.put("key2", "value2");
		unitTestPreferenceNode.put("key3", "value3");
		Preferences unitTestPreferenceNode2 = unitTestPreferenceNode.node("UnitTest2");
		unitTestPreferenceNode2.put("key4", "value4");
		machinesPreferenceNode.flush();
		PreferencesHelper.logPreferenceNode(machinesPreferenceNode);
	}

	@AfterEach
	public void purgeData() throws BackingStoreException {
		Preferences machinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		machinesPreferenceNode.node("UnitTest").removeNode();
		PreferencesHelper.logPreferenceNode(machinesPreferenceNode);
	}

	@Test
	public void loadSingleValue() {
		Preferences machinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences unitTestPreferenceNode = machinesPreferenceNode.node("UnitTest");
		assertEquals("value1", unitTestPreferenceNode.get("key1", "no"));
	}

	@Test
	public void loadMultipleValues() {
		Preferences machinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences unitTestPreferenceNode = machinesPreferenceNode.node("UnitTest");
		assertEquals("value1", unitTestPreferenceNode.get("key1", "no"));
		assertEquals("value2", unitTestPreferenceNode.get("key2", "no"));
		assertEquals("value3", unitTestPreferenceNode.get("key3", "no"));
	}

	@Test
	public void loadNestedalues() {
		Preferences machinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences unitTestPreferenceNode = machinesPreferenceNode.node("UnitTest").node("UnitTest2");
		assertEquals("value4", unitTestPreferenceNode.get("key4", "no"));
	}
}
