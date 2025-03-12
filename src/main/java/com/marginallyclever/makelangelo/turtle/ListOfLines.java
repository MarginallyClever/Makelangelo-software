package com.marginallyclever.makelangelo.turtle;

import org.jetbrains.annotations.NotNull;

import javax.vecmath.Point2d;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A list of 2D line segments.  The intent here is that the points in each LineSegment are the same instances
 * as the points in the ListOfPoints.  This way tools that work on a ListOfPoints will also work on a ListOfLines.
 * The trouble is keeping the two lists in sync.</p>
 */
public class ListOfLines extends ArrayList<Line2d> implements ListOfPoints {
    @NotNull
    public List<Line2d> getAllLines() {
        return this;
    }

    public boolean hasNoLines() {
        return isEmpty();
    }

    @Override
    public @NotNull List<Point2d> getAllPoints() {
        List<Point2d> points = new ArrayList<>();
        for (Line2d line2D : this) {
            points.addAll(line2D.getAllPoints());
        }

        return points;
    }

    @Override
    public boolean hasNoPoints() {
        if(this.isEmpty()) return true;
        for (Line2d line2D : this) {
            if(!line2D.hasNoPoints()) return false;
        }
        return true;
    }
}
