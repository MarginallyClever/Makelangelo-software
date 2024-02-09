package com.marginallyclever.makelangelo.makeart.tools;

import com.marginallyclever.convenience.LineCollection;
import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TurtleModifierAction;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ReorderTurtleAction} tries to reorder the line segments of a {@link Turtle}'s path such that the
 * the new path will take less time to draw.  
 * First it attempts to remove any duplicate line segments.
 * Second it runs a "greedy tour" which does a pretty good job of sorting by draw-first, travel-second behavior. 
 * @author Dan Royer
 *
 */
public class ReorderTurtleAction extends TurtleModifierAction {
	private static final Logger logger = LoggerFactory.getLogger(ReorderTurtleAction.class);
	
	public ReorderTurtleAction() {
		super(Translator.get("Reorder"));
	}
	
	public Turtle run(Turtle turtle) {
		if(turtle.history.isEmpty()) return turtle;
		
		logger.debug("reorder() start @ {} instructions.", turtle.history.size());
		
		Turtle output = new Turtle();
		output.history.clear();
		
		// history is made of changes, travels, and draws
		List<Turtle> colors = turtle.splitByToolChange();
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
	private Turtle reorderTurtle(Turtle turtle) {
		LineCollection originalLines = turtle.getAsLineSegments();
		int originalCount = originalLines.size();
		Color c = turtle.getFirstColor();
		logger.debug("  {} converted to {} lines.", c.hashCode(), originalCount);

		List<LineCollection> firstPass = greedyReordering(originalLines);
		LineCollection secondPass = sortFirstPass(firstPass);

		Turtle t = new Turtle(c);
		t.addLineSegments(secondPass);
		return t;
	}

	/**
	 * Search firstPass for elements which are connected in sequence.  Two elments are connected in sequence if
	 * (A.start == B.start || A.start == B.end).
	 * @param firstPass a list of {@link LineCollection}s
	 * @return
	 */
	private LineCollection sortFirstPass(List<LineCollection> firstPass) {
		final double epsilon = 1e-6;

		if(firstPass.isEmpty()) return new LineCollection();

		for(int i=0;i<firstPass.size();++i) {
		 	LineCollection a = firstPass.get(i);
			if(a.isEmpty()) continue;

		 	for(int j=i+1;j<firstPass.size();++j) {
				LineCollection b = firstPass.get(j);
				if(b.isEmpty()) continue;

				if(a.getEnd().equalsEpsilon(b.getStart(),epsilon)) {
					a.addAll(b);
					b.clear();
				} else if(a.getEnd().equalsEpsilon(b.getEnd(),epsilon)) {
					b.flip();
					a.addAll(b);
					b.clear();
				} else if(a.getStart().equalsEpsilon(b.getStart(),epsilon)) {
					a.flip();
					a.addAll(b);
					b.clear();
				} else if(a.getStart().equalsEpsilon(b.getEnd(),epsilon)) {
					a.flip();
					b.flip();
					a.addAll(b);
					b.clear();
				}
			}
		}

		// remove the empty elements.
		List<LineCollection> secondPass = new ArrayList<>();
		for(LineCollection lc : firstPass) {
			if(!lc.isEmpty()) {
				secondPass.add(lc);
			}
		}

		LineCollection output = new LineCollection();

		if(secondPass.size()==0) {
			logger.debug("  no reordering.");
			return output;
		}
		if(secondPass.get(0).isEmpty()) {
			logger.debug("  not possible?!");
			return output;
		}

		// another greedy tour.
		Point2D lastPosition = secondPass.get(0).getEnd();
		output.addAll(secondPass.remove(0));

		while(!secondPass.isEmpty()) {
			LineCollection best = null;
			double distance = Double.MAX_VALUE;
			boolean flip=false;
			for(LineCollection lc : secondPass) {
				double d0 = lc.getStart().distanceSquared(lastPosition);
				double d1 = lc.getEnd().distanceSquared(lastPosition);
				double nearest = Math.min(d0, d1);
				if(distance > nearest) {
					distance = nearest;
					best = lc;
					flip = (d1<d0);
				}
			}
			secondPass.remove(best);
			if(flip) best.flip();
			output.addAll(best);
			lastPosition = best.getEnd();
		}

		return output;
	}

	/**
	 * From the pool of uniqueLines, take one and make it the head.
	 * looking for the nearest available segment that begins where the head ends.
	 * The segment found is removed from the available pool and becomes the new head.  Repeat until the avilable pool is empty.
	 * @param uniqueLines the unsorted list.
	 * @return the sorted list.
	 */
	private List<LineCollection> greedyReordering(LineCollection uniqueLines) {
		logger.debug("  greedyReordering()");
		List<LineCollection> firstPass = new ArrayList<>();
		if(uniqueLines.isEmpty()) return firstPass;

		LineCollection orderedLines = new LineCollection();

		Point2D lastPosition = uniqueLines.get(0).start;
		
		while(!uniqueLines.isEmpty()) {
			double bestD = Double.MAX_VALUE;
			LineSegment2D bestLine = null;
			boolean bestFlip = false;
			
			for( LineSegment2D line : uniqueLines ) {
				// is either end of line closer than our best?
				double d0 = lastPosition.distanceSquared(line.start);
				double d1 = lastPosition.distanceSquared(line.end);
				double nearest = Math.min(d0, d1);
				if(bestD > nearest) {
					bestD = nearest;
					bestLine = line;
					bestFlip = (d1 < d0);
				}
				if(bestD==0) break;
			}
			
			if(bestFlip) bestLine.flip();

			if(bestD>1e-6) {
				firstPass.add(orderedLines);
				orderedLines = new LineCollection();
			}

			uniqueLines.remove(bestLine);
			orderedLines.add(bestLine);
			
			// Start next iteration where current line ends.
			lastPosition = bestLine.end;
		}

		if(!orderedLines.isEmpty()) {
			firstPass.add(orderedLines);
		}
		
		return firstPass;
	}

	
	// TODO: move this to its own Action?
	@SuppressWarnings("unused")
	private LineCollection removeDuplicates(LineCollection originalLines, double EPSILON2) {
		logger.debug("  removeDuplicates()");
		LineCollection uniqueLines = new LineCollection();

		for(LineSegment2D candidateLine : originalLines) {
			boolean isDuplicate = false;
			
			// Compare this line to all the lines previously marked as non-duplicate
			for( LineSegment2D uniqueLine : uniqueLines ) {
				// Check if lines are colinear
				if( uniqueLine.ptLineDistSq(candidateLine.start) < EPSILON2 &&
					uniqueLine.ptLineDistSq(candidateLine.end) < EPSILON2 ) {
					// they are!
					// if they touch or overlap then I have a candidate.
					// measure where the points are relative to each other.
					boolean candidateStartsCloseToUnique = uniqueLine.ptSegDistSq(candidateLine.start) < EPSILON2;
					boolean candidateEndsCloseToUnique = uniqueLine.ptSegDistSq(candidateLine.end) < EPSILON2;
					
					if(candidateStartsCloseToUnique) {
						if(candidateEndsCloseToUnique) {
							// Candidate doesn't add anything which isn't already covered by the unique line.
							// No further action needed.
						} else {
							// Partial overlap, extend uniqueLine
							extendLine(uniqueLine, candidateLine.end);
						}
						isDuplicate = true;
						break;
					} else if(candidateEndsCloseToUnique) {
						// Partial overlap, extend uniqueLine
						extendLine(uniqueLine, candidateLine.start);
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
 	private void extendLine(LineSegment2D targetLine, Point2D extPoint) {
		double newLengthA = targetLine.start.distanceSquared(extPoint);
		double newLengthB = targetLine.end.distanceSquared(extPoint);
		double currentLength = targetLine.lengthSquared();
		
		// Maximize length of target line by replacing the start or end point with the extPoint		
		if(newLengthA > currentLength && newLengthA > newLengthB) {
			// Draw line from targetLine.a to extPoint
			targetLine.end = extPoint;
		} else if(newLengthB > currentLength) {
			// Draw line from extPoint to targetLine.b 
			targetLine.start = extPoint;
		}
	}
}
