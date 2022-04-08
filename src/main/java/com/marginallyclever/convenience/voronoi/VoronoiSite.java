package com.marginallyclever.convenience.voronoi;

import java.awt.Point;

/**
 * See {@link VoronoiTesselator} for a description of the Voronoi
 */
public class VoronoiSite {
  public Point coord;
  public int sitenbr;

  public VoronoiSite() {
    coord = new Point();
  }
}
