package com.marginallyclever.donatello.nodes.shapes;

import com.marginallyClever.makelangelo.turtle.Turtle;
import com.marginallyClever.nodeGraphCore.Node;
import com.marginallyClever.nodeGraphCore.NodeVariable;

public class TurtleCircle extends Node {
    private final NodeVariable<Number> radius = NodeVariable.newInstance("radius", Number.class, 50,true,false);
    private final NodeVariable<Turtle> contents = NodeVariable.newInstance("contents", Turtle.class, new Turtle(),false,true);

    public TurtleCircle() {
        super("Circle");
        addVariable(radius);
        addVariable(contents);
    }

    public TurtleCircle(double radius) {
        this();
        this.radius.setValue(radius);
    }

    @Override
    public Node create() {
        return new TurtleCircle();
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
            e.printStackTrace();
        }
    }
}
