package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputNumber;
import com.marginallyclever.donatello.ports.InputOneOfMany;
import com.marginallyclever.makelangelo.turtle.ConcreteListOfPoints;
import com.marginallyclever.nodegraphcore.Node;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import java.util.stream.IntStream;

/**
 * Create a grid of points controlled by the number (quantity) and spacing (distance between points).
 */
public class GridOfPoints extends Node {
    private final InputNumber Xa = new InputNumber("Xa",10);
    private final InputNumber Xb = new InputNumber("Xb",10d);
    private final InputNumber Ya = new InputNumber("Ya",10);
    private final InputNumber Yb = new InputNumber("Yb",10d);
    private final InputOneOfMany style = new InputOneOfMany("style");
    private final InputDouble angle = new InputDouble("angle");
    private final OutputPoints output = new OutputPoints("output");

    public GridOfPoints() {
        super("GridOfPoints");
        addPort(Xa);
        addPort(Xb);
        addPort(Ya);
        addPort(Yb);
        addPort(style);
        addPort(angle);
        addPort(output);

        style.setOptions(new String[]{"a * b","b / (a count)","b / (a distance)"});
    }

    @Override
    public void update() {
        // we're going to make a grid nx,ny with margin dx,dy.
        int nx,ny;
        double dx, dy;

        var list = new ConcreteListOfPoints();
        switch(style.getValue()) {
            case 2: {
                // d = a
                // n = total distance (b) divided by spacing (a)
                dx = Math.max(1, Xa.getValue().doubleValue());
                dy = Math.max(1, Ya.getValue().doubleValue());
                nx = (int)Math.max(1, Xb.getValue().doubleValue() / dx);
                ny = (int)Math.max(1, Yb.getValue().doubleValue() / dy);
                break;
            }
            case 1: {
                // n = a
                // d = total distance (b) divided by number of points (a).
                nx = Math.max(1, Xa.getValue().intValue());
                ny = Math.max(1, Ya.getValue().intValue());
                dx = Xb.getValue().doubleValue() / nx;
                dy = Yb.getValue().doubleValue() / ny;
                break;
            }
            default: {
                // n = a
                // d = b
                nx = Math.max(1, Xa.getValue().intValue());
                ny = Math.max(1, Ya.getValue().intValue());
                dx = Xb.getValue().doubleValue();
                dy = Yb.getValue().doubleValue();
                break;
            }
        }

        double halfX = (nx*dx) / 2;
        double halfY = (ny*dy) / 2;
        Matrix3d transform = new Matrix3d();
        transform.rotZ(Math.toRadians(angle.getValue()));

        // now make the grid
        IntStream.range(0,ny).forEach(y -> {
            IntStream.range(0,nx).forEach(x -> {
                var p = new Point3d(x * dx - halfX, y * dy - halfY,0d);
                transform.transform(p);
                list.add(new Point2d(p.x, p.y));
            });
        });
        output.setValue(list);
    }
}
