package com.marginallyclever.convenience.linecollection;

import com.marginallyclever.convenience.LineSegment2D;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Point2d;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * {@link RamerDouglasPeuckerDeque} simplifies a {@link LineCollection} using the Ramer-Douglas-Peucker algorithm
 * with a deque.  Tested to be slower than {@link RamerDouglasPeuckerRecursive}.
 */
public class RamerDouglasPeuckerDeque implements LineSimplifier {
    LineCollection original;

    public RamerDouglasPeuckerDeque(@NotNull LineCollection lineCollection) {
        original = lineCollection;
    }

    @Override
    public @NotNull LineCollection simplify(double distanceTolerance) {
        int len = original.size();
        if (len <= 2) return original;

        boolean[] usePt = new boolean[len];
        usePt[len - 1] = true;

        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{0, len - 1});

        while (!stack.isEmpty()) {
            int[] range = stack.pop();
            int i = range[0];
            int j = range[1];

            if (i + 1 == j) continue;

            LineSegment2D seg = new LineSegment2D(
                    original.get(i).start,
                    original.get(j).end, original.get(i).color);
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
                usePt[maxIndex] = true;
                stack.push(new int[]{i, maxIndex});
                stack.push(new int[]{maxIndex+1, j});
            }
        }

        LineCollection result = new LineCollection();
        Point2d head = original.getFirst().start;

        for (int i = 0; i < len; i++) {
            if (usePt[i]) {
                Point2d next = original.get(i).end;
                result.add(new LineSegment2D(head, next, original.get(i).color));
                head = next;
            }
        }

        return result;
    }
}
