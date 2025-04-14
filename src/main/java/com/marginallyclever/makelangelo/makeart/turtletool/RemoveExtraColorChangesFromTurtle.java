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
		var allLayers = turtle.getLayers();
		logger.debug("start @ {}", allLayers.size());

		// if adjacent StrokeLayer has the same color, remove the color change by merging the two layers.
		for( int i=0; i<allLayers.size()-1; i++) {
			if(allLayers.get(i).getColor().equals(allLayers.get(i+1).getColor())) {
				allLayers.get(i).addAll(allLayers.get(i+1));
				allLayers.remove(i+1);
				i--;
			}
		}

		logger.debug("end @ {}", allLayers.size());
	}
}
