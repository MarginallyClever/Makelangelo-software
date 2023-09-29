package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.makelangelo.turtle.Turtle;

public class TransformTurtle extends Node {
    private final DockReceiving<Turtle> turtle = new DockReceiving<>("turtle", Turtle.class,new Turtle());
    private final DockReceiving<Number> sx = new DockReceiving<>("scale x",Number.class,1);
    private final DockReceiving<Number> sy = new DockReceiving<>("scale y",Number.class,1);
    private final DockReceiving<Number> rotate = new DockReceiving<>("rotate degrees",Number.class,0);
    private final DockReceiving<Number> tx = new DockReceiving<>("translate x",Number.class,0);
    private final DockReceiving<Number> ty = new DockReceiving<>("translate y",Number.class,0);
    private final DockShipping<Turtle> output = new DockShipping<>("output", Turtle.class,new Turtle());

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
    public void update() {
        Turtle input = turtle.getValue();
        Turtle moved = new Turtle(input);
        moved.scale(sx.getValue().doubleValue(),sy.getValue().doubleValue());
        moved.rotate(rotate.getValue().doubleValue());
        moved.translate(tx.getValue().doubleValue(),ty.getValue().doubleValue());
        output.setValue(moved);
    }
}
