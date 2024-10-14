package com.marginallyclever.makelangelo.makeart.turtletool;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class LineStringPlus extends LineString {
    private final Point start, end;

    public LineStringPlus(LineString v) {
        super(v.getCoordinateSequence(), v.getFactory());
        start = super.getStartPoint();
        end = super.getEndPoint();
    }

    public Point getStartPoint() {
        return start;
    }

    public Point getEndPoint() {
        return end;
    }
}
