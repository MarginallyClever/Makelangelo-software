package com.marginallyclever.convenience;

import org.junit.jupiter.api.Test;

import javax.vecmath.Point2d;
import java.awt.*;

public class LineCollectionTest {
    @Test
    void testSplitByColor() {
        LineCollection lc = new LineCollection();
        lc.add(new LineSegment2D(new Point2d(0, 0), new Point2d(1, 1), Color.BLACK));
        lc.add(new LineSegment2D(new Point2d(1, 1), new Point2d(2, 2), Color.BLACK));
        lc.add(new LineSegment2D(new Point2d(2, 2), new Point2d(3, 3), Color.RED));
        lc.add(new LineSegment2D(new Point2d(3, 3), new Point2d(4, 4), Color.RED));
        var list = lc.splitByColor();
        assert list.size() == 2;
    }

    @Test
    void testSimplify() {
        // lines that should not merge.
        LineCollection lc2 = new LineCollection();
        lc2.add(new LineSegment2D(new Point2d(3, 3), new Point2d(4, 4), Color.RED));
        lc2.add(new LineSegment2D(new Point2d(4, 4), new Point2d(5, 4), Color.RED));
        var list2 = lc2.simplify(1e-6);
        assert list2.size() == 2;

        LineCollection lc1 = new LineCollection();
        // lines that should merge
        lc1.add(new LineSegment2D(new Point2d(0, 0), new Point2d(1, 1), Color.BLACK));
        lc1.add(new LineSegment2D(new Point2d(1, 1), new Point2d(2, 2), Color.BLACK));
        lc1.add(new LineSegment2D(new Point2d(2, 2), new Point2d(3, 3), Color.BLACK));
        lc2.add(new LineSegment2D(new Point2d(3, 3), new Point2d(4, 4), Color.BLACK));
        lc2.add(new LineSegment2D(new Point2d(4, 4), new Point2d(5, 5), Color.BLACK));
        var list1 = lc1.simplify(1e-6);
        assert list1.size() == 1;
    }
}
