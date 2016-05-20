package com.marginallyclever.makelangelo;

import org.junit.Test;


public class VersionTest {
	@Test
	public void checkVersion() throws IllegalStateException {
		String [] toks = Makelangelo.VERSION.split("\\.");
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
