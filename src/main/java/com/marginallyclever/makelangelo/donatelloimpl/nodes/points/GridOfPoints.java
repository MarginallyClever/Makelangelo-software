package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.nodegraphcore.Node;

import javax.vecmath.Point2d;
import java.util.stream.IntStream;

/**
 * Create a grid of points controlled by the number (quantity) and spacing (distance between points).
 */
public class GridOfPoints extends Node {
    private final InputInt numberAcross = new InputInt("x count",10);
    private final InputDouble spaceAcross = new InputDouble("x spacing",10d);
    private final InputInt numberDown = new InputInt("y count",10);
    private final InputDouble spaceDown = new InputDouble("y spacing",10d);
    private final OutputPoints output = new OutputPoints("output");

    public GridOfPoints() {
        super("GridOfPoints");
        addVariable(numberAcross);
        addVariable(spaceAcross);
        addVariable(numberDown);
        addVariable(spaceDown);
        addVariable(output);
    }

    @Override
    public void update() {
        var nx = Math.max(1,numberAcross.getValue());
        var ny = Math.max(1,numberDown.getValue());
        var dx = spaceAcross.getValue();
        var dy = spaceDown.getValue();

        var list = new ListOfPoints();
        IntStream.range(0,ny).forEach(y->{
            IntStream.range(0,nx).forEach(x->{
                list.add(new Point2d(x*dx,y*dy));
            });
        });

        output.send(list);
    }
}
