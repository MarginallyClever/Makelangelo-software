package com.marginallyclever.makelangelo.robot;

public class PaperSize {
	public String name;
	public int width;
	public int height;
	
	PaperSize(String name,int width,int height) {
		this.name = name;
		this.width = width;
		this.height = height;
	}
	
	public String toString() {
		return name+" ("+width+" x "+height+")";
	}
}