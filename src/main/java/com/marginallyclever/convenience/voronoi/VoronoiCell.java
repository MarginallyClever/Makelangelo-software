package com.marginallyclever.convenience.voronoi;

import com.marginallyclever.convenience.Point2D;

public class VoronoiCell {
    public final Point2D center = new Point2D();
    public double weight;

    public VoronoiCell(double x,double y) {
        super();
        this.center.set(x,y);
    }
}
