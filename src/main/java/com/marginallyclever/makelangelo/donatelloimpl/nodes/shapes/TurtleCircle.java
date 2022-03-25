package com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.NodeVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurtleCircle extends Node {
    private static final Logger logger = LoggerFactory.getLogger(TurtleCircle.class);

    private final NodeVariable<Number> radius = NodeVariable.newInstance("radius", Number.class, 50,true,false);
    private final NodeVariable<Turtle> contents = NodeVariable.newInstance("contents", Turtle.class, new Turtle(),false,true);

    public TurtleCircle() {
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
            contents.setValue(t);
            cleanAllInputs();
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
