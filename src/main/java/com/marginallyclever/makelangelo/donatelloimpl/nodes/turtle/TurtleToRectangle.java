package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.port.Output;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Returns the bounding box of the turtle's path.
 * @author Dan Royer
 * @since 2022-04-14
 */
public class TurtleToRectangle extends Node {
    private final InputTurtle turtle = new InputTurtle("turtle");
    private final Output<Rectangle2D> output = new Output<>("output", Rectangle2D.class, new Rectangle(0,0,0,0));

    public TurtleToRectangle() {
        super("TurtleToRectangle");
        addPort(turtle);
        addPort(output);
    }

    @Override
    public void update() {
        Turtle myTurtle = turtle.getValue();
        if(myTurtle!=null ) {
            output.setValue(myTurtle.getBounds());
        }
    }
}
