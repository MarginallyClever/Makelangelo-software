package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RemoveExtraColorChangesFromTurtle} removes any color changes that are not needed.
 */
public class RemoveExtraColorChangesFromTurtle {
	private static final Logger logger = LoggerFactory.getLogger(RemoveExtraColorChangesFromTurtle.class);
	
	public static void run(Turtle turtle) {
		logger.debug("start @ {}", turtle.strokeLayers.size());

		// if adjacent StrokeLayer has the same color, remove the color change by merging the two layers.
		for( int i=0; i<turtle.strokeLayers.size()-1; i++) {
			if(turtle.strokeLayers.get(i).getColor().equals(turtle.strokeLayers.get(i+1).getColor())) {
				turtle.strokeLayers.get(i).addAll(turtle.strokeLayers.get(i+1));
				turtle.strokeLayers.remove(i+1);
				i--;
			}
		}

		logger.debug("end @ {}", turtle.strokeLayers.size());
	}
}
