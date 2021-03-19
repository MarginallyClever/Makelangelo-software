package com.marginallyclever.makelangelo.pen;

import java.util.prefs.Preferences;

import com.marginallyclever.core.ColorRGB;

public class Pen {
	private ColorRGB downColorDefault;
	private ColorRGB downColor;
	private ColorRGB upColor;
	private double diameter;
	
	public final static PenColor commonPenColors [] = {
		new PenColor(0x000000,"black"),
		new PenColor(0xff0000,"red"),
		new PenColor(0x00ff00,"green"),
		new PenColor(0x0000ff,"blue"),
		new PenColor(0x00ffff,"cyan"),
		new PenColor(0xff00ff,"magenta"),
		new PenColor(0xffff00,"yellow"),
		new PenColor(0xffffff,"white"),
	};


	public Pen() {
		downColor = downColorDefault = new ColorRGB(0,0,0); // black
		upColor = new ColorRGB(0,255,0);  // blue
		diameter = 0.8;  // mm
	}


	public void loadPenConfig(Preferences prefs) {
		prefs = prefs.node("Pen");

		setDiameter(Double.parseDouble(prefs.get("diameter", Double.toString(getDiameter()))));
		
		int r,g,b;
		r = prefs.getInt("penDownColorR", downColor.getRed());
		g = prefs.getInt("penDownColorG", downColor.getGreen());
		b = prefs.getInt("penDownColorB", downColor.getBlue());
		downColor = downColorDefault = new ColorRGB(r,g,b);
		r = prefs.getInt("penUpColorR", upColor.getRed());
		g = prefs.getInt("penUpColorG", upColor.getGreen());
		b = prefs.getInt("penUpColorB", upColor.getBlue());
		upColor = new ColorRGB(r,g,b);
	}

	public void savePenConfig(Preferences prefs) {
		prefs = prefs.node("Pen");
		prefs.put("diameter", Double.toString(getDiameter()));
		prefs.putInt("penDownColorR", downColorDefault.getRed());
		prefs.putInt("penDownColorG", downColorDefault.getGreen());
		prefs.putInt("penDownColorB", downColorDefault.getBlue());
		prefs.putInt("penUpColorR", upColor.getRed());
		prefs.putInt("penUpColorG", upColor.getGreen());
		prefs.putInt("penUpColorB", upColor.getBlue());
	}
	
	public void setDownColorDefault(ColorRGB arg0) {
		downColorDefault=arg0;
	}
	
	public ColorRGB getPenDownColorDefault() {
		return downColorDefault;
	}
	
	public ColorRGB getDownColor() {
		return downColor;
	}
	
	public void setDownColor(ColorRGB arg0) {
		downColor=arg0;
	}
	
	public void setUpColor(ColorRGB arg0) {
		upColor=arg0;
	}
	
	public ColorRGB getUpColor() {
		return upColor;
	}

	public void setDiameter(double d) {
		diameter = d;
	}
	
	public double getDiameter() {
		return diameter;
	}
}
