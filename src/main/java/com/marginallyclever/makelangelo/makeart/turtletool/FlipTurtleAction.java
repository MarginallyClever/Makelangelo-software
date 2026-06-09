package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.turtle.Turtle;

public class FlipTurtleAction extends TurtleTool {
	private final double scaleX,scaleY;
	
	public FlipTurtleAction(String name,double x,double y) {
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
