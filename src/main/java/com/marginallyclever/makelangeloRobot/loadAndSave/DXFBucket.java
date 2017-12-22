package com.marginallyclever.makelangeloRobot.loadAndSave;

import org.kabeja.dxf.helpers.Point;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Used by LoadDXF2.  A DXFBucket is a collection of DXFBucketEntities, and a limited to a given volume of space.
 * @author Dan Royer
 *
 */
public class DXFBucket {
	public Point topLeft;
	public Point bottomRight;
	protected int x,y;
	protected List<DXFBucketEntity> contents;
	
	public DXFBucket(int x,int y) {
		this.topLeft = new Point();
		this.bottomRight = new Point();
		this.x=x;
		this.y=y;
		
		this.contents = new LinkedList<DXFBucketEntity>();
	}
	
	public void push(DXFBucketEntity e,Point p) {
		contents.add(e);
		e.addBucket(this,p);
	}
	
	public DXFBucketEntity getFirstEntity() {
		return contents.get(0);
	}
	
	public void remove(DXFBucketEntity o) {
		contents.remove(o);
	}

	
	public DXFBucketEntity findBestFitToPoint(Point p,double epsilon) {
		if(p==null) return getFirstEntity();
		
		double bestD = epsilon*epsilon;
		double d;
		DXFBucketEntity bestEntity=null;
		
		Iterator<DXFBucketEntity> i = contents.iterator();
		while(i.hasNext()) {
			DXFBucketEntity be = i.next();
			d = distanceBetweenPointsSquared(p,be.pointA);
			if( d < bestD) {
				bestEntity = be;
				bestD = d*d;
			}
			d = distanceBetweenPointsSquared(p,be.pointB);
			if( d < bestD) {
				bestEntity = be;
				bestD = d*d;
			}
		}
		return bestEntity;
	}

	protected double distanceBetweenPointsSquared(Point a,Point b) {
		double dx = a.getX() - b.getX();
		double dy = a.getY() - b.getY();
		return dx*dx + dy*dy;
	}
}
