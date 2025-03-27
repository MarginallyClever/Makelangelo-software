package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.convenience.linecollection.LineCollection;
import com.marginallyclever.convenience.linecollection.RamerDouglasPeuckerRecursive;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs <a href="https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Douglas-Peucker
 * line simplification</a>.
 * @author Dan Royer
 * @since 7.31.0
 */
public class SimplifyTurtleAction extends TurtleTool {
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
		int beforeCount = originalLines.size();
		logger.debug("  Converted to {} lines.", beforeCount);

		LineCollection longLines = removeColinearSegments(originalLines);
		int afterCount = longLines.size();
		int change = beforeCount - afterCount;
		logger.debug("  - {} shorts = {} lines.", change, afterCount);
		if(change<=0) {
			return turtle;
		}
		Turtle t = new Turtle();
		t.addLineSegments(longLines);
		int ns = t.history.size();
		logger.debug("end @ {}", ns);
		return t;
	}

	/**
	 * Split the collection by color, then by travel moves to get contiguous blocks in a single color.
	 * simplify these blocks using Douglas-Peucker method. 
	 * @param originalLines the lines to simplify
	 * @return the simplified lines
	 */
	private LineCollection removeColinearSegments(LineCollection originalLines) {
		LineCollection result = new LineCollection();

		var byColor = originalLines.splitByColor();
		for(LineCollection c : byColor ) {
			var byTravel = c.splitByTravel();
			for(LineCollection t : byTravel ) {
				LineCollection after = (new RamerDouglasPeuckerRecursive(t)).simplify(distanceTolerance);
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
