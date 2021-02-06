package com.marginallyclever.artPipeline.generators;

import com.marginallyclever.artPipeline.TurtleNode;
import com.marginallyclever.artPipeline.TurtleNodePanel;
import com.marginallyclever.artPipeline.generators.panels.Generator_Empty_Panel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Draws a border around the paper.  Uses current paper settings.
 * @author Dan Royer
 *
 */
public class Generator_Border extends TurtleNode {
	double width=100;
	double height=100;
	
	@Override
	public String getName() {
		return Translator.get("BorderName");
	}

	@Override
	public TurtleNodePanel getPanel() {
		return new Generator_Empty_Panel(this);
	}

	@Override
	public boolean iterate() {
		setTurtleResult(null);
		
		Turtle turtle = new Turtle();
		
		double yMin = -height/2;
		double yMax =  height/2;
		double xMin = -width/2;
		double xMax =  width/2;

		turtle.reset();
		turtle.penUp();
		turtle.moveTo(xMin,yMax);
		turtle.penDown();
		turtle.moveTo(xMin,yMax);
		turtle.moveTo(xMax,yMax);
		turtle.moveTo(xMax,yMin);
		turtle.moveTo(xMin,yMin);
		turtle.moveTo(xMin,yMax);
		turtle.penUp();
		
		setTurtleResult(turtle);
		
	    return false;
	}
}
