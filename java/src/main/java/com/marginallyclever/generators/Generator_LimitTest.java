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

		double ymin = machine.getPaperBottom() * 10;
		double ymax = machine.getPaperTop()    * 10;
		double xmin = machine.getPaperLeft()   * 10;
		double xmax = machine.getPaperRight()  * 10;
		
		// Draw outside edge
		tool.writeMoveTo(out, xmin, ymax);
		lowerPen(out);
		tool.writeMoveTo(out, xmax, ymax);
		tool.writeMoveTo(out, xmax, ymin);
		tool.writeMoveTo(out, xmin, ymin);
		tool.writeMoveTo(out, xmin, ymax);
		liftPen(out);

		tool.writeMoveTo(out, machine.getHomeX(), machine.getHomeY());
	    
		return true;
	}

}
