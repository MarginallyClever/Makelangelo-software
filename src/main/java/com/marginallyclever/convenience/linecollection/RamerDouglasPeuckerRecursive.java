package com.marginallyclever.convenience.linecollection;

import com.marginallyclever.convenience.LineSegment2D;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Point2d;

/**
 * {@link RamerDouglasPeuckerRecursive} simplifies a {@link LineCollection} using the <a
 * href="https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Ramer-Douglas-Peucker
 * algorithm</a> in a recursive fashion.
 */
public class RamerDouglasPeuckerRecursive implements LineSimplifier {
    private final LineCollection original;
    private final boolean[] keep;

    public RamerDouglasPeuckerRecursive(@NotNull LineCollection lineCollection) {
        original = lineCollection;
        keep = new boolean[original.size()];
    }

    @Override
    public @NotNull LineCollection simplify(double distanceTolerance) {
        testSegmentsAreContiguous();

        var len = original.size();
        keep[0] = true;
        keep[len-1] = true;

        try {
            simplifySection(0, len, distanceTolerance * distanceTolerance, keep);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return assembleResult();
    }

    private LineCollection assembleResult() {
        LineCollection result = new LineCollection();
        Point2d head = original.getFirst().start;

        var len = original.size();
        int count = 0;
        for (int i = 0; i < len; i++) {
            if (keep[i]) {
                count++;
                Point2d next = original.get(i).end;
                result.add(new LineSegment2D(head, next, original.get(i).color));
                head = next;
            }
        }

        if(len!=count) {
            System.out.println("simplified from " + len + " to " + count);
        }
        return result;
    }

    /**
     * Test that the segments are contiguous.
     * @throws IllegalArgumentException if the segments are not contiguous
     */
    private void testSegmentsAreContiguous() throws IllegalArgumentException {
        var len = original.size();
        // test that the end of each original line is the start of the next
        for (int i = 0; i < len-1; i++) {
            if (original.get(i).end.distanceSquared(original.get(i+1).start)>1e-4) {
                throw new IllegalArgumentException("Line segments must be contiguous.");
            }
        }
    }

    /**
     * <p>Simplify the line collection by removing points that are within distanceTolerance of the line between their neighbors.</p>
     * <p>The strategy is to split the work at the point that is farthest from the line between the start and end points,
     * then try to simplify the two halves.</p>
     * @param start the start index
     * @param end the end index
     * @param distanceToleranceSq the distance tolerance squared
     * @param usePt the array of booleans that indicates whether a point should be retained after simplification
     */
    private void simplifySection(int start, int end,double distanceToleranceSq,boolean[] usePt) {
        if ((start + 1) >= end) return; // adjacent points, nothing to simplify
        System.out.println("testing "+start+" to "+end);

        LineSegment2D seg=null;
        try {
            var a = original.get(start).start;
            var b = original.get(end-1).end;
            var c = original.get(start).color;

            seg = new LineSegment2D(a,b,c);
        } catch(Exception e) {
            e.printStackTrace();
        }

        // find the point that is farthest from the line between the start and end points.
        double maxDistanceSq = 0;
        int maxIndex = -1;
        for (int k = start+1; k < end-1; k++) {
            double distSq = seg.ptLineDistSq(original.get(k).end);
            if (distSq > maxDistanceSq) {
                maxDistanceSq = distSq;
                maxIndex = k;
            }
        }

        if (maxDistanceSq > distanceToleranceSq && maxIndex>-1) {
            // Split the work at the point of greatest inflection.
            // Keep the point and simplify the two halves.
            usePt[maxIndex] = true;
            simplifySection(start, maxIndex,distanceToleranceSq,usePt);
            simplifySection(maxIndex, end,distanceToleranceSq,usePt);
        } // else all points between start and end are within tolerance, they are already marked as false (discard).
    }
}
