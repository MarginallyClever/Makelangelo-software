package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Draws a border around the paper.  Uses current paper settings.
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
		float yMin = (float)settings.getMarginBottom();
		float yMax = (float)settings.getMarginTop();
		float xMin = (float)settings.getMarginLeft();
		float xMax = (float)settings.getMarginRight();

		turtle = new Turtle();
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
