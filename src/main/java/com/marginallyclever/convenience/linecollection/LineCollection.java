package com.marginallyclever.convenience.linecollection;

import com.marginallyclever.convenience.LineSegment2D;

import javax.vecmath.Point2d;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * <p>An array of 2D line segments and their colors.</p>
 * <p>Utilities for splitting and simplifying the collection.</p>
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
	 * <p>Splits this collection by color.  Does not affect the original list.  Does not deep copy.</p>
	 * @return the list of collections separated by color
	 */
	public List<LineCollection> splitByColor() {
		if(this.isEmpty()) return new ArrayList<>();

		var map = new HashMap<Color,LineCollection>();
		LineSegment2D head = getFirst();
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
	 * <p>Splits this collection by travel moves.  Assumes the list is already well sorted.
	 * A travel move is any moment in the collection where element (N).end != (N+1).start.</p>
	 * <p>Does not affect the original list.  Does not deep copy.</p>
	 * @return the list of collections separated by color
	 */
	public List<LineCollection> splitByTravel() {
		List<LineCollection> result = new ArrayList<>();
		if(this.isEmpty()) return result;

		LineSegment2D head = getFirst();
		LineCollection c = new LineCollection();
		result.add(c);
		c.add(head);

		for(int i=1;i<size();++i) {
			LineSegment2D next = get(i);
			if(next.start == head.end || next.start.distanceSquared(head.end)<1e-6) {
				c.add(next);
				head = next;
			} else {
				head = next;
				c = new LineCollection();
				result.add(c);
				c.add(head);
			}
		}
		return result;
	}

	/**
	 * Simplify the line collection using the <a href="https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Ramer-Douglas-Peucker algorithm</a>.
	 * @param distanceTolerance the distance tolerance
	 * @return the simplified line collection
	 */
	public LineCollection simplify(double distanceTolerance) {
		return new RamerDouglasPeuckerRecursive(this).simplify(distanceTolerance);
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