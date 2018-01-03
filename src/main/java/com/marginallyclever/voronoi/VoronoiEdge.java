package com.marginallyclever.voronoi;


public class VoronoiEdge {
  public double a = 0, b = 0, c = 0;
  public VoronoiSite[] ep;  // JH: End points?
  public VoronoiSite[] reg; // JH: Sites this edge bisects?
  public int edgenbr;

  public VoronoiEdge() {
    ep = new VoronoiSite[2];
    reg = new VoronoiSite[2];
  }
}
