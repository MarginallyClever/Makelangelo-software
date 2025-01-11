package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;
import com.marginallyclever.nodegraphcore.Node;

public class AddTurtles extends Node {
    private final Input<Turtle> turtleA = new Input<>("A", Turtle.class, new Turtle());
    private final Input<Turtle> turtleB = new Input<>("B", Turtle.class, new Turtle());
    private final Output<Turtle> output = new Output<>("output", Turtle.class, new Turtle());

    public AddTurtles() {
        super("AddTurtles");
        addVariable(turtleA);
        addVariable(turtleB);
        addVariable(output);
    }

    @Override
    public void update() {
        Turtle a = turtleA.getValue();
        Turtle b = turtleB.getValue();
        Turtle sum = new Turtle(a);
        sum.add(b);
        output.send(sum);
    }
}
