package com.marginallyclever.makelangelo;

import org.junit.Test;

import com.marginallyclever.convenience.log.Log;


public class VersionTest {
	@Test
	public void checkVersion() throws IllegalStateException {
		Log.start();
		
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
		
		Log.end();
	}
}
