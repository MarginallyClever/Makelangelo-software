package com.marginallyclever.makelangelo.paper;

/**
 * Convenience class to hold paper size information.
 */
public class PaperSize {
	public String name;
	public int width;
	public int height;
	
	public PaperSize(String name, int width, int height) {
		this.name = name;
		this.width = width;
		this.height = height;
	}
	
	public String toString() {
		return name+" ("+width+" x "+height+")";
	}
}