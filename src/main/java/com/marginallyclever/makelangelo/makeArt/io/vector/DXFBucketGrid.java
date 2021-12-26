package com.marginallyclever.makelangelo.makeArt.io.vector;

import org.kabeja.dxf.helpers.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Used by LoadDXF2.  A grid of DXFBuckets and convenience methods to access the buckets and their contents.
 * @author Dan Royer
 *
 */
public class DXFBucketGrid {

	private static final Logger logger = LoggerFactory.getLogger(DXFBucketGrid.class);
	
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
		
		//logger.debug("OUTER BOUNDS ("+topLeft.getX()+","+topLeft.getY()+")-("+bottomRight.getX()+","+bottomRight.getY()+")");
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
				//logger.debug(x+","+y+" = ("+b.topLeft.getX()+","+b.topLeft.getY()+")-("+b.bottomRight.getX()+","+b.bottomRight.getY()+")");
			}
		}
	}
	
	/**
	 * Add one DXFEntity to the appropriate bucket.
	 * @param e the entity
	 * @param p the point.  Could be the start or the end of the entity
	 */
	public void addEntity(DXFBucketEntity e,Point p) {
		for (DXFBucket b : buckets) {
			if (b.topLeft.getY() <= p.getY() &&
					b.topLeft.getX() <= p.getX() &&
					b.bottomRight.getY() >= p.getY() &&
					b.bottomRight.getX() >= p.getX()) {
				// inside this bucket's region
				b.push(e, p);
				return;
			}
		}
		logger.debug("Not added {}", p);
	}
	
	public void countEntitiesInBuckets() {
		int total=0;

		for (DXFBucket b : buckets) {
			if (b.contents.size() > 0) {
				logger.debug("bucket {},{} has {} entities.", b.x, b.y, b.contents.size());
				total += b.contents.size();
			}
		}
		logger.debug("{} total entities in buckets (including duplicates).", total);
	}
	
	/**
	 * dumpes everything into one bucket, probably creating duplicates.
	 * @param groups
	 */
	protected void dumpEverythingIntoABucket(List<DXFGroup> groups) {
		DXFGroup group = new DXFGroup();
		groups.add(group);

		for (DXFBucket bucket : buckets) {
			for (DXFBucketEntity dxfBucketEntity : bucket.contents) {
				group.addLast(dxfBucketEntity);
			}
		}
		//logger.debug(groups.size()+ " groups after dump.");
		//logger.debug(group.entities.size()+ " entities after dump.");
	}
	
	@Deprecated
	protected void sortEntitiesIntoContinguousGroups(List<DXFGroup> groups,double epsilon) {
		DXFBucket sourceBucket;
		Point p=null;
	
		double dx = buckets.get(buckets.size()-1).bottomRight.getX() - buckets.get(0).topLeft.getX();
		double dy = buckets.get(buckets.size()-1).bottomRight.getY() - buckets.get(0).topLeft.getY();
		double BIG_EPSILON = dx*dx+dy*dy;
		
		// as long as there are entities in buckets
		while((sourceBucket=findNonEmptyBucket(p,BIG_EPSILON))!=null) {
			do {
				// start a new group
				DXFGroup group = new DXFGroup();
				groups.add(group);
				// add the first entity found 
				DXFBucketEntity firstBe = sourceBucket.findBestFitToPoint(p,BIG_EPSILON);
				DXFBucketEntity be = firstBe;
				DXFBucket otherBucket = sourceBucket;
				DXFBucket thisBucket = sourceBucket;
	
				// Loop forward
				while(be!=null) {
					thisBucket.remove(be);
					group.addLast(be);
					// find the bucket at the far end of this entity
					otherBucket = be.getRemainingBucket();
					// find the physical end of this entity in the bucket
					p = be.getPointInBucket(otherBucket);
					// take this entity out
					otherBucket.remove(be);
					// find the best fit for an entity that shares this point
					be = otherBucket.findBestFitToPoint(p,epsilon);
					thisBucket = otherBucket;
					// until there are no more entities in this contiguous group
				}
				
				// Maybe the first line we picked is in the middle of a contiguous group.  
				// Find the start by looping backward.
				if(firstBe!=null) {
					p = firstBe.getPointInBucket(sourceBucket);
					be = sourceBucket.findBestFitToPoint(p,epsilon);
					thisBucket = sourceBucket;
					while(be!=null) {
						thisBucket.remove(be);
						if(be!=firstBe) {
							group.addFirst(be);
						}
						// find the bucket at the far end of this entity
						otherBucket = be.getRemainingBucket();
						// find the physical end of this entity in the bucket
						p = be.getPointInBucket(otherBucket);
						// take this entity out
						otherBucket.remove(be);
						// find the best fit for an entity that shares this point
						be = otherBucket.findBestFitToPoint(p,epsilon);
						// until there are no more entities in this contiguous group
						thisBucket = otherBucket;
					}
				}
				//logger.debug();
			} while(!sourceBucket.contents.isEmpty());
		}

		//logger.debug(groups.size()+ " groups after sort.");
	}
	
	/**
	 * Find the non-empty bucket nearest to point p.  If p is null, pick the first non-empty bucket.
	 * @param p
	 * @return the selected bucket
	 */
	protected DXFBucket findNonEmptyBucket(Point p,double epsilon) {
		if(p==null) {
			for (DXFBucket bucket : buckets) {
				if (!bucket.contents.isEmpty()) return bucket;
			}
		} else {
			DXFBucket bestBucket = null;
			double bestD=epsilon*epsilon;
			for (DXFBucket bucket : buckets) {
				if (!bucket.contents.isEmpty()) {
					if (bestBucket == null) {
						bestBucket = bucket;
						double dx = (bestBucket.topLeft.getX() + bestBucket.bottomRight.getX()) / 2;
						double dy = (bestBucket.topLeft.getY() + bestBucket.bottomRight.getY()) / 2;
						bestD = dx * dx + dy * dy;
					} else {
						double dx = (bestBucket.topLeft.getX() + bestBucket.bottomRight.getX()) / 2;
						double dy = (bestBucket.topLeft.getY() + bestBucket.bottomRight.getY()) / 2;
						double d = dx * dx + dy * dy;
						if (bestD > d) {
							bestD = d;
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
