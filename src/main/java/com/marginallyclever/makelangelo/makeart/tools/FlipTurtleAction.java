package com.marginallyclever.makelangelo.makeart.tools;

import com.marginallyclever.makelangelo.makeart.TurtleModifierAction;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.io.Serial;

public class FlipTurtleAction extends TurtleModifierAction {
	@Serial
	private static final long serialVersionUID = 3429861670595048893L;
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
