package com.marginallyclever.makelangelo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.util.PreferencesHelper;


public class MakelangeloTests {
	private Makelangelo m;
	
	@BeforeAll
	public void beforeAll() {
		System.out.println("Log.start");
		Log.start();
		System.out.println("PreferencesHelper.start");
		PreferencesHelper.start();
		System.out.println("Translator.start");
		Translator.start();
		System.out.println("m = new Makelangelo");
		m = new Makelangelo();
		System.out.println("Ready");
	}
	
	@AfterAll
	public void afterAll() {
		System.out.println("Log.end");
		Log.end();
	}
	
	@Test
	public void checkVersion() throws IllegalStateException {
		
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
