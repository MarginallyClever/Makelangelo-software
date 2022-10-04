package com.marginallyclever.makelangelo.makeart.tools;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

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
