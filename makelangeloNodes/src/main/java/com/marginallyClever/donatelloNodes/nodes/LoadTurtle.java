package com.marginallyClever.donatelloNodes.nodes;

import com.marginallyClever.makelangelo.makeArt.io.vector.TurtleFactory;
import com.marginallyClever.makelangelo.turtle.Turtle;
import com.marginallyClever.nodeGraphCore.Node;
import com.marginallyClever.nodeGraphCore.NodeVariable;

import java.awt.geom.Rectangle2D;

public class LoadTurtle extends Node {
    private final NodeVariable<String> filename = NodeVariable.newInstance("filename",String.class,null,true,false);
    private final NodeVariable<Turtle> contents = NodeVariable.newInstance("contents", Turtle.class, new Turtle(),false,true);
    private final NodeVariable<Number> w = NodeVariable.newInstance("width", Number.class, 0,false,true);
    private final NodeVariable<Number> h = NodeVariable.newInstance("height", Number.class, 0,false,true);

    public LoadTurtle() {
        super("LoadTurtle");
        addVariable(filename);
        addVariable(contents);
        addVariable(w);
        addVariable(h);
    }

    public LoadTurtle(String filename) {
        this();
        this.filename.setValue(filename);
    }

    @Override
    public Node create() {
        return new LoadTurtle();
    }

    @Override
    public void update() {
        try {
            Turtle t = TurtleFactory.load(filename.getValue());
            contents.setValue(t);
            Rectangle2D r = t.getBounds();
            w.setValue(r.getWidth());
            h.setValue(r.getHeight());
            cleanAllInputs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
