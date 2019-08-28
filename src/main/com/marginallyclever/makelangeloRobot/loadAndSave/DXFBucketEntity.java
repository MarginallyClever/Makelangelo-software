package com.marginallyclever.makelangeloRobot.loadAndSave;

import org.kabeja.dxf.helpers.Point;

import org.kabeja.dxf.DXFEntity;

/**
 * A DXFBucketEntity has two end points.  Each end point is in a bucket.  They may be the same bucket. 
 * @author Dan Royer
 *
 */
public class DXFBucketEntity {
	public DXFEntity entity;
	public DXFBucketPointPair a,b;
	
	public DXFBucketEntity(DXFEntity entity) {
		this.entity=entity;
	}

	public void addBucket(DXFBucket bucket,Point p) {
		     if(a==null) a=new DXFBucketPointPair(bucket,p);
		else if(b==null) b=new DXFBucketPointPair(bucket,p);
		else {
			// should never get here
			System.out.println("addBucket()");
			assert(false);
		}
	}
	public void removeBucket(DXFBucket match) {
		if(a.bucket==match) a=null;
		if(b.bucket==match) b=null;
	}

	public DXFBucketPointPair getRemainingBucketPointPair() {
		if(a!=null) {
			DXFBucketPointPair c=a;
			a=null;
			return c;
		}
		if(b!=null) {
			DXFBucketPointPair c=b;
			a=null;
			return c;
		}
		// should never reach this point
		System.out.println("getRemainingBucket()");
		assert(false);
		return null;
	}
	
	public Point getPointInBucket(DXFBucket match) {
		if(a.bucket==match) return a.point;
		if(b.bucket==match) return b.point;
		// should never reach this point
		System.out.println("getPointInBucket()");
		assert(false);
		return null;
	}
}
