package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.Packet;

import java.awt.*;

/**
 * Returns the bounding box of the turtle's path.
 * @author Dan Royer
 * @since 2022-04-14
 */
public class TurtleToRectangle extends Node {
    private final DockReceiving<Turtle> turtle = new DockReceiving<>("turtle", Turtle.class,new Turtle());
    private final DockShipping<Rectangle> output = new DockShipping<>("output", Rectangle.class, new Rectangle(0,0,0,0));

    public TurtleToRectangle() {
        super("TurtleToRectangle");
        addVariable(turtle);
        addVariable(output);
    }

    @Override
    public void update() {
        if(turtle.hasPacketWaiting()) turtle.receive();
        
        Turtle myTurtle = turtle.getValue();
        if(myTurtle!=null ) {
            output.send(new Packet<>(myTurtle.getBounds()));
        }
    }
}
