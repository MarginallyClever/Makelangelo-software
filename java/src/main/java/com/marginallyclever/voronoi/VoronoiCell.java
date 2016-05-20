package com.marginallyclever.voronoi;

import java.awt.Point;

public class VoronoiCell implements Comparable<VoronoiCell> {
	public Point centroid = new Point();

	@Override
	public int compareTo(VoronoiCell arg0) {
		int y = centroid.y - arg0.centroid.y;
		if(y!=0) return y;
		
		return centroid.x - arg0.centroid.x;
	}
}
