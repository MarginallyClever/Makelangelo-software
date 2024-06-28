package com.marginallyclever.convenience;

import javax.vecmath.Point2d;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LineCollection extends ArrayList<LineSegment2D> {
	private boolean[] usePt;

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
	public ArrayList<LineCollection> splitByColor() {
		ArrayList<LineCollection> result = new ArrayList<LineCollection> ();
		if(this.size()>0) {
			LineSegment2D head = get(0);
			
			LineCollection c = new LineCollection();
			result.add(c);
			c.add(head);
			
			for(int i=1;i<size();++i) {
				LineSegment2D next = get(i);
				if(next.color.equals(head.color)) {
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
	 * Splits this collection by travel moves.  Does not affect the original list.  Does not deep copy.
	 * A travel move is any moment in the collection where element (N).b != (N+1).a
	 * @return the list of collections separated by color
	 */
	public List<LineCollection> splitByTravel() {
		List<LineCollection> result = new ArrayList<> ();
		if(this.size()==0) return result;

		LineSegment2D head = get(0);

		LineCollection c = new LineCollection();
		result.add(c);
		c.add(head);

		for(int i=1;i<size();++i) {
			LineSegment2D next = get(i);
			if(next.start.distanceSquared(head.end)>1e-6) {
				c = new LineCollection();
				result.add(c);
			}
			c.add(next);
			head = next;
		}
		return result;
	}

	// remove all redundant points from the list
	public LineCollection simplify(double distanceTolerance) {
		LineCollection result = new LineCollection();
		if (size() < 3) { // If less than 3 points, just return the original collection
			result.addAll(this);
			return result;
		}

		Point2d head = get(0).start;
		Point2d tail = get(0).end;

		// get the first segment
		LineSegment2D seg = new LineSegment2D(head,tail,get(0).color);

		for(int i=1; i < size(); i++) {
			LineSegment2D next = get(i);
			if (seg.ptLineDistSq(next.end) > distanceTolerance) {
				// the next point is far enough away from the line, so add the line to the result
				result.add(new LineSegment2D(head,tail,get(i-1).color));
				head = tail;
				tail = next.end;
				seg = new LineSegment2D(head,tail,get(i).color);
			} else {
				// the next point is close to the line, so extend the line
				tail = next.end;
				//seg = new LineSegment2D(head,tail,get(i).color);
			}
		}
		result.add(new LineSegment2D(head,tail,get(size()-1).color));

		return result;
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