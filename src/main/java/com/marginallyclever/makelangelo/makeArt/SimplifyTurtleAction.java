package com.marginallyclever.makelangelo.makeArt;

import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.LineCollection;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Performs Douglas-Peucker line simplification.
 * see https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
 * @author Dan Royer
 * @since 7.31.0
 */
public class SimplifyTurtleAction extends TurtleModifierAction {
	private static final long serialVersionUID = 7013596037448318526L;
	private static final Logger logger = LoggerFactory.getLogger(SimplifyTurtleAction.class);
	private static double distanceTolerance = 1.6;
	
	public SimplifyTurtleAction() {
		super(Translator.get("Simplify"));
	}
	
	@Override
	public Turtle run(Turtle turtle) {
		int os = turtle.history.size();
		logger.debug("begin @ {}", os);
		
		LineCollection originalLines = new LineCollection(turtle.getAsLineSegments());
		int originalCount = originalLines.size();
		logger.debug("  Converted to {} lines.", originalCount);

		ArrayList<LineSegment2D> longLines = removeColinearSegments(originalLines); 
		int longCount = longLines.size();
		int shortCount = originalCount - longCount;
		logger.debug("  - {} shorts = {} lines.", shortCount, longCount);

		Turtle t = new Turtle();
		t.addLineSegments(longLines);
		int ns = t.history.size();
		logger.debug("end @ {}", ns);
		
		return t;
	}

	/**
	 * Split the collection by color, then by travel moves to get contiguous blocks in a single color.
	 * simplify these blocks using Douglas-Peucker method. 
	 * @param originalLines
	 * @return
	 */
	private ArrayList<LineSegment2D> removeColinearSegments(LineCollection originalLines) {
		LineCollection result = new LineCollection();
		
		ArrayList<LineCollection> byColor = originalLines.splitByColor();
		for(LineCollection c : byColor ) {
			ArrayList<LineCollection> byTravel = c.splitByTravel();
			for(LineCollection t : byTravel ) {
				LineCollection after = t.simplify(distanceTolerance);
				result.addAll(after);
			}
		}
		
		return result;
	}

	/**
	 * Sets the distance tolerance for the simplification. All vertices in the
	 * simplified line will be within this distance of the original line.
	 *
	 * @param distanceTolerance the approximation tolerance to use
	 */
	public void setDistanceTolerance(double distanceTolerance) {
		SimplifyTurtleAction.distanceTolerance = distanceTolerance;
	}
}
