package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.Packet;

public class AddTurtles extends Node {
    private final DockReceiving<Turtle> turtleA = new DockReceiving<>("A", Turtle.class, new Turtle());
    private final DockReceiving<Turtle> turtleB = new DockReceiving<>("B", Turtle.class, new Turtle());
    private final DockShipping<Turtle> output = new DockShipping<>("output", Turtle.class, new Turtle());

    public AddTurtles() {
        super("AddTurtles");
        addVariable(turtleA);
        addVariable(turtleB);
        addVariable(output);
    }

    @Override
    public void update() {
        if(turtleA.hasPacketWaiting()) turtleA.receive();
        if(turtleB.hasPacketWaiting()) turtleB.receive();

        Turtle a = turtleA.getValue();
        Turtle b = turtleB.getValue();
        Turtle sum = new Turtle(a);
        sum.add(b);
        output.send(new Packet<>(sum));
    }
}
