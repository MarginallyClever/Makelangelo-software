package com.marginallyclever.makelangelo.turtle;

import org.jetbrains.annotations.Contract;

/**
 * A {@link Line2d} is a series of connected 2D points with double precision.
 */
public class Line2d extends ConcreteListOfPoints {
    public Line2d() {
        super();
    }

    public Line2d(Line2d arg0) {
        super(arg0);
    }

    @Contract(pure = true)
    public boolean isClosed(double epsilon) {
        var points = getAllPoints();
        if(points.size()<2) return false;

        var first = points.getFirst();
        var last = points.getLast();
        if(first == last) return true;  // same instance, must match
        return first.distanceSquared(last) < epsilon*epsilon;
    }

    @Contract(pure = true)
    public boolean isClosed() {
        return isClosed(1e-6);
    }
}
