package com.marginallyclever.makelangelo.turtle;

import org.jetbrains.annotations.NotNull;

import javax.vecmath.Point2d;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of 2D points with double precision.
 */
public class ConcreteListOfPoints extends ArrayList<Point2d> implements ListOfPoints {
    public ConcreteListOfPoints() {}

    public ConcreteListOfPoints(ConcreteListOfPoints arg0) {
        super();

        for(Point2d point : arg0) {
            this.add(new Point2d(point));
        }
    }

    @Override
    public @NotNull List<Point2d> getAllPoints() {
        return this;
    }

    @Override
    public boolean hasNoPoints() {
        return isEmpty();
    }
}
