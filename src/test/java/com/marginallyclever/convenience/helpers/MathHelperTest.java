package com.marginallyclever.convenience.helpers;

import org.junit.jupiter.api.Test;

import javax.vecmath.Point2d;

public class MathHelperTest {
    @Test
    void testBetween() {
        Point2d a = new Point2d(0,0);
        Point2d b = new Point2d(1,0);
        Point2d c = new Point2d(0.5,0);
        double epsilon = 1e-9;

        assert(MathHelper.between(a,b,c,epsilon));

        for(int i=0;i<50;++i) {
            a.set(Math.random()*500-250, Math.random()*500-250);
            b.set(Math.random()*500-250, Math.random()*500-250);
            c = MathHelper.lerp(a,b,Math.random());
            assert(MathHelper.between(a,b,c,epsilon));
        }
    }
}
