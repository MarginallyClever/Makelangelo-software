package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.makeart.io.vector.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.NodeVariable;

import java.awt.geom.Rectangle2D;

public class LoadTurtle extends Node {
    private final NodeVariable<String> filename = NodeVariable.newInstance("filename",String.class,null,true,false);
    private final NodeVariable<Turtle> contents = NodeVariable.newInstance("contents", Turtle.class, new Turtle(),false,true);
    private final NodeVariable<Number> w = NodeVariable.newInstance("width", Number.class, 0,false,true);
    private final NodeVariable<Number> h = NodeVariable.newInstance("height", Number.class, 0,false,true);
    private final NodeVariable<Number> length = NodeVariable.newInstance("length", Number.class, 0,false,true);


    public LoadTurtle() {
        super("LoadTurtle");
        addVariable(filename);
        addVariable(contents);
        addVariable(w);
        addVariable(h);
    }

    @Override
    public void update() throws Exception {
        Turtle t = TurtleFactory.load(filename.getValue());
        contents.setValue(t);
        Rectangle2D r = t.getBounds();
        w.setValue(r.getWidth());
        h.setValue(r.getHeight());
        length.setValue(t.getDrawDistance());
        cleanAllInputs();
    }
}
