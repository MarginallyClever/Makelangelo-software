package com.marginallyclever.voronoi;

import java.awt.geom.Point2D;

import org.apache.batik.ext.awt.geom.Polygon2D;

public class VoronoiCell implements Comparable<VoronoiCell> {
	public Point2D centroid = new Point2D.Double();
	public Point2D oldCentroid = new Point2D.Double();
	public Polygon2D region = new Polygon2D();
	public double weight;

	@Override
	public int compareTo(VoronoiCell arg0) {
		int y1 = (int)Math.floor(arg0.centroid.getY()/10);
		int y0 = (int)Math.floor(centroid.getY()/10);
		if(y1 != y0) return y0-y1;
		int x1 = (int)Math.floor(arg0.centroid.getX()/10);
		int x0 = (int)Math.floor(centroid.getX()/10);
		return x1-x0;
	}
	
	public String toString() {
		return centroid.toString()+",w="+weight;
	}
}
