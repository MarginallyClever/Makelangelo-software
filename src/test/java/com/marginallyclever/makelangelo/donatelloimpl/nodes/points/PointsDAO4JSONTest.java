package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.makelangelo.turtle.ConcreteListOfPoints;
import com.marginallyclever.makelangelo.turtle.ListOfPoints;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Point2d;

public class PointsDAO4JSONTest {
    @Test
    public void testToAndFromJSON() {
        ConcreteListOfPoints before = new ConcreteListOfPoints();
        for(int i=0;i<100;++i) {
            before.add(new Point2d(Math.random()*1000,Math.random()*1000));
        }

        PointsDAO4JSON dao = new PointsDAO4JSON();
        ListOfPoints after = dao.fromJSON(dao.toJSON(before));

        Assertions.assertNotSame(before, after);
        Assertions.assertEquals(before.hashCode(), after.hashCode());
        var list2 = after.getAllPoints();
        assert(before.size() == list2.size());
        for(int i=0;i<before.size();++i) {
            Point2d p1 = before.get(i);
            Point2d p2 = list2.get(i);
            Assertions.assertEquals(p1.x, p2.x);
            Assertions.assertEquals(p1.y, p2.y);
        }
    }
}
