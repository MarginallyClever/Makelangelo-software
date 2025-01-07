package com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.dock.Input;
import com.marginallyclever.nodegraphcore.dock.Output;
import com.marginallyclever.nodegraphcore.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rectangle extends Node {
    private static final Logger logger = LoggerFactory.getLogger(Rectangle.class);

    private final Input<Number> w = new Input<>("width", Number.class, 100);
    private final Input<Number> h = new Input<>("height", Number.class, 100);
    private final Output<Turtle> contents = new Output<>("contents", Turtle.class, new Turtle());

    public Rectangle() {
        super("Rectangle");
        addVariable(w);
        addVariable(h);
        addVariable(contents);
    }

    @Override
    public void update() {
        try {
            Turtle t = new Turtle();
            double ww = w.getValue().doubleValue()/2.0;
            double hh = h.getValue().doubleValue()/2.0;
            t.jumpTo(-ww,-hh);
            t.moveTo( ww,-hh);
            t.moveTo( ww, hh);
            t.moveTo(-ww, hh);
            t.moveTo(-ww,-hh);
            t.penUp();
            contents.send(t);
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
