package com.marginallyclever.makelangelo.makeArt;

import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.LineCollection;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Performs Douglas-Peucker line simplification.
 * see https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
 * @author Dan Royer
 *
 */
public class SimplifyTurtle extends AbstractAction {
	private static final long serialVersionUID = 7013596037448318526L;
	private static final Logger logger = LoggerFactory.getLogger(SimplifyTurtle.class);
	private Makelangelo myMakelangelo;

	private static double distanceTolerance = 1.6;
	
	public SimplifyTurtle(Makelangelo m) {
		super(Translator.get("Simplify"));
		myMakelangelo=m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		myMakelangelo.setTurtle(run(myMakelangelo.getTurtle()));
	}
	
	public static Turtle run(Turtle turtle) {
		int os = turtle.history.size();
		logger.debug("begin @ {}", os);
		
		LineCollection originalLines = new LineCollection(turtle.getAsLineSegments());
		int originalCount = originalLines.size();
		logger.debug("  Converted to {} lines.", originalCount);

		ArrayList<LineSegment2D> longLines = removeVeryShortSegments(originalLines); 
		int longCount = longLines.size();
		int shortCount = originalCount - longCount;
		logger.debug("  - {} shorts = {} lines.", shortCount, longCount);

		Turtle t = new Turtle();
		t.addLineSegments(longLines,1.0);
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
	private static ArrayList<LineSegment2D> removeVeryShortSegments(LineCollection originalLines) {
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
	public static void setDistanceTolerance(double distanceTolerance) {
		SimplifyTurtle.distanceTolerance = distanceTolerance;
	}
}
