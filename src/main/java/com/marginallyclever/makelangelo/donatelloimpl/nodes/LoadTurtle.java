package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.geom.Rectangle2D;

public class LoadTurtle extends Node {
    private final DockReceiving<String> filename = new DockReceiving<>("filename",String.class,null);
    private final DockReceiving<Turtle> contents = new DockReceiving<>("contents", Turtle.class, new Turtle());
    private final DockReceiving<Number> w = new DockReceiving<>("width", Number.class, 0);
    private final DockReceiving<Number> h = new DockReceiving<>("height", Number.class, 0);
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
        try {
            Turtle t = TurtleFactory.load(filename.getValue());
            contents.setValue(t);
            Rectangle2D r = t.getBounds();
            w.setValue(r.getWidth());
            h.setValue(r.getHeight());
            length.setValue(t.getDrawDistance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
