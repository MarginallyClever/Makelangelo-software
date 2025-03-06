package com.marginallyclever.makelangelo.turtle;

import org.jetbrains.annotations.NotNull;

import javax.vecmath.Point2d;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of 2D points with double precision.
 */
public class ConcreteListOfPoints implements ListOfPoints {
    private final List<Point2d> list = new ArrayList<>();

    public void add(Point2d point2d) {
        list.add(point2d);
    }

    @Override
    public @NotNull List<Point2d> getAllPoints() {
        return list;
    }

    @Override
    public boolean hasNoPoints() {
        return list.isEmpty();
    }
}
