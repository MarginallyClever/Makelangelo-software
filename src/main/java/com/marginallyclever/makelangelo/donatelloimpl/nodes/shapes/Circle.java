package com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Circle extends Node {
    private static final Logger logger = LoggerFactory.getLogger(Circle.class);

    private final DockReceiving<Number> radius = new DockReceiving<>("radius", Number.class, 50);
    private final DockShipping<Turtle> contents = new DockShipping<>("contents", Turtle.class, new Turtle());

    public Circle() {
        super("Circle");
        addVariable(radius);
        addVariable(contents);
    }

    @Override
    public void update() {
        if(radius.hasPacketWaiting()) radius.receive();

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
            contents.send(new Packet<>(t));
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
