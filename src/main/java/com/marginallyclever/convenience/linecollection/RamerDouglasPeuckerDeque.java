package com.marginallyclever.convenience.linecollection;

import com.marginallyclever.convenience.LineSegment2D;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Point2d;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * {@link RamerDouglasPeuckerDeque} simplifies a {@link LineCollection} using the Ramer-Douglas-Peucker algorithm
 * with a deque.  Tested to be slower than {@link RamerDouglasPeuckerRecursive}.
 */
public class RamerDouglasPeuckerDeque implements LineSimplifier {
    private final double CONTIGUOUS_TOLERANCE = 1e-4;

    private final List<Point2d> points = new ArrayList<>();
    private final boolean[] keep;
    private final Color c;

    public RamerDouglasPeuckerDeque(@NotNull LineCollection lineCollection) {
        testSegmentsAreContiguousAndSameColor(lineCollection);

        c = lineCollection.getFirst().color;
        points.add(lineCollection.getFirst().start);
        for (var line : lineCollection) {
            points.add(line.end);
        }
        keep = new boolean[points.size()];
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

    @Override
    public @NotNull LineCollection simplify(double distanceTolerance) {
        var len = points.size();
        keep[0] = true;
        keep[len-1] = true;

        var distanceToleranceSq = distanceTolerance * distanceTolerance;

        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{0, len - 1});

        while (!stack.isEmpty()) {
            int[] range = stack.pop();
            int start = range[0];
            int end = range[1];

            if (start + 1 == end) continue;

            var a = points.get(start);
            var b = points.get(end);
            double maxDistance = 0;
            int maxIndex = start;

            for (int k = start + 1; k < end; k++) {
                var p = points.get(k);
                double distance =  java.awt.geom.Line2D.ptLineDistSq(a.x,a.y, b.x,b.y, p.x,p.y);
                if (distance > maxDistance) {
                    maxDistance = distance;
                    maxIndex = k;
                }
            }

            if (maxDistance > distanceTolerance) {
                keep[maxIndex] = true;
                stack.push(new int[]{start, maxIndex});
                stack.push(new int[]{maxIndex, end});
            }
        }

        return assembleResult();
    }

    private @NotNull LineCollection assembleResult() {
        LineCollection result = new LineCollection();
        var head = points.getFirst();

        var len = points.size();
        for (int i = 1; i < len; i++) {
            if (keep[i]) {
                var next = points.get(i);
                result.add(new LineSegment2D(head, next, c));
                head = next;
            }
        }

        return result;
    }
}
