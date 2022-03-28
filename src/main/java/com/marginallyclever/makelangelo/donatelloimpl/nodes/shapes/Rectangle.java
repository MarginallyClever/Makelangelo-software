package com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.NodeVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rectangle extends Node {
    private static final Logger logger = LoggerFactory.getLogger(Rectangle.class);

    private final NodeVariable<Number> w = NodeVariable.newInstance("width", Number.class, 100,true,true);
    private final NodeVariable<Number> h = NodeVariable.newInstance("height", Number.class, 100,true,true);
    private final NodeVariable<Turtle> contents = NodeVariable.newInstance("contents", Turtle.class, new Turtle(),false,true);

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
            cleanAllInputs();
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
