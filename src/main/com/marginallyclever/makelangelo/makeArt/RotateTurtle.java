package com.marginallyclever.makelangelo.makeArt;

import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.convenience.turtle.TurtleMove;

public class RotateTurtle {
	public static void run(Turtle turtle,double degrees) {
		double r=Math.toRadians(degrees);
		double c=Math.cos(r);
		double s=Math.sin(r);

		for( TurtleMove m : turtle.history ) {
			double xn=m.x*c - m.y*s;
			double yn=m.x*s + m.y*c;
			m.x=xn;
			m.y=yn;
		}
	}
}
