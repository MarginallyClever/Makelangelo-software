package com.marginallyclever.makelangelo;

import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TranslatorTest {
	@BeforeAll
	public static void beforeAll() {
		PreferencesHelper.start();
		Translator.start();
	}

	@Test
	public void startTranslatorTwiceTest() {
		String[] first = Translator.getLanguageList();
		Translator.start();
		String[] second = Translator.getLanguageList();
		assertArrayEquals(first, second);
	}
	
	@Test
	public void loadLanguageTest() {
		String [] available = Translator.getLanguageList();
		int current = Translator.getCurrentLanguageIndex();
		assertNotNull(available[current]);
	}
	
	@Test
	public void changeLanguageTest() {
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

	@Test
	public void getOneValueThatExists() {
		assertEquals("Makelangelo", Translator.get("Robot"));
	}

	@Test
	public void getOneValueThatDoesNotExist() {
		assertEquals(Translator.MISSING + "DoesNotExist", Translator.get("DoesNotExist"));
	}
}
