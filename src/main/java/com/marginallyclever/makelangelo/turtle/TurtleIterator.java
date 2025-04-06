package com.marginallyclever.makelangelo.turtle;

import javax.annotation.Nonnull;
import javax.vecmath.Point2d;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link TurtleIterator} iterates over the points in a {@link Turtle}.
 */
public class TurtleIterator implements Iterator<Point2d> {
    private final Turtle turtle;
    private int layerIndex = 0;
    private int lineIndex = 0;
    private int pointIndex = 0;
    private final int max;
    private int sum = 0;
    private Point2d lastPoint=null;

    public TurtleIterator(@Nonnull Turtle turtle) {
        this.turtle = turtle;
        this.max = turtle.countPoints();
    }

    @Override
    public boolean hasNext() {
        return sum < max;
    }

    @Override
    public Point2d next() {
        var allLayers = turtle.getLayers();
        while (layerIndex < allLayers.size()) {
            var layer = allLayers.get(layerIndex);
            while(lineIndex < layer.getAllLines().size()) {
                var line = layer.getAllLines().get(lineIndex);
                if(pointIndex < line.getAllPoints().size()) {
                    sum++;
                    lastPoint = line.getAllPoints().get(pointIndex++);
                    return lastPoint;
                }
                lineIndex++;
                pointIndex = 0;
            }
            layerIndex++;
            lineIndex = 0;
        }
        throw new NoSuchElementException("No more points in the Turtle.");
    }

    public Point2d getPoint() {
        if (pointIndex >= 0) {
            return getLayer().getAllPoints().get(pointIndex);
        } else {
            throw new IllegalStateException("No point available. Call next() first.");
        }
    }

    public StrokeLayer getLayer() {
        return turtle.getLayers().get(layerIndex);
    }

    public Line2d getLine() {
        return getLayer().getAllLines().get(lineIndex);
    }

    /**
     * @return true if the point index is zero, which means it is the start of a line, which means a travel must have
     * preceded it.
     */
    public boolean isTravel() {
        return pointIndex==1;
    }

    /**
     * @return true if the current line index is zero, which means it is the start of a layer, which means a tool
     * change must have preceded it.
     */
    public boolean isToolChange() {
        return lineIndex==0 && pointIndex==1;
    }
}
