package com.marginallyclever.makelangelo.makeart.imageconverter.voronoi;

import com.marginallyclever.convenience.ConvexHull;
import com.marginallyclever.convenience.Point2D;

import javax.vecmath.Vector2d;
import java.awt.geom.Rectangle2D;


public class VoronoiCell implements Comparable<VoronoiCell> {
	public ConvexHull convexHull = new ConvexHull();
	public Point2D centroid = new Point2D();
	public double weight;
	public double wx, wy;
	public int hits;
	
	@Override
	public int compareTo(VoronoiCell arg0) {
		double y1 = arg0.centroid.y;
		double y0 = centroid.y;
		int c = Double.compare(y0, y1);
		if( c == 0 ) {
			double x1 = arg0.centroid.x;
			double x0 = centroid.x;
			c = Double.compare(x0, x1);
		}
		return c;
	}
	
	public String toString() {
		return centroid.toString()+",w="+weight;
	}
	
	public void clear() {
		convexHull.clear();
		weight=0;
		hits=0;
		wx=0;
		wy=0;
	}
	
	public void addPoint(double x,double y) {
		convexHull.add(new Vector2d(x,y));
	}

	public boolean contains(double x,double y) {
		return convexHull.contains(new Vector2d(x,y));
	}

	public Rectangle2D getBounds() {
		return convexHull.getBounds();
	}

	public void addWeight(double x,double y,double weight) {
		this.hits++;
		this.weight += weight;
		this.wx += x * weight;
		this.wy += y * weight;
	}

	public void scaleByWeight() {
		if (hits>0 && weight > 0) {
			wx /= weight;
			wy /= weight;
			weight/=hits;
		} else {
			weight=1;
			hits=1;
			wx = centroid.x;
			wy = centroid.y;
		}
	}
}
