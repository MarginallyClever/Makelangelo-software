package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputDouble;
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
        addPort(turtle);
        addPort(sx);
        addPort(sy);
        addPort(rotate);
        addPort(tx);
        addPort(ty);
        addPort(output);
    }

    @Override
    public void update() {
        Turtle input = turtle.getValue();
        if(!input.hasDrawing()) {
            output.setValue(input);
            return;
        }
        Turtle moved = new Turtle(input);
        moved.scale(sx.getValue(),sy.getValue());
        moved.rotate(rotate.getValue());
        moved.translate(tx.getValue(),ty.getValue());
        output.setValue(moved);
    }
}
