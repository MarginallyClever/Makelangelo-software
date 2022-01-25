package com.marginallyclever.convenience;

import java.util.ArrayList;

public class LineCollection extends ArrayList<LineSegment2D> {
	private static final long serialVersionUID = 1L;
	private boolean[] usePt;

	public LineCollection() {
		super();
	}
	
	public LineCollection(ArrayList<LineSegment2D> list) {
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
				if(next.c.isEqualTo(head.c)) {
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
	public ArrayList<LineCollection> splitByTravel() {
		ArrayList<LineCollection> result = new ArrayList<LineCollection> ();
		if(this.size()>0) {
			LineSegment2D head = get(0);
			
			LineCollection c = new LineCollection();
			result.add(c);
			c.add(head);
			
			for(int i=1;i<size();++i) {
				LineSegment2D next = get(i);
				if(next.b.distanceSquared(head.a)<1e-6) {
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

	public LineCollection simplify(double distanceTolerance) {
		usePt = new boolean[size()];
		for (int i = 0; i < size(); i++) {
			usePt[i] = true;
		}
		
		simplifySection(0, size() - 1,distanceTolerance);
		
		LineCollection result = new LineCollection();
		Point2D head = get(0).a;
		
		for (int i = 0; i < size(); i++) {
			if (usePt[i]) {
				Point2D next = get(i).b;
				result.add(new LineSegment2D(head,next,get(i).c));
				head=next;
			}
		}
		
		return result;
	}

	private void simplifySection(int i, int j,double distanceTolerance) {
		if ((i + 1) == j) return;
		LineSegment2D seg = new LineSegment2D(
			get(i).a,
			get(j).b,
			get(i).c);
		double maxDistance = -1.0;
		int maxIndex = i;
		for (int k = i + 1; k < j; k++) {
			double distance = seg.ptLineDistSq(get(k).b);
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
			simplifySection(i, maxIndex,distanceTolerance);
			simplifySection(maxIndex, j,distanceTolerance);
		}
	}
};