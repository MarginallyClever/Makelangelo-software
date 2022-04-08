package com.marginallyclever.convenience.voronoi;

/**
 * See {@link VoronoiTesselator} for a description of the Voronoi
 */
public class VoronoiGraphEdge {
	// share edge end points
	public double x1, y1;
	public double x2, y2;

	// the index of each centroid adjacent to this edge.
	public int site1;
	public int site2;
  
	public String toString() {
		return "("+x1+","+y1+")-"+"("+x1+","+y1+") ["+site1+"/"+site2+"]";
	}
}
