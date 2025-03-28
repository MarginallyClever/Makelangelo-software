package com.marginallyclever.makelangelo.turtle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Point2d;
import java.util.List;

public interface ListOfPoints {
    @NotNull List<Point2d> getAllPoints();

    @Contract(pure = true)
    boolean hasNoPoints();
}
