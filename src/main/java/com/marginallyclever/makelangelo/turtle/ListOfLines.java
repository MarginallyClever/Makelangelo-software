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
public class ListOfLines implements ListOfPoints {
    private final List<Line2d> list = new ArrayList<>();

    public void add(Line2d line2D) {
        list.add(line2D);
    }

    @NotNull
    public List<Line2d> getAllLines() {
        return list;
    }

    public boolean hasNoLines() {
        return list.isEmpty();
    }

    @Override
    public @NotNull List<Point2d> getAllPoints() {
        List<Point2d> points = new ArrayList<>();
        for (Line2d line2D : list) {
            points.addAll(line2D.getAllPoints());
        }

        return points;
    }

    @Override
    public boolean hasNoPoints() {
        if(list.isEmpty()) return true;
        return false;
    }
}
