package com.marginallyclever.makelangelo.turtle;

import com.marginallyclever.convenience.Point2D;

import java.util.Iterator;

/**
 * Walks a turtle along a path more efficiently than using {@link Turtle#interpolate(double)}.
 */
public class TurtlePathWalker {
    private final Iterator<TurtleMove> iterator;
    private TurtleMove prev;
    private TurtleMove m;
    private final double totalDistance;
    private double tSum;
    private double segmentDistance;
    private double segmentDistanceSum;

    public TurtlePathWalker(Turtle turtle) {
        iterator = turtle.history.iterator();
        prev = new TurtleMove(0, 0, MovementType.TRAVEL);
        totalDistance = turtle.getDrawDistance();
        tSum = 0;
        segmentDistanceSum = 0;
        advance();
    }

    /**
     * Advance to the next movement that is a draw command.
     */
    private void advance() {
        while (iterator.hasNext()) {
            m = iterator.next();
            if (m.type == MovementType.DRAW_LINE) {
                double dx = m.x - prev.x;
                double dy = m.y - prev.y;
                segmentDistance = Math.sqrt(dx*dx+dy*dy);
                return;
            } else if(m.type == MovementType.TRAVEL) {
                prev = m;
            }
        }
        // in case we run out of moves
        m = null;
        segmentDistance = 0;
    }

    /**
     * Walk the turtle along the path by the given distance.
     * @param distance the distance to walk
     * @return the new position of the turtle
     */
    public Point2D walk(double distance) {
        tSum+=distance;
        while (segmentDistanceSum < tSum ) {
            if (segmentDistanceSum+segmentDistance>=tSum) {
                double dx = m.x - prev.x;
                double dy = m.y - prev.y;
                double ratio = Math.max(Math.min((tSum - segmentDistanceSum) / segmentDistance,1),0);
                double newX = prev.x + ratio * dx;
                double newY = prev.y + ratio * dy;
                return new Point2D(newX, newY);
            } else {
                segmentDistanceSum += segmentDistance;
                prev = m;
                advance();
                if(m==null) break;
            }
        }
        return new Point2D(prev.x, prev.y);
    }

    public boolean isDone() {
        return totalDistance <= tSum;
    }

    public double getTSum() {
        return tSum;
    }
}
