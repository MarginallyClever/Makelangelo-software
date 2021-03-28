package com.marginallyclever.makelangelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import javax.vecmath.Vector3d;

import com.marginallyclever.core.LineIntersectionTester;
import com.marginallyclever.core.Point2D;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.core.turtle.TurtleMove;

public class TurtleOptimizer {
	private class Polyline extends LinkedList<Integer> {
		private static final long serialVersionUID = 1L;
	}

	private class Segment2D {
		public int a,b;
		
		public Segment2D(int aa,int bb) {
			a=aa;
			b=bb;
		}
	}
	
	private ArrayList<Point2D> points = new ArrayList<Point2D>();
	private ArrayList<Segment2D> segments = new ArrayList<Segment2D>();
	private ArrayList<Polyline> polyLines = new ArrayList<Polyline>();
	
	private int optimizeAddPointToPool(double x,double y,final double EPSILON) {
		int i=0;
		for( Point2D p1 : points ) {
			if(Math.abs(x-p1.x)<EPSILON && Math.abs(y-p1.y)<EPSILON) {
				// no
				return i;
			}
			++i;
		}
		// yes
		points.add(new Point2D(x,y));
		return i; // which is the same as points.size()-1;
	}

	/**
	 * Build a list of unique points (further than EPSILON apart)
	 * and line segments (two points connected by a line)
	 * @param turtle
	 * @return numbre of travel moves.
	 */
	private int buildPointAndSegmentList(Turtle turtle) {
		final double EPSILON = 0.01;
		
		TurtleMove prev = null;
		int drawMoves=0;
		int travelMoves=0;

		for( TurtleMove m : turtle.history ) {
			// is this point unique in the pool?
			if( m.isUp ) {
				travelMoves++;
			} else {
				drawMoves++;
				if(prev!=null) {
					int a = optimizeAddPointToPool(prev.x,prev.y,EPSILON);
					int b = optimizeAddPointToPool(m.x,m.y,EPSILON);
					if(a!=b) {
						// we keep only segments that are longer than EPSILON.
						segments.add(new Segment2D(a,b));
					}
				}
			}
			
			prev = m;
		}
		
		//System.out.println("history = "+turtle.history.size());
		//System.out.println("drawMoves = "+drawMoves);
		//System.out.println("travelMoves = "+travelMoves);
		//System.out.println("points = "+points.size());
		//System.out.println("segments = "+segments.size());
		
		assert(segments.size()<=drawMoves);
		return travelMoves;
	}

	/**
	 * Build the {@link Polyline} list.  A Polyline is a set of segments arranged head-to-tail. 
	 */
	private void collateSegmentsIntoPolylines() {
		while( segments.size()>0 ) {
			Polyline line = new Polyline();
			polyLines.add(line);
			
			Segment2D s0 = segments.remove(0);
			int head = s0.a;
			int tail = s0.b;
			line.add(head);
			line.add(tail);

			// if a segment meets the head or tail of this snake, take it from the segment pool and add grow the snake. 
			ArrayList<Segment2D> segmentsToKeep = new ArrayList<Segment2D>();
			for( Segment2D s : segments ) {
					 if(s.a==head) {	line.addFirst(s.b);		head=s.b;	}
				else if(s.b==head) {	line.addFirst(s.a);		head=s.a;	}
				else if(s.a==tail) {	line.addLast(s.b);		tail=s.b;	}
				else if(s.b==tail) {	line.addLast(s.a);		tail=s.a;	}
				else segmentsToKeep.add(s);
			}
			segments = segmentsToKeep;
			//System.out.println("line size="+line.size());
		}
		System.out.println("polylines = "+polyLines.size());
		assert(polyLines.size()<=segments.size());
	}
	
