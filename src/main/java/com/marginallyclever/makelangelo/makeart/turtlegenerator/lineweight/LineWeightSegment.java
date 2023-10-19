package com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight;

import com.marginallyclever.convenience.Point2D;

import javax.vecmath.Vector2d;

/**
 * Many segments make up a {@link LineWeight}.
 * @author Dan Royer
 */
class LineWeightSegment {
    public Point2D start, end;
    public int ix, iy;  // index for faster search
    public double weight;

    public LineWeightSegment(Point2D start, Point2D end, double weight) {
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public void flip() {
        Point2D temp = end;
        end = start;
        start = temp;
    }

    public Vector2d getUnit() {
        Vector2d n = new Vector2d(end.x - start.x, end.y - start.y);
        n.normalize();
        return n;
    }
}
