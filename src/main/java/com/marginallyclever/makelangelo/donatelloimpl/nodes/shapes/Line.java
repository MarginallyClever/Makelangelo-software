package com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.*;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Line extends Node {
    private static final Logger logger = LoggerFactory.getLogger(Line.class);

    private final Input<Number> x0 = new Input<>("x0", Number.class, 0);
    private final Input<Number> y0 = new Input<>("y0", Number.class, 0);
    private final Input<Number> x1 = new Input<>("x1", Number.class, 1);
    private final Input<Number> y1 = new Input<>("y1", Number.class, 0);
    private final Output<Turtle> contents = new Output<>("contents", Turtle.class, new Turtle());

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
        try {
            Turtle t = new Turtle();
            t.jumpTo(x0.getValue().doubleValue(),y0.getValue().doubleValue());
            t.moveTo(x1.getValue().doubleValue(),y1.getValue().doubleValue());
            t.penUp();
            contents.send(t);
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
