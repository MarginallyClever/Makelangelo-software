package com.marginallyclever.makelangelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.util.PreferencesHelper;

public class TranslatorTests {
	@BeforeAll
	public static void beforeAll() {
		System.out.println("beforeAll() start");
		Log.start();
		PreferencesHelper.start();
		System.out.println("beforeAll() end");
	}
	
	@AfterAll
	public static void afterAll() {
		System.out.println("afterAll() start");
		Log.end();
		System.out.println("afterAll() end");
	}
	
	//@Test
	public void startTranslatorTwiceTest() {
		System.out.println("startTranslatorTwiceTest() start");
		Translator.start();
		System.out.println(Arrays.toString(Translator.getLanguageList()));
		Translator.start();
		System.out.println(Arrays.toString(Translator.getLanguageList()));
		System.out.println("startTranslatorTwiceTest() end");
	}
	
	//@Test
	public void loadLanguageTest() {
		System.out.println("loadLanguageTest() start");
		Translator.start();
		int current = Translator.getCurrentLanguageIndex();
		String [] available = Translator.getLanguageList();
		System.out.println("current language="+available[current]);
		System.out.println("loadLanguageTest() end");
	}
	
	@Test
	public void changeLanguageTest() {
		System.out.println("changeLanguageTest() start");
		Translator.start();
		System.out.println("...a");
		String [] available = Translator.getLanguageList();
		System.out.println("...b");
		assertTrue(available.length>1,"More than one language needed to complete test.");
		int current = Translator.getCurrentLanguageIndex();
		System.out.println("...c");
		int next = (current+1)%available.length;
		System.out.println("...d");
		Translator.setCurrentLanguage(available[next]);
		System.out.println("...e");
		Translator.saveConfig();
		System.out.println("...f");
		Translator.loadConfig();
		System.out.println("...g");
		int read = Translator.getCurrentLanguageIndex();
		System.out.println("...h");
		assertEquals(read,next,"Changing language failed.");
		// return to previous state
		Translator.setCurrentLanguage(available[current]);
		System.out.println("...i");
		Translator.saveConfig();
		System.out.println("changeLanguageTest() end");
	}
}
