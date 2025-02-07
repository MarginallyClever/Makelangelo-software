package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.donatello.ports.InputNumber;
import com.marginallyclever.donatello.ports.InputOneOfMany;
import com.marginallyclever.nodegraphcore.Node;

import javax.vecmath.Point2d;
import java.util.stream.IntStream;

/**
 * Create a grid of points controlled by the number (quantity) and spacing (distance between points).
 */
public class GridOfPoints extends Node {
    private final InputInt numberAcross = new InputInt("x count",10);
    private final InputNumber spaceAcross = new InputNumber("x spacing",10d);
    private final InputInt numberDown = new InputInt("y count",10);
    private final InputNumber spaceDown = new InputNumber("y spacing",10d);
    private final InputOneOfMany style = new InputOneOfMany("style");
    private final OutputPoints output = new OutputPoints("output");

    public GridOfPoints() {
        super("GridOfPoints");
        addVariable(numberAcross);
        addVariable(spaceAcross);
        addVariable(numberDown);
        addVariable(spaceDown);
        addVariable(style);
        addVariable(output);

        style.setOptions(new String[]{"spacing","total"});
    }

    @Override
    public void update() {
        var nx = Math.max(1,numberAcross.getValue());
        var ny = Math.max(1,numberDown.getValue());
        double dx, dy;

        var list = new ListOfPoints();
        if(style.getValue()==1) {
            // total distance divided by number of points.
            dx = spaceAcross.getValue().doubleValue() / nx;
            dy = spaceDown.getValue().doubleValue() / ny;
        } else {
            // space between points
            dx = spaceAcross.getValue().doubleValue();
            dy = spaceDown.getValue().doubleValue();
        }

        IntStream.range(0, ny).forEach(y -> {
            IntStream.range(0, nx).forEach(x -> {
                list.add(new Point2d(x * dx, y * dy));
            });
        });
        output.send(list);
    }
}
