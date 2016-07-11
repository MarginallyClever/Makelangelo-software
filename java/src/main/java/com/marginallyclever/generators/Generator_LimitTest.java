/*
 * https://en.wikipedia.org/wiki/Maze_generation_algorithm#Recursive_backtracker
 */
package com.marginallyclever.generators;

import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangelo.Translator;

/**
 * generate gcode to move the pen from the home position, around the edge of the paper, and back to home.
 * @author Dan Royer
 *
 */
public class Generator_LimitTest extends ImageGenerator {
	protected float xmax, xmin, ymax, ymin;

	@Override
	public String getName() {
		return Translator.get("LimitTestName");
	}

	@Override
	public boolean generate(Writer out) throws IOException {
		imageStart(out);
		tool = machine.getCurrentTool();
		liftPen(out);
		tool.writeChangeTo(out);

		ymin = (float)machine.getPaperBottom() * 10;
		ymax = (float)machine.getPaperTop()    * 10;
		xmin = (float)machine.getPaperLeft()   * 10;
		xmax = (float)machine.getPaperRight()  * 10;
		
		// Draw outside edge
		moveTo(out, xmin, ymin,true);
		lowerPen(out);
		moveTo(out, xmin, ymin,false);
		moveTo(out, xmin, ymax,false);
		moveTo(out, xmax, ymax,false);
		moveTo(out, xmax, ymin,false);
		moveTo(out, xmin, ymin,false);
		liftPen(out);

	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	    
		return true;
	}

}
