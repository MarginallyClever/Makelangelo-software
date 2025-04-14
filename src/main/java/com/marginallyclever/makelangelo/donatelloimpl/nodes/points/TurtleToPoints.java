package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.turtle.ConcreteListOfPoints;
import com.marginallyclever.nodegraphcore.Node;

import javax.vecmath.Point2d;

/**
 * Converts a {@link com.marginallyclever.makelangelo.turtle.Turtle} to a {@link ConcreteListOfPoints}.
 */
public class TurtleToPoints extends Node {
    private final InputTurtle turtle = new InputTurtle("Turtle");
    private final OutputPoints points = new OutputPoints("points");

    public TurtleToPoints() {
        super("TurtleToPoints");
        addPort(turtle);
        addPort(points);
    }

    @Override
    public void update() {
        var in = turtle.getValue();
        var out = new ConcreteListOfPoints();

        for( var layer : in.getLayers() ) {
            for( var line : layer.getAllLines() ) {
                for( var point : line.getAllPoints()) {
                    out.add(new Point2d(point.x,point.y));
                }
            }
        }

        points.setValue(out);
    }
}
