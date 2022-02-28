package com.marginallyclever.makelangelo.turtle;

import java.awt.Color;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.StringHelper;

public class TurtleMove {

	public MovementType type;
	public double x,y;  // destination
	
	public TurtleMove(double destinationX, double destinationY, MovementType movementType) {
		super();
		this.x=destinationX;
		this.y=destinationY;
		this.type=movementType;
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