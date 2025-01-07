package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.dock.Input;
import com.marginallyclever.nodegraphcore.dock.Output;
import com.marginallyclever.nodegraphcore.Node;


import java.awt.geom.Rectangle2D;

public class LoadTurtle extends Node {
    private final Input<String> filename = new Input<>("filename",String.class,null);
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
            Turtle t = TurtleFactory.load(filename.getValue());
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
