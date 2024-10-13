package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RemoveExtraColorChangesFromTurtle} removes any color changes that are not needed.
 */
public class RemoveExtraColorChangesFromTurtle {
	private static final Logger logger = LoggerFactory.getLogger(RemoveExtraColorChangesFromTurtle.class);
	
	public static void run(Turtle turtle) {
		logger.debug("start @ {}", turtle.history.size());


		List<TurtleMove> newHistory = new ArrayList<>();
		TurtleMove last=null;
		for(TurtleMove move : turtle.history) {
			if(move.type != MovementType.TOOL_CHANGE) {
				newHistory.add(move);
			} else if(last==null || !last.getColor().equals(move.getColor())) {
				last = move;
				newHistory.add(move);
			}
		}
		turtle.history.clear();
		turtle.history.addAll(newHistory);
		
		// There may be some dumb travel moves left. (several travels in a row.)
	
		logger.debug("end @ {}", turtle.history.size());
	}
}
