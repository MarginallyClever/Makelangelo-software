/*
 * https://en.wikipedia.org/wiki/Maze_generation_algorithm#Recursive_backtracker
 */
package com.marginallyclever.generators;

import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangelo.Translator;

public class Generator_LimitTest extends ImageGenerator {
	protected float xmax, xmin, ymax, ymin;

	@Override
	public String getName() {
		return Translator.get("LimitTestName");
	}

	@Override
	public boolean generate(Writer out) throws IOException {
		imageStart(out);
		liftPen(out);

		ymin = (float)machine.getPaperBottom() * 10;
		ymax = (float)machine.getPaperTop()    * 10;
		xmin = (float)machine.getPaperLeft()   * 10;
		xmax = (float)machine.getPaperRight()  * 10;
		
		// Draw outside edge
		tool.writeMoveTo(out, (float) xmin, (float) ymin);
		lowerPen(out);
		tool.writeMoveTo(out, (float) xmin, (float) ymin);
		tool.writeMoveTo(out, (float) xmin, (float) ymax);
		tool.writeMoveTo(out, (float) xmax, (float) ymax);
		tool.writeMoveTo(out, (float) xmax, (float) ymin);
		tool.writeMoveTo(out, (float) xmin, (float) ymin);
		liftPen(out);

		ymin = (float)machine.getPaperBottom() * (float)machine.getPaperMargin() * 10;
		ymax = (float)machine.getPaperTop()    * (float)machine.getPaperMargin() * 10;
		xmin = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin() * 10;
		xmax = (float)machine.getPaperRight()  * (float)machine.getPaperMargin() * 10;
		
		// Draw outside edge
		tool.writeMoveTo(out, (float) xmin, (float) ymin);
		lowerPen(out);
		tool.writeMoveTo(out, (float) xmin, (float) ymin);
		tool.writeMoveTo(out, (float) xmin, (float) ymax);
		tool.writeMoveTo(out, (float) xmax, (float) ymax);
		tool.writeMoveTo(out, (float) xmax, (float) ymin);
		tool.writeMoveTo(out, (float) xmin, (float) ymin);
	    liftPen(out);

	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	    
		return true;
	}

}
