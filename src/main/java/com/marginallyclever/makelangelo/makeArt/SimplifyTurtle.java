package com.marginallyclever.makelangelo.makeArt;

import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class SimplifyTurtle extends AbstractAction {
	private static final Logger logger = LoggerFactory.getLogger(SimplifyTurtle.class);
	private static final long serialVersionUID = 2930297421274921735L;
	private Makelangelo myMakelangelo;
	
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
		logger.debug("SimplifyTurtle begin @ {}", os);
		
		ArrayList<LineSegment2D> originalLines = turtle.getAsLineSegments();
		int originalCount = originalLines.size();
		logger.debug("  Converted to {} lines.", originalCount);

		double minimumStepSize = 1e-3;
		ArrayList<LineSegment2D> longLines = removeVeryShortSegments(originalLines,minimumStepSize); 
		int longCount = longLines.size();
		int shortCount = originalCount - longCount;
		logger.debug("  - {} shorts = {} lines.", shortCount, longCount);

		Turtle t = new Turtle();
		t.addLineSegments(longLines,1.0);
		int ns = t.history.size();
		logger.debug("SimplifyTurtle end @ {}", ns);
		
		return t;
	}

	private static ArrayList<LineSegment2D> removeVeryShortSegments(ArrayList<LineSegment2D> toTest, double minimumLength) {
		ArrayList<LineSegment2D> toKeep = new ArrayList<LineSegment2D>();
		int count = toTest.size();
		if(count==0) return toKeep;
		
		toKeep.add(toTest.get(0));
		LineSegment2D first = toKeep.get(0); 

		for(int i=1;i<count;++i) {
			LineSegment2D second = toTest.get(i);
			// sequential with no jump?
			if(first.b.distanceSquared(second.a)<minimumLength) {
				// very short?
				if(second.a.distanceSquared(second.b)<minimumLength) {
					first.b=second.b;
					continue;
				}

				// colinear?
				Vector2d firstN = makeUnitVectorFromLineSegment(first);
				Vector2d secondN = makeUnitVectorFromLineSegment(second);
				if(firstN.dot(secondN)>0.9999) {
					first.b=second.b;
					continue;
				}
				second.a=first.b;
			}
			
			toKeep.add(second);
			first = second;
		}
		return toKeep;
	}

	private static Vector2d makeUnitVectorFromLineSegment(LineSegment2D line) {
		Vector2d n = new Vector2d();
		n.x = line.b.x - line.a.x;
		n.y = line.b.y - line.a.y;
		n.normalize();
		return n;
	}
}
