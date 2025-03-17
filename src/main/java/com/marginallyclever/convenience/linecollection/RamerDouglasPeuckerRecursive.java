package com.marginallyclever.convenience.linecollection;

import com.marginallyclever.convenience.LineSegment2D;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Point2d;

/**
 * {@link RamerDouglasPeuckerRecursive} simplifies a {@link LineCollection} using the Ramer-Douglas-Peucker algorithm
 * in a recursive fashion.
 */
public class RamerDouglasPeuckerRecursive implements LineSimplifier {
    private final LineCollection original;
    private final boolean[] usePt;

    public RamerDouglasPeuckerRecursive(@NotNull LineCollection lineCollection) {
        original = lineCollection;
        usePt = new boolean[original.size()];
    }

    @Override
    public @NotNull LineCollection simplify(double distanceTolerance) {
        var len = original.size();
        usePt[len-1] = true;

        simplifySection(0, len - 1, distanceTolerance, usePt);

        LineCollection result = new LineCollection();
        Point2d head = original.getFirst().start;

        for (int i = 0; i < len; i++) {
            if (usePt[i]) {
                Point2d next = original.get(i).end;
                result.add(new LineSegment2D(head,next,original.get(i).color));
                head=next;
            }
        }

        return result;
    }

    /**
     * Simplify the line collection by removing points that are within distanceTolerance of the line between their neighbors.
     * The strategy is to split the work at the point that is farthest from the line between the start and end points,
     * then try to simplify the two halves.
     * @param i the start index
     * @param j the end index
     * @param distanceTolerance the distance tolerance
     * @param usePt the array of booleans that indicates whether a point should be retained after simplification
     */
    private void simplifySection(int i, int j,double distanceTolerance,boolean[] usePt) {
        if ((i + 1) == j) return; // no points to simplify

        LineSegment2D seg = new LineSegment2D(
                original.get(i).start,
                original.get(j).end,
                original.get(i).color);
        double maxDistance = 0;
        int maxIndex = i;
        for (int k = i + 1; k < j; k++) {
            double distance = seg.ptLineDistSq(original.get(k).end);
            if (distance > maxDistance) {
                maxDistance = distance;
                maxIndex = k;
            }
        }
        if (maxDistance > distanceTolerance) {
            // split the work at the point that is farthest from the line between the start and end points.
            // keep the point and simplify the two halves.
            usePt[maxIndex] = true;
            simplifySection(i, maxIndex,distanceTolerance,usePt);
            simplifySection(maxIndex+1, j,distanceTolerance,usePt);
        }
    }
}