	public void optimizeOneTurtle(Turtle turtle) {
		double len0 = turtle.getDistance();
		
		points.clear();
		segments.clear();
		polyLines.clear();
		
		int travelMoves = buildPointAndSegmentList(turtle);
		collateSegmentsIntoPolylines();
		assert(polyLines.size()<=travelMoves);

		// find the bounds of the points
		Point2D top = new Point2D(-Double.MAX_VALUE,-Double.MAX_VALUE);
		Point2D bottom = new Point2D(Double.MAX_VALUE,Double.MAX_VALUE);
		for( Point2D p : points ) {
			top.x = Math.max(p.x, top.x);
			top.y = Math.max(p.y, top.y);
			bottom.x = Math.min(p.x, bottom.x);
			bottom.y = Math.min(p.y, bottom.y);
		}
		top.x+=0.001;
		top.y+=0.001;
		double w = top.x-bottom.x;
		double h = top.y-bottom.y;

		// we have a box from top to bottom.  
		// let's make a grid bucketsPerSide*bucketsPerSide large.
		int numEnds = polyLines.size()*2;
		int bucketsPerSide = (int)Math.ceil(Math.sqrt(numEnds/2));
		if(bucketsPerSide<1) bucketsPerSide=1;
		
		// allocate buckets
		Polyline[] buckets = new Polyline[bucketsPerSide*bucketsPerSide];
		for( int b=0;b<buckets.length;b++) {
			buckets[b] = new Polyline();
		}
		
		// put the head and tail of each Polyline into their buckets.
		for( Polyline line : polyLines ) {
			for( int index : new int[] { line.peekFirst(), line.peekLast() } ) {
				Point2D p = points.get(index);
				int ix = (int)(bucketsPerSide * (p.x-bottom.x) / w);
				int iy = (int)(bucketsPerSide * (p.y-bottom.y) / h);
				buckets[iy*bucketsPerSide+ix].add(index);
			}
		}

		//printBucketDebugInfo(buckets,bucketsPerSide);
		
		// Use the buckets to quickly and greedily sort Polylines.
		ArrayList<Polyline> newSequence = new ArrayList<Polyline>();
		ArrayList<Polyline> foundLines = new ArrayList<Polyline>(); 
		// indexes are point id numbers.
		Polyline foundIndexes = new Polyline();
		Point2D lastPoint=null;
		int ix,iy;
		int bx=0;
		int by=0;
		
		while(polyLines.size()>0) {
			int radius=0;
			// search nearby buckets in an expanding circle until we find something.
			while(foundIndexes.size()==0) {
				if(radius==0) {
					foundIndexes.addAll(buckets[by*bucketsPerSide+bx]);
				} else {
					//System.out.println("radius="+radius);
					for(iy=by-radius;iy<=by+radius;++iy) {
						if(iy<0 || iy >= bucketsPerSide) continue;
						ix = bx-radius;  if(ix>=0            ) foundIndexes.addAll(buckets[iy*bucketsPerSide+ix]);
						ix = bx+radius;  if(ix<bucketsPerSide) foundIndexes.addAll(buckets[iy*bucketsPerSide+ix]);
					}
					for(ix=bx-radius;ix<=bx+radius;++ix) {
						if(ix<0 || ix >= bucketsPerSide) continue;
						iy = by-radius;  if(iy>=0            ) foundIndexes.addAll(buckets[iy*bucketsPerSide+ix]);
						iy = by+radius;  if(iy<bucketsPerSide) foundIndexes.addAll(buckets[iy*bucketsPerSide+ix]);
					}
				}
				radius++;
			}
			
			// We found at least one index, maybe more.
			// We don't know which bucket the index(es) came from.
			// figure out to which polyLine they belong.
			Polyline bestLine;
			{
				//System.out.println("found "+foundIndexes.size()+" candidate point(s).");
				for( Polyline line : polyLines ) {
					int first=line.peekFirst();
					int last =line.peekLast();
					if(foundIndexes.contains(first) || foundIndexes.contains(last)) {
						// make sure found lines are unique.
						if(!foundLines.contains(line)) {
							foundLines.add(line);
						}
					}
				}
				//System.out.println("found "+foundLines.size()+" unique polyLine(s).");
				
				// We know which lines were found.
				// We know they are pretty close.
				// We prefer Polyline with head and tail close together.
				// Sort based on this preference.
				final Point2D testLastPoint = lastPoint;
				if(lastPoint!=null) {
					foundLines.sort(new Comparator<Polyline>() {
						@Override
						public int compare(Polyline a, Polyline b) {					
							Point2D aHead=points.get(a.peekFirst());
							Point2D aTail=points.get(a.peekLast());
							Point2D bHead=points.get(b.peekFirst());
							Point2D bTail=points.get(b.peekLast());
							//double aa = Math.min(testLastPoint.distanceSquared(aHead),testLastPoint.distanceSquared(aTail)) - aHead.distanceSquared(aTail);
							//double bb = Math.min(testLastPoint.distanceSquared(bHead),testLastPoint.distanceSquared(bTail)) - bHead.distanceSquared(bTail);
							double aa = testLastPoint.distanceSquared(aHead) - aHead.distanceSquared(aTail);
							double bb = testLastPoint.distanceSquared(bHead) - bHead.distanceSquared(bTail);
							//double aa = testLastPoint.distanceSquared(aTail) - aHead.distanceSquared(aTail);
							//double bb = testLastPoint.distanceSquared(bTail) - bHead.distanceSquared(bTail);
							
							return (int)(aa-bb);
						}
					});
				}
				// the first line is the best line in the list
				bestLine = foundLines.get(0);
				foundLines.clear();
				
				// set bx/by for next bucket search
				int first = bestLine.peekLast();
				int last = bestLine.peekLast();
				lastPoint = points.get(foundIndexes.contains(first) ? last : first);
				bx = (int)(bucketsPerSide * (lastPoint.x-bottom.x) / w);
				by = (int)(bucketsPerSide * (lastPoint.y-bottom.y) / h);

				foundIndexes.clear();
			}

			newSequence.add(bestLine);
			// Don't forget to remove bestline from the buckets.
			polyLines.remove(bestLine);
			Integer bh = bestLine.peekFirst();
			Integer bt = bestLine.peekLast();
			for( Polyline b : buckets ) {
				b.remove(bh);
				b.remove(bt);
			}
			
			if((polyLines.size()%1000)==0) {
				//System.out.println(polyLines.size());
			}
		}
		
		untwist(newSequence);

		// rebuild the new, more efficient turtle path
		System.out.println("Rebuilding...");
		ArrayList<TurtleMove> newHistory = new ArrayList<TurtleMove>();
		for( Polyline line : newSequence ) {
			boolean first=true;
			for( Integer index : line ) {
				Point2D p = points.get(index);
				newHistory.add(new TurtleMove(p.x,p.y,first));
				first=false;
			}
		}
		turtle.history = newHistory;
		
		double len1 = turtle.getDistance();
		System.out.println("before="+len0+", after="+len1+", saved="+(len0-len1));
	}

