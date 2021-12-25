package com.marginallyclever.makelangelo.makeArt;

import com.marginallyclever.convenience.Clipper2D;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.Point2D;
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

		List<TurtleMove> oldHistory = turtle.history;
		turtle.history = new ArrayList<TurtleMove>();
		
		// limits we will need for rectangle
		Point2D rMax = new Point2D(rectangle.getMaxX(),rectangle.getMaxY());
		Point2D rMin = new Point2D(rectangle.getMinX(),rectangle.getMinY());
		// working space for clipping
		Point2D P0 = new Point2D(); 
		Point2D P1 = new Point2D(); 
		
		TurtleMove prev=null;
		
		for( TurtleMove m : oldHistory ) {
			switch(m.type) {
			case TurtleMove.DRAW:
			case TurtleMove.TRAVEL:
				if(prev!=null) {
					P0.set(prev.x, prev.y);
					P1.set(m.x, m.y);
					boolean result = Clipper2D.clipLineToRectangle(P0,P1,rMax,rMin);
					// !result means full crop, do nothing.
					if(result) {
						// partial crop.  Which end(s)?
						boolean startCropped=MathHelper.lengthSquared(P0.x-prev.x, P0.y-prev.y)>1e-8;
						boolean   endCropped=MathHelper.lengthSquared(P1.x-   m.x, P1.y-   m.y)>1e-8;
						
						if(startCropped && endCropped) {
							// crosses rectangle, both ends out.
							turtle.history.add(new TurtleMove(P0.x,P0.y,TurtleMove.TRAVEL));
							turtle.history.add(m);
							TurtleMove m2=new TurtleMove(P1.x,P1.y,m.type);
							turtle.history.add(m2);
						} else if(!startCropped && !endCropped) {
							turtle.history.add(m);
						} else if(endCropped) {
							// end cropped, leaving the rectangle
							TurtleMove m2=new TurtleMove(P1.x,P1.y,m.type);
							turtle.history.add(m2);
						} else {
							// start cropped, coming back into rectangle
							turtle.history.add(new TurtleMove(P0.x,P0.y,TurtleMove.TRAVEL));
							turtle.history.add(m);
						}
					}
				}
				prev=m;
				
				break;
			default:
				turtle.history.add(m);
				break;
			}
		}
		
		// There may be some dumb travel moves left. (several travels in a row.)
	
		logger.debug("crop end @ {}", turtle.history.size());

	}
}
