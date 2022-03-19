package com.marginallyclever.donatello.nodes;

import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.NodeVariable;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

public class ColorTurtle extends Node {
    private final NodeVariable<Turtle> turtle = NodeVariable.newInstance("turtle", Turtle.class,new Turtle(),true,false);
    private final NodeVariable<Number> red   = NodeVariable.newInstance("red",Number.class,0,true,false);
    private final NodeVariable<Number> green = NodeVariable.newInstance("green",Number.class,0,true,false);
    private final NodeVariable<Number> blue   = NodeVariable.newInstance("blue",Number.class,0,true,false);
    private final NodeVariable<Turtle> output = NodeVariable.newInstance("output", Turtle.class,new Turtle(),false,true);

    public ColorTurtle() {
        super("ColorTurtle");
        addVariable(turtle);
        addVariable(red  );
        addVariable(green);
        addVariable(blue );
        addVariable(output);
    }

    @Override
    public Node create() {
        return new ColorTurtle();
    }

    @Override
    public void update() throws Exception {
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
        cleanAllInputs();
    }
}
