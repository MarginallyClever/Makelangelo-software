package com.marginallyclever.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.NodeVariable;

public class AddTurtles extends Node {
    private final NodeVariable<Turtle> turtleA = NodeVariable.newInstance("A", Turtle.class, new Turtle(),true,false);
    private final NodeVariable<Turtle> turtleB = NodeVariable.newInstance("B", Turtle.class, new Turtle(),true,false);
    private final NodeVariable<Turtle> output = NodeVariable.newInstance("output", Turtle.class, new Turtle(),false,true);

    public AddTurtles() {
        super("AddTurtles");
        addVariable(turtleA);
        addVariable(turtleB);
        addVariable(output);
    }

    public AddTurtles(Turtle turtleA, Turtle turtleB) {
        this();
        this.turtleA.setValue(turtleA);
        this.turtleB.setValue(turtleB);
    }

    @Override
    public Node create() {
        return new AddTurtles();
    }

    @Override
    public void update() throws Exception {
        Turtle a = turtleA.getValue();
        Turtle b = turtleB.getValue();
        Turtle sum = new Turtle(a);
        sum.add(b);
        output.setValue(sum);
        cleanAllInputs();
    }
}
