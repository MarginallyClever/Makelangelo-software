package com.marginallyclever.convenience.voronoi;

import org.locationtech.jts.geom.Coordinate;

public class VoronoiCell {
    public final Coordinate center = new Coordinate();
    public double weight;
    public double change;

    public VoronoiCell(double x,double y) {
        super();
        set(x,y);
    }

    public void set(double x, double y) {
        this.center.x=x;
        this.center.y=y;
    }
}
