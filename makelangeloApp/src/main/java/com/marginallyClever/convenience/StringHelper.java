package com.marginallyClever.convenience;

import java.util.Locale;

public class StringHelper {
	
	/**
	 * format floats with 3 decimal places in US locale ONLY
	 * @param arg0
	 * @return the formatted string
	 */
	public static String formatFloat(float arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format(Locale.US,"%.3f", arg0);
	}

	/**
	 * format doubles with 3 decimal places in US locale ONLY
	 * @param arg0
	 * @return the formatted string
	 */
	public static String formatDouble(double arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format(Locale.US,"%.3f", arg0);
	}
	
	/**
	 * @param s string to pad
	 * @param n number of spaces to pad
	 * @return newly formatted string.
	 */
	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}

	public static String getElapsedTime(int seconds) {
	    seconds /= 60;
	    long minutes = seconds % 60;
	    seconds /= 60;
	    long hours = seconds % 24;
	    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
}
