package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ReorderTurtleAction} tries to reorder the line segments of a {@link Turtle}'s path such that the
 * the new path will take less time to draw.  
 * First it attempts to remove any duplicate line segments.
 * Second it runs a "greedy tour" which does a pretty good job of sorting by draw-first, travel-second behavior. 
 * @author Dan Royer
 *
 */
public class ReorderTurtleAction extends TurtleTool {
	private static final Logger logger = LoggerFactory.getLogger(ReorderTurtleAction.class);
	
	public ReorderTurtleAction() {
		super(Translator.get("Reorder"));
	}
	
	public Turtle run(Turtle input) {
		if(input.getLayers().isEmpty()) return input;
		
		logger.debug("reorder() start @ {} loops.", input.countLoops());

		var output = (new ReorderHelper()).splitAndReorderTurtle(input);
		logger.debug("reorder() end @ {} loops.", output.countLoops());
		return output;
	}
}
