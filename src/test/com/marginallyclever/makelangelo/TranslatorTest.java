package com.marginallyclever.makelangelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.util.PreferencesHelper;

public class TranslatorTest {
	@BeforeEach
	public void beforeEach() {
		Log.start();
		PreferencesHelper.start();
		Translator.start();
	}
	
	@AfterEach
	public void afterEach() {
		Log.end();
	}
	
	@Test
	public void loadLanguage() {
		int current = Translator.getCurrentLanguageIndex();
		String [] available = Translator.getLanguageList();
		System.out.println("current language="+available[current]);
	}
	
	@Test
	public void changeLanguageTest() {
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
	}
}
