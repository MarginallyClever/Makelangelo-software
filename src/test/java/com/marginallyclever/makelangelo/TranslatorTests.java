package com.marginallyclever.makelangelo;

import com.marginallyclever.util.FindAllTraductionGet;
import static com.marginallyclever.util.FindAllTraductionGet.getTraductionGetStringMissingKey;
import static com.marginallyclever.util.FindAllTraductionGet.matchTraductionGetInAllSrcJavaFiles;
import com.marginallyclever.util.FindAllTraductionResult;
import com.marginallyclever.util.PreferencesHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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

	@Test
	public void searchSimpleStringMissingKeyUsedAsArgumentForTranslatorGetInTheSrcCode() {	    
	    try {
		//Pre requis
		Translator.start();
		
		String baseDirToSearch = "src" + File.separator + "main" + File.separator + "java";
		System.out.printf("PDW=%s\n", new File(".").getAbsolutePath());
		File srcDir = new File(".", baseDirToSearch);
		try {
		    System.out.printf("srcDir=%s\n", srcDir.getCanonicalPath());
		} catch (IOException ex) {
		    ex.printStackTrace();
		}

		Map<FindAllTraductionResult, Path> mapMatchResultToFilePath = matchTraductionGetInAllSrcJavaFiles(srcDir);

		SortedMap<String, ArrayList<FindAllTraductionResult>> groupIdenticalMissingKey = getTraductionGetStringMissingKey(mapMatchResultToFilePath);
		System.out.printf("groupIdenticalMissingKey.size()=%d\n", groupIdenticalMissingKey.size());
		
		// output the missing keys if any.
		for (String k : groupIdenticalMissingKey.keySet()) {
		    System.out.printf("missing traduction key : \"%s\"\n", k);
		    for (FindAllTraductionResult tr : groupIdenticalMissingKey.get(k)) {
			System.out.printf(" used in : \"%s\" line %d\n", tr.pSrc, tr.lineInFile);
		    }
		}
		
		// validate or not the test. (succes if no missing keys found)
		assertTrue(groupIdenticalMissingKey.isEmpty(), "Some traduction missing keys !?.");
		
	    } catch (Exception e) {
		e.printStackTrace();		
	    }
	}
}
