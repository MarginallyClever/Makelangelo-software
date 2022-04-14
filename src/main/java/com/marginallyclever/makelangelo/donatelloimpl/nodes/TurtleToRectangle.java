package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.NodeVariable;

import java.awt.*;

/**
 * Returns the bounding box of the turtle's path.
 * @author Dan Royer
 * @since 2022-04-14
 */
public class TurtleToRectangle extends Node {
    private final NodeVariable<Turtle> turtle = NodeVariable.newInstance("turtle", Turtle.class,new Turtle(),true,false);
    private final NodeVariable<Rectangle> output = NodeVariable.newInstance("output", Rectangle.class, new Rectangle(0,0,0,0),false,true);

    public TurtleToRectangle() {
        super("TurtleToRectangle");
        addVariable(turtle);
        addVariable(output);
    }

    @Override
    public void update() throws Exception {
        Turtle myTurtle = turtle.getValue();
        if(myTurtle!=null ) {
            output.setValue(myTurtle.getBounds());
            cleanAllInputs();
        }
    }
}
