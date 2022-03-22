package com.marginallyclever.donatelloimpl.nodes.shapes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.NodeVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurtleLine extends Node {
    private static final Logger logger = LoggerFactory.getLogger(TurtleLine.class);

    private final NodeVariable<Number> x0 = NodeVariable.newInstance("x0", Number.class, 0,true,false);
    private final NodeVariable<Number> y0 = NodeVariable.newInstance("y0", Number.class, 0,true,false);
    private final NodeVariable<Number> x1 = NodeVariable.newInstance("x1", Number.class, 1,true,false);
    private final NodeVariable<Number> y1 = NodeVariable.newInstance("y1", Number.class, 0,true,false);
    private final NodeVariable<Turtle> contents = NodeVariable.newInstance("contents", Turtle.class, new Turtle(),false,true);

    public TurtleLine() {
        super("Line");
        addVariable(x0);
        addVariable(y0);
        addVariable(x1);
        addVariable(y1);
        addVariable(contents);
    }

    public TurtleLine(double px0,double py0,double px1,double py1) {
        this();
        x0.setValue(px0);
        y0.setValue(py0);
        x1.setValue(px1);
        y1.setValue(py1);
    }

    @Override
    public Node create() {
        return new TurtleLine();
    }

    @Override
    public void update() {
        try {
            Turtle t = new Turtle();
            t.jumpTo(x0.getValue().doubleValue(),y0.getValue().doubleValue());
            t.moveTo(x1.getValue().doubleValue(),y1.getValue().doubleValue());
            t.penUp();
            contents.setValue(t);
            cleanAllInputs();
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
