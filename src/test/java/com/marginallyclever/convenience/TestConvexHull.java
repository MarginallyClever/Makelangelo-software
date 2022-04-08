package com.marginallyclever.convenience;

import org.junit.jupiter.api.Test;

import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dan Royer
 * @since 2022-04-06
 */
public class TestConvexHull {
    @Test
    public void testAddingIndividuals() {
        ConvexHull hull = new ConvexHull();
        hull.add(new Vector2d(16, 3));
        hull.add(new Vector2d(12, 17));
        hull.add(new Vector2d(0, 6));
        hull.add(new Vector2d(-4, -6));
        hull.add(new Vector2d(16, 6));
        hull.add(new Vector2d(16, -7));
        hull.add(new Vector2d(16, -3));
        hull.add(new Vector2d(17, -4));
        hull.add(new Vector2d(5, 19));
        hull.add(new Vector2d(19, -8));
        hull.add(new Vector2d(3, 16));
        hull.add(new Vector2d(12, 13));
        hull.add(new Vector2d(3, -4));
        hull.add(new Vector2d(17, 5));
        hull.add(new Vector2d(-3, 15));
        hull.add(new Vector2d(-3, -9));
        hull.add(new Vector2d(0, 11));
        hull.add(new Vector2d(-9, -3));
        hull.add(new Vector2d(-4, -2));
        hull.add(new Vector2d(12, 10));
        assertEquals("ConvexHull{hull=[(-9.0, -3.0), (-3.0, -9.0), (19.0, -8.0), (17.0, 5.0), (12.0, 17.0), (5.0, 19.0), (-3.0, 15.0)]}", hull.toString());
        Vector2d center = hull.getCenterOfHull();
        assert(hull.contains(center));
        assert(hull.contains(new Vector2d(-9,-3)));
        assert(!hull.contains(new Vector2d(-10000,-10000)));
    }

    @Test
    public void testAddingCollection() {
        List<Vector2d> list = new ArrayList<>();
        list.add(new Vector2d(16, 3));
        list.add(new Vector2d(12, 17));
        list.add(new Vector2d(0, 6));
        list.add(new Vector2d(-4, -6));
        list.add(new Vector2d(16, 6));
        list.add(new Vector2d(16, -7));
        list.add(new Vector2d(16, -3));
        list.add(new Vector2d(17, -4));
        list.add(new Vector2d(5, 19));
        list.add(new Vector2d(19, -8));
        list.add(new Vector2d(3, 16));
        list.add(new Vector2d(12, 13));
        list.add(new Vector2d(3, -4));
        list.add(new Vector2d(17, 5));
        list.add(new Vector2d(-3, 15));
        list.add(new Vector2d(-3, -9));
        list.add(new Vector2d(0, 11));
        list.add(new Vector2d(-9, -3));
        list.add(new Vector2d(-4, -2));
        list.add(new Vector2d(12, 10));

        ConvexHull hull = new ConvexHull();
        hull.set(list);

        assertEquals("ConvexHull{hull=[(-9.0, -3.0), (-3.0, -9.0), (19.0, -8.0), (17.0, 5.0), (12.0, 17.0), (5.0, 19.0), (-3.0, 15.0)]}", hull.toString());
        Vector2d center = hull.getCenterOfHull();
        assert(hull.contains(center));
        assert(hull.contains(new Vector2d(-9,-3)));
        assert(!hull.contains(new Vector2d(-10000,-10000)));
    }
}
