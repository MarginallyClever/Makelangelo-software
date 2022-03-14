package com.marginallyClever.donatelloNodes.nodes;

import com.marginallyClever.nodeGraphCore.Node;
import com.marginallyClever.nodeGraphCore.NodeVariable;
import com.marginallyClever.makelangelo.makeArt.io.vector.TurtleFactory;
import com.marginallyClever.makelangelo.turtle.Turtle;

public class SaveTurtle extends Node {
    private final NodeVariable<String> filename = NodeVariable.newInstance("filename",String.class,null,true,false);
    private final NodeVariable<Turtle> turtle = NodeVariable.newInstance("turtle", Turtle.class,new Turtle(),true,false);

    public SaveTurtle() {
        super("SaveTurtle");
        addVariable(filename);
        addVariable(turtle);
    }

    @Override
    public Node create() {
        return new SaveTurtle();
    }

    @Override
    public void update() {
        if(filename.getValue().isEmpty()) return;

        try {
            TurtleFactory.save(turtle.getValue(),filename.getValue());
            cleanAllInputs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
