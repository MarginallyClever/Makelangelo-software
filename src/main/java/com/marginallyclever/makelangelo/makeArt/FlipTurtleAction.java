package com.marginallyclever.makelangelo.makeArt;

import com.marginallyclever.makelangelo.turtle.Turtle;

public class FlipTurtleAction extends TurtleModifierAction {
	private static final long serialVersionUID = 3429861670595048893L;
	private double scaleX, scaleY;
	
	public FlipTurtleAction(double x,double y,String name) {
		super(name);
		scaleX=x;
		scaleY=y;
	}
	
	@Override
	public Turtle run(Turtle turtle) {
		Turtle out = new Turtle(turtle);
		out.scale(scaleX, scaleY);
		return out;
	}

}
