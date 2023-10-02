package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.Packet;

import java.awt.geom.Rectangle2D;

public class LoadTurtle extends Node {
    private final DockReceiving<String> filename = new DockReceiving<>("filename",String.class,null);
    private final DockShipping<Turtle> contents = new DockShipping<>("contents", Turtle.class, new Turtle());
    private final DockShipping<Number> w = new DockShipping<>("width", Number.class, 0);
    private final DockShipping<Number> h = new DockShipping<>("height", Number.class, 0);
    private final DockShipping<Number> length = new DockShipping<>("length", Number.class, 0);


    public LoadTurtle() {
        super("LoadTurtle");
        addVariable(filename);
        addVariable(contents);
        addVariable(w);
        addVariable(h);
    }

    @Override
    public void update() {
        if(filename.hasPacketWaiting()) filename.receive();

        try {
            Turtle t = TurtleFactory.load(filename.getValue());
            contents.send(new Packet<>(t));
            Rectangle2D r = t.getBounds();
            w.send(new Packet<>(r.getWidth()));
            h.send(new Packet<>(r.getHeight()));
            length.send(new Packet<>(t.getDrawDistance()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
