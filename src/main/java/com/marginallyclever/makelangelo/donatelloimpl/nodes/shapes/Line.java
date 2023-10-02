package com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Line extends Node {
    private static final Logger logger = LoggerFactory.getLogger(Line.class);

    private final DockReceiving<Number> x0 = new DockReceiving<>("x0", Number.class, 0);
    private final DockReceiving<Number> y0 = new DockReceiving<>("y0", Number.class, 0);
    private final DockReceiving<Number> x1 = new DockReceiving<>("x1", Number.class, 1);
    private final DockReceiving<Number> y1 = new DockReceiving<>("y1", Number.class, 0);
    private final DockShipping<Turtle> contents = new DockShipping<>("contents", Turtle.class, new Turtle());

    public Line() {
        super("Line");
        addVariable(x0);
        addVariable(y0);
        addVariable(x1);
        addVariable(y1);
        addVariable(contents);
    }

    @Override
    public void update() {
        if(x0.hasPacketWaiting()) x0.receive();
        if(y0.hasPacketWaiting()) y0.receive();
        if(x1.hasPacketWaiting()) x1.receive();
        if(y1.hasPacketWaiting()) y1.receive();

        try {
            Turtle t = new Turtle();
            t.jumpTo(x0.getValue().doubleValue(),y0.getValue().doubleValue());
            t.moveTo(x1.getValue().doubleValue(),y1.getValue().doubleValue());
            t.penUp();
            contents.send(new Packet<>(t));
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
