package com.marginallyclever.convenience.linecollection;

import com.marginallyclever.convenience.LineSegment2D;

import javax.vecmath.Point2d;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A collection of 2D line segments and their colors.  Utilities for splitting and simplifying the collection.
 */
public class LineCollection extends ArrayList<LineSegment2D> {
	public LineCollection() {
		super();
	}
	
	public LineCollection(LineCollection list) {
		super();
		addAll(list);
	}
	
	/**
	 * Splits this collection by color.  Does not affect the original list.  Does not deep copy.
	 * @return the list of collections separated by color
	 */
	public List<LineCollection> splitByColor() {
		var map = new HashMap<Color,LineCollection>();

		if(this.isEmpty()) return new ArrayList<>();

		LineSegment2D head = get(0);
		LineCollection c = new LineCollection();
		map.put(head.color,c);
		c.add(head);

		for(int i=1;i<size();++i) {
			LineSegment2D next = get(i);
			if(!next.color.equals(head.color)) {
				head = next;
				if(map.containsKey(next.color)) {
					c = map.get(next.color);
				} else {
					c = new LineCollection();
					map.put(next.color, c);
				}
			}
			c.add(next);
		}

        return new ArrayList<>(map.values());
	}

	/**
	 * Splits this collection by travel moves.  Does not affect the original list.  Does not deep copy.
	 * A travel move is any moment in the collection where element (N).b != (N+1).a
	 * @return the list of collections separated by color
	 */
	public List<LineCollection> splitByTravel() {
		List<LineCollection> result = new ArrayList<> ();
		if(this.size()>0) {
			LineSegment2D head = get(0);
			
			LineCollection c = new LineCollection();
			result.add(c);
			c.add(head);
			
			for(int i=1;i<size();++i) {
				LineSegment2D next = get(i);
				if(next.end.distanceSquared(head.start)<1e-6) {
					c.add(next);
				} else {
					head = next;
					c = new LineCollection();
					result.add(c);
					c.add(head);
				}
			}
		}
		return result;
	}

	/**
	 * Simplify the line collection by removing points that are within distanceTolerance of the line between their neighbors.
	 * @param distanceTolerance the distance tolerance
	 * @return the simplified line collection
	 */
	public LineCollection simplify(double distanceTolerance) {
		var len = size();
		boolean[] usePt = new boolean[len];
        Arrays.fill(usePt, true);
		
		simplifySection(0, len - 1,distanceTolerance,usePt);
		
		LineCollection result = new LineCollection();
		Point2d head = get(0).start;
		
		for (int i = 0; i < len; i++) {
			if (usePt[i]) {
				Point2d next = get(i).end;
				result.add(new LineSegment2D(head,next,get(i).color));
				head=next;
			}
		}
		
		return result;
	}

	/**
	 * Simplify the line collection by removing points that are within distanceTolerance of the line between their neighbors.
	 * The strategy is to split the work at the point that is farthest from the line between the start and end points,
	 * then try to simplify the two halves.
	 * @param i the start index
	 * @param j the end index
	 * @param distanceTolerance the distance tolerance
	 * @param usePt the array of booleans that indicates whether a point should be retained after simplification
	 */
	private void simplifySection(int i, int j,double distanceTolerance,boolean[] usePt) {
		if ((i + 1) == j) return;
		LineSegment2D seg = new LineSegment2D(
			get(i).start,
			get(j).end,
			get(i).color);
		double maxDistance = -1.0;
		int maxIndex = i;
		for (int k = i + 1; k < j; k++) {
			double distance = seg.ptLineDistSq(get(k).end);
			if (distance > maxDistance) {
				maxDistance = distance;
				maxIndex = k;
			}
		}
		if (maxDistance <= distanceTolerance) {
			for (int k = i + 1; k < j; k++) {
				usePt[k] = false;
			}
		} else {
			simplifySection(i, maxIndex,distanceTolerance,usePt);
			simplifySection(maxIndex, j,distanceTolerance,usePt);
		}
	}

	public Point2d getStart() {
		return get(0).start;
	}

	public Point2d getEnd() {
		return get(size()-1).end;
	}

	public void flip() {
		Collections.reverse(this);
		for( LineSegment2D line : this ) {
			line.flip();
		}
	}
}