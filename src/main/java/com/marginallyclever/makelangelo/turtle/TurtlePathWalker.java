package com.marginallyclever.makelangelo.turtle;



import javax.vecmath.Point2d;
import java.util.Iterator;

/**
 * Walks the path of a {@link Turtle} more efficiently than the old <code>interpolate(double)</code> method.
 */
public class TurtlePathWalker {
    private final Iterator<TurtleMove> iterator;
    private TurtleMove prev;
    private TurtleMove m;
    private final double drawDistance;
    private double tSum;
    private double segmentDistance;
    private double segmentDistanceSum;

    public TurtlePathWalker(Turtle turtle) {
        iterator = turtle.history.iterator();
        prev = new TurtleMove(0, 0, MovementType.TRAVEL);
        drawDistance = turtle.getDrawDistance();
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
     * Advance along the drawn portion of the {@link Turtle} path by the given relative distance.
     * @param distance the relative distance to move
     * @return the new position of the turtle
     * @throws IllegalArgumentException if distance is negative.
     */
    public Point2d walk(double distance) {
        if(distance<0) throw new IllegalArgumentException("distance must be positive");

        tSum+=distance;
        while (segmentDistanceSum <= tSum ) {
            if (segmentDistanceSum+segmentDistance>=tSum) {
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
                if(m==null) break;
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
