package com.marginallyclever.makelangelo;

import org.junit.jupiter.api.Test;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.util.PreferencesHelper;


public class MakelangeloTests {	
	@Test
	public void checkVersion() throws IllegalStateException {
		System.out.println("Log.start");
		Log.start();
		System.out.println("PreferencesHelper.start");
		PreferencesHelper.start();
		System.out.println("Translator.start");
		Translator.start();
		System.out.println("m = new Makelangelo");
		Makelangelo m = new Makelangelo();
		System.out.println("Ready");
		
		String [] toks = m.VERSION.split("\\.");
		if(toks.length!=3) {
			throw new IllegalStateException("Makelangelo.VERSION must be major.minor.tiny.");
		}
		try {
			Integer.parseInt(toks[0]);
		} catch(NumberFormatException e) {
			throw new IllegalStateException("Makelangelo.VERSION must start with a number, not a letter.");
		}
		System.out.println("Log.end");
		Log.end();
	}
}
