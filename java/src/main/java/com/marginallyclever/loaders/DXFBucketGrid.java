package com.marginallyclever.loaders;

import org.kabeja.dxf.helpers.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DXFBucketGrid {
	public static final double DXF_EPSILON = 0.1;
	
	private List<DXFBucket> buckets;
	
	/**
	 * 
	 * @param cellsWide >0
	 * @param cellsHigh >0
	 * @param topLeft smallest X and smallest Y value
	 * @param bottomRight largest X and largest Y value
	 */
	public DXFBucketGrid(int cellsWide,int cellsHigh,Point topLeft,Point bottomRight) {
		int totalCells = cellsWide * cellsHigh;
		assert(totalCells>0);
		
		System.out.println("OUTER BOUNDS ("+topLeft.getX()+","+topLeft.getY()+")-("+bottomRight.getX()+","+bottomRight.getY()+")");
		buckets = new ArrayList<DXFBucket>();
		double dx = bottomRight.getX() - topLeft.getX(); 
		double dy = bottomRight.getY() - topLeft.getY();
		
		int x,y;
		for(y=0;y<cellsHigh;++y) {
			for(x=0;x<cellsWide;++x) {
				DXFBucket b = new DXFBucket(x,y);
				buckets.add(b);
				b.topLeft    .setX( ( x    / (double)cellsWide) * dx + topLeft.getX());
				b.topLeft    .setY( ( y    / (double)cellsHigh) * dy + topLeft.getY());
				b.bottomRight.setX( ((x+1) / (double)cellsWide) * dx + topLeft.getX());
				b.bottomRight.setY( ((y+1) / (double)cellsHigh) * dy + topLeft.getY());
				System.out.println(x+","+y+" = ("+b.topLeft.getX()+","+b.topLeft.getY()+")-("+b.bottomRight.getX()+","+b.bottomRight.getY()+")");
			}
		}
	}
	
	/**
	 * Add one DXFEntity to the appropriate bucket.
	 * @param e the entity
	 * @param p the point.  Could be the start or the end of the entity
	 */
	public void addEntity(DXFBucketEntity e,Point p) {
		Iterator<DXFBucket> ib = buckets.iterator();
		while(ib.hasNext()) {
			DXFBucket b = ib.next();
			if(b.topLeft.getY() <= p.getY() && 
				b.topLeft.getX() <= p.getX() && 
				b.bottomRight.getY() >= p.getY() && 
				b.bottomRight.getX() >= p.getX()) {
				// inside this bucket's region
				b.push(e,p);
				return;
			}
		}
		System.out.println("Not added "+p);
	}
	
	public void countEntitiesInBuckets() {
		int total=0;

		Iterator<DXFBucket> ib = buckets.iterator();
		while(ib.hasNext()) {
			DXFBucket b = ib.next();
			System.out.println("bucket "+b.x+","+b.y+" has "+b.contents.size()+" entities.");
			total+=b.contents.size();
		}
		System.out.println(total+" total entities in buckets (including duplicates).");
	}
	
	protected void sortEntitiesIntoContinguousGroups(List<DXFGroup> groups) {
		System.out.println(groups.size()+ " groups at start.");
		int entityCount=0;
		DXFBucket bucket;
		Point p=null;
	
		// as long as there are entities in buckets
		while((bucket=findNonEmptyBucket(p))!=null) {
			// start a new group
			DXFGroup group = new DXFGroup();
			System.out.print("+group");
			groups.add(group);
			// add the first entity found 
			DXFBucketEntity be = bucket.getFirstEntity();
			DXFBucket otherBucket = bucket;

			DXFBucketEntity firstBe = be;
			DXFBucket firstBucket = otherBucket;

			// loop forward
			do {
				++entityCount;
				System.out.print("+");
				bucket = otherBucket;
				bucket.remove(be);
				group.addLast(be);
				// find the bucket at the far end of this entity
				otherBucket = be.getRemainingBucket();
				// find the physical end of this entity in the bucket
				p = be.getPointInBucket(otherBucket);
				// take this entity out
				otherBucket.remove(be);
				// find the best fit for an entity that shares this point
				be = otherBucket.findBestFitToPoint(p,DXF_EPSILON);
				// until there are no more entities in this contiguous group
			} while(be!=null);
			
			// maybe the first line we picked is in the middle of a contiguous group.  Find the real start
			// loop backward
			otherBucket = firstBucket;
			be = firstBe;
			p = be.getPointInBucket(otherBucket);
			be = otherBucket.findBestFitToPoint(p,DXF_EPSILON);
			while(be!=null) {
				++entityCount;
				System.out.print("~");
				bucket = otherBucket;
				bucket.remove(be);
				group.addFirst(be);
				// find the bucket at the far end of this entity
				otherBucket = be.getRemainingBucket();
				// find the physical end of this entity in the bucket
				p = be.getPointInBucket(otherBucket);
				// take this entity out
				otherBucket.remove(be);
				// find the best fit for an entity that shares this point
				be = otherBucket.findBestFitToPoint(p,DXF_EPSILON);
				// until there are no more entities in this contiguous group
			} 
			
			System.out.println();
		}
		System.out.println(groups.size()+ " groups at end.");
		System.out.println(entityCount+ " entities at end.");
	}
	
	protected DXFBucket findNonEmptyBucket(Point p) {
		if(p==null) {
			Iterator<DXFBucket> bucketIter = buckets.iterator();
			while(bucketIter.hasNext()) {
				DXFBucket bucket = bucketIter.next();
				if(!bucket.contents.isEmpty()) return bucket;
			}
		} else {
			DXFBucket bestBucket = null;
			double bestD=100000000;
			Iterator<DXFBucket> bucketIter = buckets.iterator();
			while(bucketIter.hasNext()) {
				DXFBucket bucket = bucketIter.next();
				if(!bucket.contents.isEmpty()) {
					if(bestBucket==null) {
						bestBucket = bucket;
						double dx = bestBucket.topLeft.getX() + bestBucket.bottomRight.getX();
						double dy = bestBucket.topLeft.getY() + bestBucket.bottomRight.getY();
						bestD = dx*dx + dy*dy; 	
					} else {
						double dx = bestBucket.topLeft.getX() + bestBucket.bottomRight.getX();
						double dy = bestBucket.topLeft.getY() + bestBucket.bottomRight.getY();
						double d = dx*dx + dy*dy; 	
						if(bestD<d) {
							bestD=d;
							bestBucket = bucket;
						}
					}
				}
			}
			return bestBucket;
		}
		return null;
	}
}
