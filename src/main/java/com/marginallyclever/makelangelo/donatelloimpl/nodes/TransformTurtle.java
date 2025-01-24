package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Transform a {@link Turtle} by scaling, rotating, and translating it.
 */
public class TransformTurtle extends Node {
    private final Input<Turtle> turtle = new Input<>("turtle", Turtle.class,new Turtle());
    private final Input<Number> sx = new Input<>("scale x",Number.class,1);
    private final Input<Number> sy = new Input<>("scale y",Number.class,1);
    private final Input<Number> rotate = new Input<>("rotate degrees",Number.class,0);
    private final Input<Number> tx = new Input<>("translate x",Number.class,0);
    private final Input<Number> ty = new Input<>("translate y",Number.class,0);
    private final Output<Turtle> output = new Output<>("output", Turtle.class,new Turtle());

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
        output.send(moved);
    }
}
