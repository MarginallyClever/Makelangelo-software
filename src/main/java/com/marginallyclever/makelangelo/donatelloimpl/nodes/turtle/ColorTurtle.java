package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputColor;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.*;

/**
 * Change the color of a {@link Turtle}.
 */
public class ColorTurtle extends Node {
    private final InputTurtle turtle = new InputTurtle("turtle");
    private final InputColor color = new InputColor("color", Color.BLACK);
    private final OutputTurtle output = new OutputTurtle("output");

    public ColorTurtle() {
        super("ColorTurtle");
        addPort(turtle);
        addPort(color);
        addPort(output);
    }

    @Override
    public void update() {
        Turtle input = turtle.getValue();
        Color c = color.getValue();
        Turtle moved = new Turtle();
        for( TurtleMove m : input.history ) {
            if(m.type== MovementType.TOOL_CHANGE) {
                moved.history.add(new TurtleMove(c.hashCode(),m.getDiameter(),MovementType.TOOL_CHANGE));
            } else {
                moved.history.add(new TurtleMove(m));
            }
        }
        // TODO could have redundant tool changes that should be removed.
        output.setValue(moved);
    }
}