	// the newSequence is pretty good... but travel moves are still crossing over each other.
	// 
	private void untwist(ArrayList<Polyline> newSequence) {
		System.out.println("Untwisting...");
		int numFixed=0;
		int numUnfixed=0;
		
		int size=newSequence.size();
		for(int a=0;a<size-1;++a) {
			// travel between Polyline a and a+1
			Integer ah = newSequence.get(a).peekLast();
			Integer at = newSequence.get(a+1).peekFirst();
			
			for(int b=a+1;b<size-1;++b) {
				// travel between Polyline b and b+1
				Integer bh = newSequence.get(b).peekLast();
				Integer bt = newSequence.get(b+1).peekFirst();
				
				if(travelsCross(ah,at,bh,bt)) {
					if(!travelsCross(ah,bh,at,bt)) {
						numFixed++;
						// reverse the content of each polyline a+1...b, inclusive.
						int start = a+1;
						int end = b;
						for(int c=start;c<=end;++c) {
							Collections.reverse(newSequence.get(c));
						}
						// reverse the order of polylines a+1...b, inclusive.
						int mid = (end-start)/2;
						for(int c=0;c<=mid;++c) {
							Collections.swap(newSequence, start+c, end-c);
						}
	
						at = newSequence.get(a+1).peekFirst();
						bh = newSequence.get(b).peekLast();
						bt = newSequence.get(b+1).peekFirst();
						if(travelsCross(ah,at,bh,bt)) {
						}
					} else {
						numUnfixed++;
					}
				}
			}
		}
		System.out.println("fixed="+numFixed+" unfixed="+numUnfixed);
	}
	
