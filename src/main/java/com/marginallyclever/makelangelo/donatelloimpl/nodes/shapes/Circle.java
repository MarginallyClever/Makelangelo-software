package com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.dock.Input;
import com.marginallyclever.nodegraphcore.dock.Output;
import com.marginallyclever.nodegraphcore.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Circle extends Node {
    private static final Logger logger = LoggerFactory.getLogger(Circle.class);

    private final Input<Number> radius = new Input<>("radius", Number.class, 50);
    private final Output<Turtle> contents = new Output<>("contents", Turtle.class, new Turtle());

    public Circle() {
        super("Circle");
        addVariable(radius);
        addVariable(contents);
    }

    @Override
    public void update() {
        try {
            Turtle t = new Turtle();
            double r = radius.getValue().doubleValue()/2.0;
            double circumference = Math.ceil(Math.PI*r*2.0);
            t.jumpTo(r,0);
            for(int i=0;i<circumference;++i) {
                double v = 2.0*Math.PI * (double)i/circumference;
                t.moveTo(Math.cos(v)*r,Math.sin(v)*r);
            }
            t.jumpTo(r,0);
            t.penUp();
            contents.send(t);
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
