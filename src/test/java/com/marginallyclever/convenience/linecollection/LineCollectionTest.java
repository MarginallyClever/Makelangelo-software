package com.marginallyclever.convenience.linecollection;

import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.makelangelo.makeart.io.LoadHelper;
import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.vecmath.Point2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class LineCollectionTest {
    @Test
    @Disabled
    public void compareSpeedSplitByColor() {
        // make a large LineCollection
        LineCollection before = new LineCollection();
        for (int i = 0; i < 1000000; i++) {
            before.add(new LineSegment2D(randomPoint(), randomPoint(), randomColor(i)));
        }

        // compare splitByColor and splitByColor2
        var start1 = System.currentTimeMillis();
        var after1 = before.splitByColor();
        var end1 = System.currentTimeMillis();
        System.out.println("splitByColor: " + (end1 - start1) + "ms");

        var start2 = System.currentTimeMillis();
        var after2 = splitByColor2(before);
        var end2 = System.currentTimeMillis();
        System.out.println("splitByColor2: " + (end2 - start2) + "ms");
    }

    public List<LineCollection> splitByColor2(LineCollection before) {
        if(before.isEmpty()) return new ArrayList<>();

        var map = new HashMap<Color,LineCollection>();
        before.parallelStream().forEach(line -> {
            if(map.containsKey(line.color)) {
                map.get(line.color).add(line);
            } else {
                LineCollection c = new LineCollection();
                c.add(line);
                map.put(line.color,c);
            }
        });
        return new ArrayList<>(map.values());
    }

    private Point2d randomPoint() {
        return new Point2d(
                Math.random()*1000,
                Math.random()*1000);
    }

    private Color randomColor(int i) {
        return switch(i) {
            case 1 -> Color.RED;
            case 2 -> Color.GREEN;
            case 3 -> Color.BLUE;
            default -> Color.BLACK;
        };
    }

    // make a LineCollection with points in a line.  confirm the simplified result has only two points.
    @Test
    public void testSimplify1() {
        Point2d [] list = {
                new Point2d(0,0),
                new Point2d(1,1),
                new Point2d(2,2),
                new Point2d(3,3),
                new Point2d(4,4),
                new Point2d(5,5),
        };
        LineCollection before = new LineCollection();
        for(int i=0;i<list.length-1;++i) {
            before.add(new LineSegment2D(list[i],list[i+1],Color.BLACK));
        }

        LineCollection after = (new RamerDouglasPeuckerRecursive(before)).simplify(0.01);
        Assertions.assertEquals(1,after.size());
    }

    // make a LineCollection with points that form an L.  confirm the simplified result has three points.
    @Test
    public void testSimplify2() {
        Point2d [] list = {
                new Point2d(0,0),
                new Point2d(1,1),
                new Point2d(2,2),
                new Point2d(3,1),
                new Point2d(4,0),
                new Point2d(5,-1),
        };
        LineCollection before = new LineCollection();
        for(int i=0;i<list.length-1;++i) {
            before.add(new LineSegment2D(list[i],list[i+1],Color.BLACK));
        }
        LineCollection after = (new RamerDouglasPeuckerRecursive(before)).simplify(0.01);
        Assertions.assertEquals(2,after.size());
    }

    @Disabled("only used for performance testing")
    @Test
    public void compareSpeedSimplify() {
        // make a large line collection with many colinear lines
        LineCollection before = new LineCollection();
        var a = randomPoint();
        for(int i=0;i<100000;++i) {
            var b = randomPoint();
            int splits = 1+(int)(Math.random()*10);
            var prev = a;
            for(int j=0;j<splits;++j) {
                var c = interpolate(a,b,j/(double)splits);
                before.add(new LineSegment2D(prev,c,Color.BLACK));
                prev = c;
            }
            before.add(new LineSegment2D(prev,b,Color.BLACK));
            a=b;
        }

        // compare speed of RamerDouglasPeuckerDeque and RamerDouglasPeuckerRecursive
        var start1 = System.currentTimeMillis();
        var after1 = new RamerDouglasPeuckerDeque(before).simplify(0.1);
        var end1 = System.currentTimeMillis();
        System.out.println("RamerDouglasPeuckerDeque: " + (end1 - start1) + "ms, "+after1.size());

        var start2 = System.currentTimeMillis();
        var after2 = new RamerDouglasPeuckerRecursive(before).simplify(0.1);
        var end2 = System.currentTimeMillis();
        System.out.println("RamerDouglasPeuckerRecursive: " + (end2 - start2) + "ms, "+after2.size());

        assert(after1.size()==after2.size());

    }

    private Point2d interpolate(Point2d a, Point2d b, double t) {
        return new Point2d(
                a.x*(1-t)+b.x*t,
                a.y*(1-t)+b.y*t);
    }

    @Test
    void testSplitByTravel() throws Exception {
        // load java/test/resources/svg/corners.svg
        Turtle turtle = TurtleFactory.load(Objects.requireNonNull(LoadHelper.class.getResource("/svg/corners.svg")).getPath());
        LineCollection before = turtle.getAsLineSegments();
        List<LineCollection> after = before.splitByTravel();
        Assertions.assertEquals(13,after.size());
    }

    @Test
    void testClosedLoop() {
        LineCollection before = new LineCollection();
        before.add(new LineSegment2D(new Point2d(0,0),new Point2d(1,0),Color.BLACK));
        before.add(new LineSegment2D(new Point2d(1,0),new Point2d(1,1),Color.BLACK));
        before.add(new LineSegment2D(new Point2d(1,1),new Point2d(0,1),Color.BLACK));
        before.add(new LineSegment2D(new Point2d(0,1),new Point2d(0,0),Color.BLACK));
        LineCollection after = (new RamerDouglasPeuckerRecursive(before)).simplify(0.1);
        Assertions.assertEquals(4,after.size());
    }
}
