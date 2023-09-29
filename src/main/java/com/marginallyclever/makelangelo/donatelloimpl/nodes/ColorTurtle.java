package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

public class ColorTurtle extends Node {
    private final DockReceiving<Turtle> turtle = new DockReceiving<>("turtle", Turtle.class,new Turtle());
    private final DockReceiving<Number> red   = new DockReceiving<>("red",Number.class,0);
    private final DockReceiving<Number> green = new DockReceiving<>("green",Number.class,0);
    private final DockReceiving<Number> blue   = new DockReceiving<>("blue",Number.class,0);
    private final DockShipping<Turtle> output = new DockShipping<>("output", Turtle.class,new Turtle());

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
        int b = blue.getValue().intValue();
        int g = green.getValue().intValue();
        ColorRGB c = new ColorRGB(r, g, b);
        Turtle moved = new Turtle();
        for( TurtleMove m : input.history ) {
            if(m.type== MovementType.TOOL_CHANGE) {
                moved.history.add(new TurtleMove(c.toInt(),m.getDiameter(),MovementType.TOOL_CHANGE));
            } else {
                moved.history.add(new TurtleMove(m));
            }
        }
        output.setValue(moved);
    }
}
