package com.marginallyclever.makelangelo.turtle;

import java.awt.Color;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.StringHelper;

public class TurtleMove {
	public static final int TRAVEL=0;  // move without drawing
	public static final int DRAW_LINE=1;  // move while drawing
	public static final int TOOL_CHANGE=2;
	
	public int type;
	public double x,y;  // destination
	
	public TurtleMove(double x0,double y0,int type0) {
		super();
		this.x=x0;
		this.y=y0;
		this.type=type0;
	}
	
	public TurtleMove(TurtleMove m) {
		this(m.x,m.y,m.type);
	}

	public ColorRGB getColor() {
		return new ColorRGB((int)x);
	}
	
	public double getDiameter() {
		return y;
	}
	
	public String toString() {
		switch(type) {
		case TOOL_CHANGE:
			Color c = new Color((int)x);
			return "TOOL R"+c.getRed()+" G"+c.getGreen()+" B"+c.getBlue()+" D"+StringHelper.formatDouble(y);
		case TRAVEL:
			return "TRAVEL X"+StringHelper.formatDouble(x)+" Y"+StringHelper.formatDouble(y);
		default:
			return "DRAW_LINE X"+StringHelper.formatDouble(x)+" Y"+StringHelper.formatDouble(y);
		}
	}
}