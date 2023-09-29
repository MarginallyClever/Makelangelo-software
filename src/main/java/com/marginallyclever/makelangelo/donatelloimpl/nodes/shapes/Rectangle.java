package com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rectangle extends Node {
    private static final Logger logger = LoggerFactory.getLogger(Rectangle.class);

    private final DockReceiving<Number> w = new DockReceiving<>("width", Number.class, 100);
    private final DockReceiving<Number> h = new DockReceiving<>("height", Number.class, 100);
    private final DockShipping<Turtle> contents = new DockShipping<>("contents", Turtle.class, new Turtle());

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
            contents.setValue(t);
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
