package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.*;

public class ColorTurtle extends Node {
    private final Input<Turtle> turtle = new Input<>("turtle", Turtle.class,new Turtle());
    private final Input<Number> red   = new Input<>("red",Number.class,0);
    private final Input<Number> green = new Input<>("green",Number.class,0);
    private final Input<Number> blue   = new Input<>("blue",Number.class,0);
    private final Output<Turtle> output = new Output<>("output", Turtle.class,new Turtle());

    public ColorTurtle() {
        super("ColorTurtle");
        addVariable(turtle);
        addVariable(red  );
        addVariable(green);
        addVariable(blue );
        addVariable(output);
    }

    @Override
    public void update() {
        Turtle input = turtle.getValue();
        int r = red.getValue().intValue();
        int g = green.getValue().intValue();
        int b = blue.getValue().intValue();
        Color c = new Color(r, g, b);
        Turtle moved = new Turtle();
        for( TurtleMove m : input.history ) {
            if(m.type== MovementType.TOOL_CHANGE) {
                moved.history.add(new TurtleMove(c.hashCode(),m.getDiameter(),MovementType.TOOL_CHANGE));
            } else {
                moved.history.add(new TurtleMove(m));
            }
        }
        output.send(moved);
    }
}
