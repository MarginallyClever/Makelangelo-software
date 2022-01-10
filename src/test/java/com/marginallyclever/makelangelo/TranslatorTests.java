package com.marginallyclever.makelangelo;

import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TranslatorTests {
	@BeforeAll
	public static void beforeAll() {
		PreferencesHelper.start();
	}

	@Test
	public void startTranslatorTwiceTest() {
		Translator.start();
		String[] first = Translator.getLanguageList();
		Translator.start();
		String[] second = Translator.getLanguageList();
		assertArrayEquals(first, second);
	}
	
	@Test
	public void loadLanguageTest() {
		Translator.start();
		int current = Translator.getCurrentLanguageIndex();
		assertNotEquals(0, current);
		String [] available = Translator.getLanguageList();
		assertNotNull(available[current]);
	}
	
	@Test
	public void changeLanguageTest() {
		Translator.start();
		String[] available = Translator.getLanguageList();
		assertTrue(available.length > 1, "More than one language needed to complete test.");
		int current = Translator.getCurrentLanguageIndex();
		try {
			int next = (current + 1) % available.length;
			Translator.setCurrentLanguage(available[next]);
			Translator.saveConfig();
			Translator.loadConfig();
			int read = Translator.getCurrentLanguageIndex();
			assertEquals(read, next, "Changing language failed.");
		} finally {
			// return to previous state
			Translator.setCurrentLanguage(available[current]);
			Translator.saveConfig();
		}
	}
}
