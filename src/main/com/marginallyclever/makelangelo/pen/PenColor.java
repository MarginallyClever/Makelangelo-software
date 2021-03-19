package com.marginallyclever.makelangelo.pen;

public class PenColor {
	// hex color (0xRRGGBBAA)
	public int hexValue;
	// english(?) name
	public String name;
	
	public PenColor(int hex,String knownAs) {
		hexValue=hex;
		name=knownAs;
	}
}
