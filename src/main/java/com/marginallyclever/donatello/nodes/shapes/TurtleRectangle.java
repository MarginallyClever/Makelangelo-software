package com.marginallyClever.donatello.nodes.shapes;

import com.marginallyClever.makelangelo.turtle.Turtle;
import com.marginallyClever.nodeGraphCore.Node;
import com.marginallyClever.nodeGraphCore.NodeVariable;

public class TurtleRectangle extends Node {
    private final NodeVariable<Number> w = NodeVariable.newInstance("width", Number.class, 100,true,true);
    private final NodeVariable<Number> h = NodeVariable.newInstance("height", Number.class, 100,true,true);
    private final NodeVariable<Turtle> contents = NodeVariable.newInstance("contents", Turtle.class, new Turtle(),false,true);

    public TurtleRectangle() {
        super("Rectangle");
        addVariable(w);
        addVariable(h);
        addVariable(contents);
    }

    public TurtleRectangle(double width,double height) {
        this();
        w.setValue(width);
        h.setValue(height);
    }

    @Override
    public Node create() {
        return new TurtleRectangle();
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
            e.printStackTrace();
        }
    }
}
