package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.convenience.Clipper2D;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class CropTurtle {
	private static final Logger logger = LoggerFactory.getLogger(CropTurtle.class);
	
	public static void run(Turtle turtle,Rectangle2D.Double rectangle) {
		logger.debug("crop start @ {}", turtle.history.size());

		List<TurtleMove> newHistory = new ArrayList<>();
		// limits we will need for rectangle
		Point2D rMax = new Point2D(rectangle.getMaxX(),rectangle.getMaxY());
		Point2D rMin = new Point2D(rectangle.getMinX(),rectangle.getMinY());
		// working space for clipping
		Point2D p0 = new Point2D();
		Point2D p1 = new Point2D();
		Point2D p0before = new Point2D();
		Point2D p1before = new Point2D();

		TurtleMove prev=null;
		
		for (TurtleMove m : turtle.history ) {
			switch (m.type) {
				case DRAW_LINE, TRAVEL -> {
					if(prev==null) {
						newHistory.add(m);
					} else {
						p0.set(prev.x, prev.y);
						p1.set(m.x, m.y);
						p0before.set(p0);
						p1before.set(p1);
						if (Clipper2D.clipLineToRectangle(p0, p1, rMax, rMin)) {
							// partial crop.  Which end(s)?
							boolean startCropped = !p0before.equalsEpsilon(p0, 1e-8);
							boolean endCropped = !p1before.equalsEpsilon(p1, 1e-8);

							if (startCropped) {
								// make a jump to the crop start
								newHistory.add(new TurtleMove(p0.x, p0.y, MovementType.TRAVEL));
							}

							if(endCropped) {
								// draw to the crop end
								newHistory.add(new TurtleMove(p1.x, p1.y, m.type));
							} else {
								// draw to the original end
								newHistory.add(m);
							}
						}
					}
					prev = m;
				}
				default -> newHistory.add(m);
			}
		}

		turtle.history.clear();
		turtle.history.addAll(newHistory);
		
		// There may be some dumb travel moves left. (several travels in a row.)
	
		logger.debug("crop end @ {}", turtle.history.size());
	}
}
