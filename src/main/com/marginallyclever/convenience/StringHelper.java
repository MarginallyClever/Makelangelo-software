package com.marginallyclever.convenience;

import java.util.Locale;

public class StringHelper {
	
	/**
	 * format floats with 3 decimal places in US locale ONLY
	 * @param arg0
	 * @return the formatted string
	 */
	static public String formatFloat(float arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format(Locale.US,"%.3f", arg0);
	}

	/**
	 * format doubles with 3 decimal places in US locale ONLY
	 * @param arg0
	 * @return the formatted string
	 */
	static public String formatDouble(double arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format(Locale.US,"%.3f", arg0);
	}
}
