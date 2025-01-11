package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.donatello.Filename;
import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;
import com.marginallyclever.nodegraphcore.Node;


import java.awt.geom.Rectangle2D;

public class LoadTurtle extends Node {
    private final Input<Filename> filename = new Input<>("filename",Filename.class,new Filename(""));
    private final Output<Turtle> contents = new Output<>("contents", Turtle.class, new Turtle());
    private final Output<Number> w = new Output<>("width", Number.class, 0);
    private final Output<Number> h = new Output<>("height", Number.class, 0);
    private final Output<Number> length = new Output<>("length", Number.class, 0);


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
            Turtle t = TurtleFactory.load(filename.getValue().get());
            contents.send(t);
            Rectangle2D r = t.getBounds();
            w.send(r.getWidth());
            h.send(r.getHeight());
            length.send(t.getDrawDistance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
