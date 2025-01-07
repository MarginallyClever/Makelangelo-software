package com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.dock.Input;
import com.marginallyclever.nodegraphcore.dock.Output;
import com.marginallyclever.nodegraphcore.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NGon extends Node {
    private static final Logger logger = LoggerFactory.getLogger(NGon.class);

    private final Input<Number> radius = new Input<>("radius", Number.class, 10);
    private final Input<Number> steps = new Input<>("steps", Number.class, 4);
    private final Output<Turtle> contents = new Output<>("contents", Turtle.class, new Turtle());

    public NGon() {
        super("NGon");
        addVariable(radius);
        addVariable(steps);
        addVariable(contents);
    }

    @Override
    public void update() {
        try {
            Turtle t = new Turtle();
            double r = radius.getValue().doubleValue();
            int s = steps.getValue().intValue();

            t.jumpTo(r,0);
            for(int i=1;i<=s;++i) {
                double v = ( 2.0*Math.PI*(double)i ) / (double)s;
                t.moveTo(Math.cos(v), Math.sin(v));
            }
            t.penUp();
            contents.send(t);
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
