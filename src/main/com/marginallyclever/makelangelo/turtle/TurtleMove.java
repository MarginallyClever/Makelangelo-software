package com.marginallyclever.makelangelo.turtle;

import java.awt.Color;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.StringHelper;

public class TurtleMove {
	public static final int TRAVEL=0;  // move without drawing
	public static final int DRAW=1;  // move while drawing
	public static final int TOOL_CHANGE=2;
	
	public int type;
	public double x,y;  // destination
	
	public TurtleMove(double x0,double y0,int type0) {
		super();
		x=x0;
		y=y0;
		type=type0;
	}
	
	public TurtleMove(TurtleMove m) {
		super();
		x=m.x;
		y=m.y;
		type=m.type;
	}

	public ColorRGB getColor() {
		return new ColorRGB((int)x);
	}
	
	public String toString() {
		switch(type) {
		case TOOL_CHANGE:
			Color c = new Color((int)x);
			return "COLOR R"+c.getRed()+" G"+c.getGreen()+" B"+c.getBlue();
		case TRAVEL:
			return "TRAVEL X"+StringHelper.formatDouble(x)+" Y"+StringHelper.formatDouble(y);
		default:
			return "DRAW X"+StringHelper.formatDouble(x)+" Y"+StringHelper.formatDouble(y);
		}
	}
}