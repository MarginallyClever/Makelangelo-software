package com.marginallyclever.artPipeline.generators;

import java.io.IOException;

import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Draws a border around the paper.  Uses current paper settings.
 * @author Dan Royer
 *
 */
public class Generator_Border extends ImageGenerator {
	
	@Override
	public String getName() {
		return Translator.get("BorderName");
	}

	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_Empty_Panel(this);
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	@Override
	public boolean generate() {
		float yMin = (float)machine.getMarginBottom();
		float yMax = (float)machine.getMarginTop();
		float xMin = (float)machine.getMarginLeft();
		float xMax = (float)machine.getMarginRight();

		turtle = new Turtle();
		turtle.penUp();
		turtle.moveTo(xMin,yMax);
		turtle.penDown();
		turtle.moveTo(xMin,yMax);
		turtle.moveTo(xMax,yMax);
		turtle.moveTo(xMax,yMin);
		turtle.moveTo(xMin,yMin);
		turtle.moveTo(xMin,yMax);
		
	    return true;
	}
}
