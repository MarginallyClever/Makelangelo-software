package com.marginallyclever.makelangeloRobot.loadAndSave;

import org.kabeja.dxf.helpers.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Used by LoadDXF2.  A grid of DXFBuckets and convenience methods to access the buckets and their contents.
 * @author Dan Royer
 *
 */
public class DXFBucketGrid {
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
		
		//System.out.println("OUTER BOUNDS ("+topLeft.getX()+","+topLeft.getY()+")-("+bottomRight.getX()+","+bottomRight.getY()+")");
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
				//System.out.println(x+","+y+" = ("+b.topLeft.getX()+","+b.topLeft.getY()+")-("+b.bottomRight.getX()+","+b.bottomRight.getY()+")");
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
				b.bottomRight.getY() > p.getY() && 
				b.bottomRight.getX() > p.getX()) {
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
			if(b.contents.size()>0) {
				System.out.println("bucket "+b.x+","+b.y+" has "+b.contents.size()+" entities.");
				total+=b.contents.size();
			}
		}
		System.out.println(total+" total entities in buckets (including duplicates).");
	}
	
	/**
	 * dumps every bucket into one group, probably creating duplicates.
	 * @param groups
	 */
	protected void dumpEveryBucketIntoOneGroup(List<DXFGroup> groups) {
		DXFGroup group = new DXFGroup();
		groups.add(group);
		
		Iterator<DXFBucket> ib = buckets.iterator();
		while(ib.hasNext()) {
			DXFBucket bucket = ib.next();
			Iterator<DXFBucketEntity> bei = bucket.contents.iterator();
			while(bei.hasNext()) {
				group.addLast(bei.next());
			}
		}
		//System.out.println(groups.size()+ " groups after dump.");
		//System.out.println(group.entities.size()+ " entities after dump.");
	}
	
	protected void sortEntitiesIntoContinguousGroups(List<DXFGroup> groups,double epsilon) {
		DXFBucket firstBucket;
		Point p=null;
	
		double dx = buckets.get(buckets.size()-1).bottomRight.getX() - buckets.get(0).topLeft.getX();
		double dy = buckets.get(buckets.size()-1).bottomRight.getY() - buckets.get(0).topLeft.getY();
		double BIG_EPSILON = dx*dx+dy*dy;
		
		// as long as there are entities in buckets
		while((firstBucket=findNonEmptyBucket(p,BIG_EPSILON))!=null) {
			do {
				// start a new group
				DXFGroup group = new DXFGroup();
				groups.add(group);
				// add the first entity found 
				DXFBucketEntity firstBe = firstBucket.findBestFitToPoint(p,BIG_EPSILON);
				DXFBucketEntity be = firstBe;
				DXFBucket thisBucket = firstBucket;
	
				// Loop forward
				while(be!=null) {
					thisBucket.remove(be);
					group.addLast(be);
					// find the bucket at the far end of this entity
					// find the physical end of this entity in the bucket
					DXFBucketPointPair other = be.getRemainingBucketPointPair();
					// find the best fit for an entity that shares this point
					be = other.bucket.findBestFitToPoint(other.point,epsilon);
					thisBucket = other.bucket;
					// until there are no more entities in this contiguous group
				}
				
				// Maybe the first line we picked is in the middle of a contiguous group.  
				// Find the start by looping backward.
				if(firstBe!=null) {
					p = firstBe.getPointInBucket(firstBucket);
					be = firstBucket.findBestFitToPoint(p,epsilon);
					thisBucket = firstBucket;
					while(be!=null) {
						thisBucket.remove(be);
						if(be!=firstBe) {
							group.addFirst(be);
						}
						// find the bucket at the far end of this entity
						DXFBucketPointPair other = be.getRemainingBucketPointPair();;
						// find the best fit for an entity that shares this point
						be = other.bucket.findBestFitToPoint(other.point,epsilon);
						// until there are no more entities in this contiguous group
						thisBucket = other.bucket;
					}
				}
				//System.out.println();
			} while(!firstBucket.contents.isEmpty());
		}

		//System.out.println(groups.size()+ " groups after sort.");
	}
	
	/**
	 * Find the non-empty bucket nearest to point p.  If p is null, pick the first non-empty bucket.
	 * @param p
	 * @return the selected bucket
	 */
	protected DXFBucket findNonEmptyBucket(Point p,double epsilon) {
		if(p==null) {
			Iterator<DXFBucket> bucketIter = buckets.iterator();
			while(bucketIter.hasNext()) {
				DXFBucket bucket = bucketIter.next();
				if(!bucket.contents.isEmpty()) return bucket;
			}
		} else {
			DXFBucket bestBucket = null;
			double bestD=epsilon*epsilon;
			Iterator<DXFBucket> bucketIter = buckets.iterator();
			while(bucketIter.hasNext()) {
				DXFBucket bucket = bucketIter.next();
				if(bucket.contents.isEmpty()) continue;
				if(bestBucket==null) {
					bestBucket = bucket;
					double dx = (bestBucket.topLeft.getX() + bestBucket.bottomRight.getX())/2;
					double dy = (bestBucket.topLeft.getY() + bestBucket.bottomRight.getY())/2;
					bestD = dx*dx + dy*dy; 	
				} else {
					double dx = (bestBucket.topLeft.getX() + bestBucket.bottomRight.getX())/2;
					double dy = (bestBucket.topLeft.getY() + bestBucket.bottomRight.getY())/2;
					double d = dx*dx + dy*dy; 	
					if(bestD>d) {
						bestD=d;
						bestBucket = bucket;
					}
				}
			}
			return bestBucket;
		}
		return null;
	}
}
