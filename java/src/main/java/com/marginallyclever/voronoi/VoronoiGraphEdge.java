package com.marginallyclever.voronoi;

public class VoronoiGraphEdge {
	public double x1, y1, x2, y2;

	public int site1;
	public int site2;
  
	public String toString() {
		return "("+x1+","+y1+")-"+"("+x1+","+y1+") ["+site1+"/"+site2+"]";
	}
}
