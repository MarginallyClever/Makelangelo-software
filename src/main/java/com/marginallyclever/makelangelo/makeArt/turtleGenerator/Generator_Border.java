package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.turtle.Turtle;

/**
 * Draws a border around the paper.  Uses current paper myPaper.
 * @author Dan Royer
 *
 */
public class Generator_Border extends TurtleGenerator {
	
	@Override
	public String getName() {
		return Translator.get("BorderName");
	}

	@Override
	public TurtleGeneratorPanel getPanel() {
		return new Generator_Empty_Panel(this);
	}

	@Override
	public void generate() {
		double yMin = myPaper.getMarginBottom();
		double yMax = myPaper.getMarginTop();
		double xMin = myPaper.getMarginLeft();
		double xMax = myPaper.getMarginRight();

		Turtle turtle = new Turtle();
		turtle.penUp();
		turtle.moveTo(xMin,yMax);
		turtle.penDown();
		turtle.moveTo(xMin,yMax);
		turtle.moveTo(xMax,yMax);
		turtle.moveTo(xMax,yMin);
		turtle.moveTo(xMin,yMin);
		turtle.moveTo(xMin,yMax);
		
		notifyListeners(turtle);
	}
}
