package com.marginallyclever.makelangelo.turtle;

import com.marginallyclever.convenience.helpers.StringHelper;

import java.awt.*;
import java.util.Objects;

@Deprecated(since="7.70.0", forRemoval=true)
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

	public void setColor(Color c) {
		this.x = c.hashCode();
	}

	public Color getColor() {
		return new Color((int)x,true);
	}
	
	public double getDiameter() {
		return y;
	}

	public void setDiameter(double d) {
		y=d;
	}

	public String toString() {
        return switch (type) {
            case TOOL_CHANGE -> {
                Color c = new Color((int) x, true);
                yield "TOOL"
						+ " R" + c.getRed()
						+ " G" + c.getGreen()
						+ " B" + c.getBlue()
						+ " A" + c.getAlpha()
						+ " D" + StringHelper.formatDouble(y);
            }
            case TRAVEL -> "TRAVEL"
					+ " X" + StringHelper.formatDouble(x)
					+ " Y" + StringHelper.formatDouble(y);
            default -> "DRAW_LINE"
					+ " X" + StringHelper.formatDouble(x)
					+ " Y" + StringHelper.formatDouble(y);
        };
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