package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Transform a {@link Turtle} by scaling, rotating, and translating it.
 */
public class TransformTurtle extends Node {
    private final InputTurtle turtle = new InputTurtle("turtle");
    private final InputDouble sx = new InputDouble("scale x", 1d);
    private final InputDouble sy = new InputDouble("scale y", 1d);
    private final InputDouble rotate = new InputDouble("rotate degrees", 0d);
    private final InputDouble tx = new InputDouble("translate x", 0d);
    private final InputDouble ty = new InputDouble("translate y", 0d);
    private final OutputTurtle output = new OutputTurtle("output");

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
        moved.scale(sx.getValue(),sy.getValue());
        moved.rotate(rotate.getValue());
        moved.translate(tx.getValue(),ty.getValue());
        output.send(moved);
    }
}
