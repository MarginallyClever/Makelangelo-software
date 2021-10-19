package com.marginallyclever.makelangelo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.util.PreferencesHelper;


public class VersionTest {
	@BeforeAll
	public static void beforeAll() {
		Log.start();
		PreferencesHelper.start();
		Translator.start();
	}
	@AfterAll
	public static void afterAll() {
		Log.end();
	}
	
	@Test
	public void checkVersion() throws IllegalStateException {
		Makelangelo m = new Makelangelo();
		
		String [] toks = m.VERSION.split("\\.");
		if(toks.length!=3) {
			throw new IllegalStateException("Makelangelo.VERSION must be major.minor.tiny.");
		}
		try {
			Integer.parseInt(toks[0]);
		} catch(NumberFormatException e) {
			throw new IllegalStateException("Makelangelo.VERSION must start with a number, not a letter.");
		}
	}
}
