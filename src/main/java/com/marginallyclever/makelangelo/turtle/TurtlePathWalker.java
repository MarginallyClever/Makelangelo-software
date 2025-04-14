package com.marginallyclever.makelangelo.turtle;

import javax.annotation.Nonnull;
import javax.vecmath.Point2d;

/**
 * Walks the path of a {@link Turtle} more efficiently than the old <code>interpolate(double)</code> method.
 */
public class TurtlePathWalker {
    private final TurtleIterator iter;
    private Point2d prev = new Point2d(0,0);
    private Point2d m;
    private final double drawDistance;
    private double tSum;
    private double segmentDistance;
    private double segmentDistanceSum;

    public TurtlePathWalker(@Nonnull Turtle turtle) {
        if (turtle == null) throw new IllegalArgumentException("Turtle cannot be null");
        this.iter = turtle.getIterator();
        drawDistance = turtle.getDrawDistance();
        tSum = 0;
        segmentDistanceSum = 0;
        advance();
    }

    /**
     * Advance to the next movement that is a draw command.
     */
    private void advance() {
        if(!iter.hasNext()) {
            m = null;
            // No more points
            segmentDistance = 0;
            return;
        }

        m = iter.next();
        if (m != null) {
            double dx = m.x - prev.x;
            double dy = m.y - prev.y;
            segmentDistance = Math.sqrt(dx * dx + dy * dy);
        }
    }

    /**
     * Advance along the drawn portion of the {@link Turtle} path by the given relative distance.
     * @param distance the relative distance to move
     * @return the new position of the turtle
     * @throws IllegalArgumentException if distance is negative.
     */
    public Point2d walk(double distance) {
        if( distance < 0) throw new IllegalArgumentException("distance must be positive");

        tSum += distance;
        while (segmentDistanceSum <= tSum ) {
            if (segmentDistanceSum + segmentDistance >= tSum) {
                double dx = m.x - prev.x;
                double dy = m.y - prev.y;
                double ratio = Math.max(Math.min((tSum - segmentDistanceSum) / segmentDistance,1),0);
                double newX = prev.x + ratio * dx;
                double newY = prev.y + ratio * dy;
                return new Point2d(newX, newY);
            } else {
                segmentDistanceSum += segmentDistance;
                prev = m;
                advance();
                if(m == null) break;
            }
        }
        return new Point2d(prev.x, prev.y);
    }

    public boolean isDone() {
        return drawDistance <= tSum;
    }

    /**
     * @return the distanced travelled so far.
     */
    public double getTSum() {
        return tSum;
    }

    /**
     * @return the total distance of the path.
     */
    public double getDrawDistance() {
        return drawDistance;
    }
}
