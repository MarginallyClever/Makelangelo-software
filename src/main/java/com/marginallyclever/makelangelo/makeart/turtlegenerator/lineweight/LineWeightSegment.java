package com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight;



import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * Many segments make up a {@link LineWeight}.
 * @author Dan Royer
 */
public class LineWeightSegment {
    public Point2d start, end;
    public int ix, iy;  // index for faster search
    public double weight;

    public LineWeightSegment(Point2d start, Point2d end, double weight) {
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public void flip() {
        Point2d temp = end;
        end = start;
        start = temp;
    }

    public Vector2d getUnit() {
        Vector2d n = new Vector2d(end.x - start.x, end.y - start.y);
        n.normalize();
        return n;
    }
}
