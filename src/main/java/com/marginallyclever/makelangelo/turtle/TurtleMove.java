package com.marginallyclever.makelangelo.turtle;

import com.marginallyClever.convenience.ColorRGB;
import com.marginallyClever.convenience.StringHelper;

import java.awt.*;
import java.util.Objects;

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

	public void setColor(ColorRGB c) {
		this.x = c.toInt();
	}

	public ColorRGB getColor() {
		return new ColorRGB((int)x);
	}
	
	public double getDiameter() {
		return y;
	}

	public void setDiameter(double d) {
		y=d;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TurtleMove that = (TurtleMove) o;
		return type == that.type && Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, x, y);
	}
}