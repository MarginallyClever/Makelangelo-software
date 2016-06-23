package com.marginallyclever.loaders;

import org.kabeja.dxf.helpers.Point;

import org.kabeja.dxf.DXFEntity;

public class DXFBucketEntity {
	public DXFEntity entity;
	public DXFBucket bucketA,bucketB;
	public Point pointA,pointB;
	
	public DXFBucketEntity(DXFEntity entity) {
		this.entity=entity;
	}

	public void addBucket(DXFBucket bucket,Point p) {
		if(bucketA==null) {
			bucketA = bucket;
			pointA = p;
		} else if(bucketB==null) {
			bucketB = bucket;
			pointB = p;
		} else {
			// should never get here
			assert(false);
		}
	}

	public DXFBucket getRemainingBucket() {
		if(bucketA!=null) return bucketA;
		else if(bucketB!=null) return bucketB;
		// should never reach this point
		assert(false);
		return null;
	}
	
	public Point getPointInBucket(DXFBucket match) {
		if(bucketA==match) return pointA;
		else if(bucketB==match) return pointB;
		// should never reach this point
		assert(false);
		return null;
	}
}
