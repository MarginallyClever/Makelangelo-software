package com.marginallyclever.makelangelo;

import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TranslatorTests {
	@BeforeAll
	public static void beforeAll() {
		PreferencesHelper.start();
	}

	@Test
	public void startTranslatorTwiceTest() {
		Translator.start();
		System.out.println(Arrays.toString(Translator.getLanguageList()));
		Translator.start();
		System.out.println(Arrays.toString(Translator.getLanguageList()));
	}
	
	@Test
	public void loadLanguageTest() {
		Translator.start();
		int current = Translator.getCurrentLanguageIndex();
		String [] available = Translator.getLanguageList();
		System.out.println("current language="+available[current]);
	}
	
	@Test
	public void changeLanguageTest() {
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
	}
}
