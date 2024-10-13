package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.turtle.Turtle;

public class FlipTurtleAction extends TurtleTool {
	private final double scaleX,scaleY;
	
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
