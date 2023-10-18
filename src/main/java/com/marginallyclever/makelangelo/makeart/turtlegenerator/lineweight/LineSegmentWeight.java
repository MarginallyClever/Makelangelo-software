package com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight;

import com.marginallyclever.convenience.Point2D;

import javax.vecmath.Vector2d;

class LineSegmentWeight {
    public Point2D start, end;
    public int ix, iy;  // index for faster search
    public double weight;

    public LineSegmentWeight(Point2D start, Point2D end, double weight) {
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public void flip() {
        Point2D temp = end;
        end = start;
        start = temp;
    }

    public Vector2d getDelta() {
        return new Vector2d(end.x - start.x, end.y - start.y);
    }
}
