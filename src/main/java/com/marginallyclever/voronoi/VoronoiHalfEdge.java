package com.marginallyclever.voronoi;


public class VoronoiHalfEdge {
  public VoronoiHalfEdge ELleft, ELright;
  public VoronoiEdge ELedge;
  public boolean deleted;
  public int ELpm;
  public VoronoiSite vertex;
  public double ystar;
  public VoronoiHalfEdge PQnext;

  public VoronoiHalfEdge() {
    PQnext = null;
  }
}
