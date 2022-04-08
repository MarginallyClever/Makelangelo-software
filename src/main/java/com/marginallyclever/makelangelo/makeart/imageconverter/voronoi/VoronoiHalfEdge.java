package com.marginallyclever.makelangelo.makeart.imageconverter.voronoi;


/**
 * See {@link VoronoiTesselator} for a description of the Voronoi
 */
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
