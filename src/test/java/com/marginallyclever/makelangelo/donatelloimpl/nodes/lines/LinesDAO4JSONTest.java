package com.marginallyclever.makelangelo.donatelloimpl.nodes.lines;

import com.marginallyclever.makelangelo.turtle.Line2d;
import com.marginallyclever.makelangelo.turtle.ListOfLines;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Point2d;

public class LinesDAO4JSONTest {
    @Test
    public void testToAndFromJSON() {
        var before = new ListOfLines();
        for(int j=0;j<50;++j) {
            var line = new Line2d();
            for(int i=0;i<100;++i) {
                line.add(new Point2d(Math.random()*1000,Math.random()*1000));
            }
            before.add(line);
        }

        LinesDAO4JSON dao = new LinesDAO4JSON();
        ListOfLines after = dao.fromJSON(dao.toJSON(before));

        Assertions.assertNotSame(before, after);
        Assertions.assertEquals(before.hashCode(), after.hashCode());
        Assertions.assertEquals(before.size(), after.size());
        for(int i=0;i<after.size();++i) {
            Line2d b1 = before.get(i);
            Line2d a1 = after.get(i);
            Assertions.assertEquals(b1.size(), a1.size());
            for(int j=0;j<b1.size();++j) {
                Point2d p1 = b1.get(j);
                Point2d p2 = a1.get(j);
                Assertions.assertEquals(p1.x, p2.x);
                Assertions.assertEquals(p1.y, p2.y);
            }
        }
    }
}
