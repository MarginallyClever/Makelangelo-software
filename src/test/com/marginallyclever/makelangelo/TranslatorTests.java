package com.marginallyclever.makelangelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.util.PreferencesHelper;

public class TranslatorTests {
	@BeforeEach
	@Timeout(15)
	public void beforeEach() {
		System.out.println("beforeEach() start");
		Log.start();
		PreferencesHelper.start();
		System.out.println("beforeEach() end");
	}
	
	@AfterEach
	@Timeout(15)
	public void afterEach() {
		System.out.println("afterEach() start");
		Log.end();
		System.out.println("afterEach() end");
	}
	
	@Test
	@Timeout(15)
	public void loadLanguageTest() {
		System.out.println("loadLanguageTest() start");
		Translator.start();
		int current = Translator.getCurrentLanguageIndex();
		String [] available = Translator.getLanguageList();
		System.out.println("current language="+available[current]);
		System.out.println("loadLanguageTest() end");
	}
	
	@Test
	@Timeout(15)
	public void changeLanguageTest() {
		System.out.println("changeLanguageTest() start");
		Translator.start();
		String [] available = Translator.getLanguageList();
		assertTrue(available.length>1,"More than one language needed to complete test.");
		int current = Translator.getCurrentLanguageIndex();
		int next = (current+1)%available.length;
		Translator.setCurrentLanguage(available[next]);
		Translator.saveConfig();
		Translator.loadConfig();
		int read = Translator.getCurrentLanguageIndex();
		assertEquals(read,next,"Changing language failed.");
		// return to previous state
		Translator.setCurrentLanguage(available[current]);
		Translator.saveConfig();
		System.out.println("changeLanguageTest() end");
	}
}
