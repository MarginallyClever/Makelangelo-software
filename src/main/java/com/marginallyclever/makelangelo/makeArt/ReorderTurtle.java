package com.marginallyclever.makelangelo.makeArt;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * {@link ReorderTurtle} tries to reorder the line segments of a {@link Turtle}'s path such that the
 * the new path will take less time to draw.  
 * First it attempts to remove any duplicate line segments.
 * Second it runs a "greedy tour" which does a pretty good job of sorting by draw-first, travel-second behavior. 
 * @author Dan Royer
 *
 */
public class ReorderTurtle extends AbstractAction {

	private static final Logger logger = LoggerFactory.getLogger(ReorderTurtle.class);
	
	private static final long serialVersionUID = 3473530693924971574L;
	private Makelangelo myMakelangelo;
	
	public ReorderTurtle(Makelangelo m) {
		super(Translator.get("Reorder"));
		myMakelangelo = m;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		myMakelangelo.setTurtle(run(myMakelangelo.getTurtle()));
	}
	
	public static Turtle run(Turtle turtle) {
		if(turtle.history.size()==0) return turtle;
		
		logger.debug("reorder() start @ {} instructions.", turtle.history.size());
		
		Turtle output = new Turtle();
		output.history.clear();
		
		// history is made of changes, travels, and draws
		ArrayList<Turtle> colors = turtle.splitByToolChange();
		logger.debug("reorder() layers: {}", colors.size());
		for( Turtle t2 : colors ) {
			output.add(reorderTurtle(t2));
		}
		
		logger.debug("reorder() end @ {} instructions.", output.history.size());
		return output;
	}

	/**
	 * Reorder drawing moves to minimize travel moves.
	 * look at all pen down moves.
	 * if two pen down moves share a start/end, then they are connected in sequence.
	 * 
	 * @param turtle
	 */
	private static Turtle reorderTurtle(Turtle turtle) {
		ArrayList<LineSegment2D> originalLines = turtle.getAsLineSegments();
		int originalCount = originalLines.size();
		ColorRGB c = turtle.getFirstColor();
		logger.debug("  {} converted to {} lines.", c.toString(), originalCount);

		ArrayList<LineSegment2D> uniqueLines = removeDuplicates(originalLines,1e-4);
		int uniqueCount = uniqueLines.size();
		int duplicateCount = originalCount - uniqueCount;
		logger.debug("  - {} duplicates = {} lines.", duplicateCount, uniqueCount);

		ArrayList<LineSegment2D> orderedLines = greedyReordering(uniqueLines);
		Turtle t = new Turtle(c);
		t.addLineSegments(orderedLines, 1.0);
		return t;
	}

	/**
	 * From the pool of uniqueLines, take one and make it the head.
	 * looking for the nearest available segment that begins where the head ends.
	 * The segment found is removed from the available pool and becomes the new head.  Repeat until the avilable pool is empty.
	 * @param uniqueLines the unsorted list.
	 * @return the sorted list.
	 */
	private static ArrayList<LineSegment2D> greedyReordering(ArrayList<LineSegment2D> uniqueLines) {
		logger.debug("  greedyReordering()");
		ArrayList<LineSegment2D> orderedLines = new ArrayList<LineSegment2D>();
		if(uniqueLines.isEmpty()) return orderedLines;

		Point2D lastPosition = uniqueLines.get(0).a;
		
		while(!uniqueLines.isEmpty()) {
			double bestD = Double.MAX_VALUE;
			LineSegment2D bestLine = null;
			boolean bestFlip = false;
			
			for( LineSegment2D line : uniqueLines ) {
				// is either end of line closer than our best?
				double dA = lastPosition.distanceSquared(line.a);
				double dB = lastPosition.distanceSquared(line.b);
				double nearest = Math.min(dA, dB);
				if(bestD > nearest) {
					bestD = nearest;
					bestLine = line;
					bestFlip = (dB < dA);
				}
				if(bestD==0) break;
			}
			
			if(bestFlip) bestLine.flip();
			
			uniqueLines.remove(bestLine);
			orderedLines.add(bestLine);
			
			// Start next iteration where current line ends.
			lastPosition = bestLine.b;
		}
		
		return orderedLines;
	}

	private static ArrayList<LineSegment2D> removeDuplicates(ArrayList<LineSegment2D> originalLines, double EPSILON2) {
		logger.debug("  removeDuplicates()");
		ArrayList<LineSegment2D> uniqueLines = new ArrayList<LineSegment2D>();

		for(LineSegment2D candidateLine : originalLines) {
			boolean isDuplicate = false;
			
			// Compare this line to all the lines previously marked as non-duplicate
			for( LineSegment2D uniqueLine : uniqueLines ) {
				// Check if lines are colinear
				if( uniqueLine.ptLineDistSq(candidateLine.a) < EPSILON2 &&
					uniqueLine.ptLineDistSq(candidateLine.b) < EPSILON2 ) {
					// they are!
					// if they touch or overlap then I have a candidate.
					// measure where the points are relative to each other.
					boolean candidateStartsCloseToUnique = uniqueLine.ptSegDistSq(candidateLine.a) < EPSILON2;
					boolean candidateEndsCloseToUnique = uniqueLine.ptSegDistSq(candidateLine.b) < EPSILON2;
					
					if(candidateStartsCloseToUnique) {
						if(candidateEndsCloseToUnique) {
							// Candidate doesn't add anything which isn't already covered by the unique line.
							// No further action needed.
						} else {
							// Partial overlap, extend uniqueLine
							extendLine(uniqueLine, candidateLine.b);
						}
						isDuplicate = true;
						break;
					} else if(candidateEndsCloseToUnique) {
						// Partial overlap, extend uniqueLine
						extendLine(uniqueLine, candidateLine.a);
						isDuplicate = true;
						break;						
					} else {
						// No match, check remaining lines for duplicates
						continue;
					}
				}
			}
			
			if(!isDuplicate) {
				// candidateLine does not match any line in the list.
				uniqueLines.add(candidateLine);					
			}
		}

		return uniqueLines;
	}

	// assumes extPoint is a point which lies on the infinite extension of targetLine
 	private static void extendLine(LineSegment2D targetLine, Point2D extPoint) {
		double newLengthA = targetLine.a.distanceSquared(extPoint);
		double newLengthB = targetLine.b.distanceSquared(extPoint);
		double currentLength = targetLine.lengthSquared();
		
		// Maximize length of target line by replacing the start or end point with the extPoint		
		if(newLengthA > currentLength && newLengthA > newLengthB) {
			// Draw line from targetLine.a to extPoint
			targetLine.b = extPoint;
		} else if(newLengthB > currentLength) {
			// Draw line from extPoint to targetLine.b 
			targetLine.a = extPoint;
		}
	}
}
