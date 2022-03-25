package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.NodeVariable;
import com.marginallyclever.makelangelo.turtle.Turtle;

public class TransformTurtle extends Node {
    private final NodeVariable<Turtle> turtle = NodeVariable.newInstance("turtle", Turtle.class,new Turtle(),true,false);
    private final NodeVariable<Number> sx = NodeVariable.newInstance("scale x",Number.class,1,true,false);
    private final NodeVariable<Number> sy = NodeVariable.newInstance("scale y",Number.class,1,true,false);
    private final NodeVariable<Number> rotate = NodeVariable.newInstance("rotate degrees",Number.class,0,true,false);
    private final NodeVariable<Number> tx = NodeVariable.newInstance("translate x",Number.class,0,true,false);
    private final NodeVariable<Number> ty = NodeVariable.newInstance("translate y",Number.class,0,true,false);
    private final NodeVariable<Turtle> output = NodeVariable.newInstance("output", Turtle.class,new Turtle(),false,true);

    public TransformTurtle() {
        super("TransformTurtle");
        addVariable(turtle);
        addVariable(sx);
        addVariable(sy);
        addVariable(rotate);
        addVariable(tx);
        addVariable(ty);
        addVariable(output);
    }

    @Override
    public void update() throws Exception {
        Turtle input = turtle.getValue();
        Turtle moved = new Turtle(input);
        moved.scale(sx.getValue().doubleValue(),sy.getValue().doubleValue());
        moved.rotate(rotate.getValue().doubleValue());
        moved.translate(tx.getValue().doubleValue(),ty.getValue().doubleValue());
        output.setValue(moved);
        cleanAllInputs();
    }
}
