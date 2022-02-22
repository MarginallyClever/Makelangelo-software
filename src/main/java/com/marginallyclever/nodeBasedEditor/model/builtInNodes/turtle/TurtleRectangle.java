package com.marginallyclever.nodeBasedEditor.model.builtInNodes.turtle;

import com.marginallyclever.makelangelo.makeArt.io.vector.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

import java.awt.geom.Rectangle2D;

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
        if(!isDirty()) return;
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