	private boolean travelsCross(Integer ah, Integer at, Integer bh, Integer bt) {
		Point2D A = points.get(ah);
		Point2D B = points.get(at);
		Point2D C = points.get(bh);
		Point2D D = points.get(bt);
		return LineIntersectionTester.doIntersect(A,B,C,D);
	}
	

	@SuppressWarnings("unused")
	private void printBucketDebugInfo(Polyline[] buckets,int bucketsPerSide) {
		// some debug info
		int i=0;
		System.out.println("buckets=[");
		for(int y=0;y<bucketsPerSide;++y) {
			for(int x=0;x<bucketsPerSide;++x) {
				System.out.print(buckets[i].size()+"\t");
				i++;
			}
			System.out.println();
		}
		System.out.println("]");
	}

	/**
	 * Any time there are two pen up moves in a row then the first is not needed.
	 * @param turtle to be simplified.
	 */
	public void removeSequentialPenUpMoves(Turtle turtle) {
		ArrayList<TurtleMove> toKeep = new ArrayList<TurtleMove>();
		
		int len = turtle.history.size();
		
		TurtleMove a=turtle.history.get(0);
		TurtleMove b=null;
		for(int i=1;i<len;++i) {
			b = turtle.history.get(i);
			// if abc are up then b is redundant.
			if(a.isUp && b.isUp) {
				// do nothing. lose a.
			} else {
				// a not redudant, keep it.
				toKeep.add(a);
			}
			a = b;
		}
		if(b!=null) {
			toKeep.add(b);
		}

		int len2 = toKeep.size();
		System.out.println("history start="+len+", end="+len2+", saved="+(len-len2));
		turtle.history.clear();
		turtle.history.addAll(toKeep);
	}
	
	/**
	 * Any time there are two pen down moves in ab and bc where abc is a straight line?  Lose b.
	 * @param turtle to be simplified.
	 */
	public void removeSequentialLinearPenDownMoves(Turtle turtle) {
		ArrayList<TurtleMove> toKeep = new ArrayList<TurtleMove>();
		
		int len = turtle.history.size();

		Vector3d v0 = new Vector3d();
		Vector3d v1 = new Vector3d();

		TurtleMove a;
		TurtleMove b;
		TurtleMove c=null;
		toKeep.add(turtle.history.get(0));
		for(int i=1;i<len-1;++i) {
			a = turtle.history.get(i-1);
			b = turtle.history.get(i);
			c = turtle.history.get(i+1);
			// if abc are up then b is redundant.
			if(!b.isUp && !c.isUp) {
				// are ABC in a straight line?
				v0.x = b.x-a.x;
				v0.y = b.y-a.y;
				v0.normalize();
				v1.x = c.x-b.x;
				v1.y = c.y-b.y;
				v1.normalize();
				// 1 degree = cos(PI/180) = 0.99984769515
				if(v1.dot(v0)>0.99984769515) {
					// Less than 1 degree.  Lose b.
				} else {
					// 1 degree or more.  b not redudant.  Keep it.
					toKeep.add(b);
				}
			} else {
				// b not redudant, keep it.
				toKeep.add(b);
			}
		}
		if(c!=null) {
			toKeep.add(c);
		}

		int len2 = toKeep.size();
		System.out.println("history start="+len+", end="+len2+", saved="+(len-len2));
		turtle.history.clear();
		turtle.history.addAll(toKeep);
	}
}
