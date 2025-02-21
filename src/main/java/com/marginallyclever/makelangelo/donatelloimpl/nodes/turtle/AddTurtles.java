package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Add two {@link Turtle}s together.
 */
public class AddTurtles extends Node {
    private final InputTurtle turtleA = new InputTurtle("A");
    private final InputTurtle turtleB = new InputTurtle("B");
    private final OutputTurtle output = new OutputTurtle("output");

    public AddTurtles() {
        super("AddTurtles");
        addPort(turtleA);
        addPort(turtleB);
        addPort(output);
    }

    @Override
    public void update() {
        Turtle a = turtleA.getValue();
        Turtle b = turtleB.getValue();
        if(!a.hasDrawing() || !b.hasDrawing()) {
            // no drawing to add
            return;
        }
        Turtle sum = new Turtle(a);
        sum.add(b);
        output.setValue(sum);
    }
}
