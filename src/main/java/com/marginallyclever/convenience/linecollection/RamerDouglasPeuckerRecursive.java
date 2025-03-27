package com.marginallyclever.convenience.linecollection;

import com.marginallyclever.convenience.LineSegment2D;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Point2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>{@link RamerDouglasPeuckerRecursive} simplifies a {@link LineCollection} using the <a
 * href="https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Ramer-Douglas-Peucker
 * algorithm</a> in a recursive fashion.</p>
 * <p>Requires all points are contiguous and of the same color.</p>
 */
public class RamerDouglasPeuckerRecursive implements LineSimplifier {
    private final double CONTIGUOUS_TOLERANCE = 1e-4;

    private final List<Point2d> points = new ArrayList<>();
    private final boolean[] keep;
    private final Color c;
    private double distanceToleranceSq;

    /**
     * Construct a new {@link RamerDouglasPeuckerRecursive} object.
     * @param lineCollection the line collection to simplify
     * @throws IllegalArgumentException if the segments are not contiguous
     */
    public RamerDouglasPeuckerRecursive(@NotNull LineCollection lineCollection) throws IllegalArgumentException {
        testSegmentsAreContiguousAndSameColor(lineCollection);

        c = lineCollection.getFirst().color;
        points.add(lineCollection.getFirst().start);
        for (var line : lineCollection) {
            points.add(line.end);
        }
        keep = new boolean[points.size()];
    }

    @Override
    public @NotNull LineCollection simplify(double distanceTolerance) {
        var len = points.size();
        keep[0] = true;
        keep[len-1] = true;

        distanceToleranceSq = distanceTolerance * distanceTolerance;

        try {
            if(lineIsClosedLoop()) {
                // if this is a closed loop the ptLineDistSq test will fail, so we need the first point
                // and the point as far from first point as possible, then test in two halves.
                int furthestIndex = getFurthestPointFromStart();
                // if the furthest point is the first or last point, we can't simplify.
                if(furthestIndex!=0 && furthestIndex!=len-1) {
                    simplifySection(0,furthestIndex);
                    simplifySection(furthestIndex,len-1);
                }
            } else {
                simplifySection(0, len - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return assembleResult();
    }

    private int getFurthestPointFromStart() {
        var a = points.getFirst();
        double maxDistanceSq = 0;
        int maxIndex = 0;
        for (int k = 1; k < points.size()-1; k++) {
            double distSq = a.distanceSquared(points.get(k));
            if (distSq > maxDistanceSq) {
                maxDistanceSq = distSq;
                maxIndex = k;
            }
        }
        return maxIndex;
    }

    private boolean lineIsClosedLoop() {
        return points.getFirst().distanceSquared(points.getLast()) < CONTIGUOUS_TOLERANCE;
    }

    private @NotNull LineCollection assembleResult() {
        LineCollection result = new LineCollection();
        var head = points.getFirst();

        var len = points.size();
        for (int i = 0; i < len; i++) {
            if (keep[i]) {
                var next = points.get(i);
                result.add(new LineSegment2D(head, next, c));
                head = next;
            }
        }

        return result;
    }

    /**
     * Test that the segments are contiguous.
     * @throws IllegalArgumentException if the segments are not contiguous
     */
    private void testSegmentsAreContiguousAndSameColor(LineCollection list) throws IllegalArgumentException {
        var len = list.size();
        if(len<2) return;

        var first = list.getFirst();
        Color firstColor = first.color;
        // test that the end of each original line is the start of the next
        for (int i = 0; i < len-1; i++) {
            var next = list.get(i+1);
            if( first.end.distanceSquared(next.start) > CONTIGUOUS_TOLERANCE ) {
                throw new IllegalArgumentException("Line segments must be contiguous.");
            }
            if( !firstColor.equals(next.color) ) {
                throw new IllegalArgumentException("Line segments must be the same color.");
            }
            first = next;
        }
    }

    /**
     * <p>Simplify the line collection by removing points that are within distanceTolerance of the line between their neighbors.</p>
     * <p>The strategy is to split the work at the point that is farthest from the line between the start and end points,
     * then try to simplify the two halves.</p>
     * @param start the start index
     * @param end the end index
     */
    private void simplifySection(int start, int end) {
        if(start + 1 >= end) return; // adjacent points, nothing to simplify

        var a = points.get(start);
        var b = points.get(end);

        // find the point that is farthest from the line between the start and end points.
        double maxDistanceSq = 0;
        int maxIndex = -1;
        for (int k = start+1; k < end; k++) {
            var p = points.get(k);
            double distSq = java.awt.geom.Line2D.ptLineDistSq(a.x,a.y, b.x,b.y, p.x,p.y);
            if (distSq > maxDistanceSq) {
                maxDistanceSq = distSq;
                maxIndex = k;
            }
        }

        if (maxDistanceSq > distanceToleranceSq && maxIndex>-1) {
            // Split the work at the point of greatest inflection.
            // Keep the point and simplify the two halves.
            keep[maxIndex] = true;
            simplifySection(start, maxIndex);
            simplifySection(maxIndex, end);
        } // else all points between start and end are within tolerance, they are already marked as false (discard).
    }
}
