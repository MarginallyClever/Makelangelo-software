package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.shapes;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

public class Rectangle extends Node {
    private final InputDouble w = new InputDouble("width", 100.0);
    private final InputDouble h = new InputDouble("height", 100.0);
    private final OutputTurtle contents = new OutputTurtle("contents");

    public Rectangle() {
        super("Rectangle");
        addPort(w);
        addPort(h);
        addPort(contents);
    }

    @Override
    public void update() {
        Turtle t = new Turtle();
        double ww = w.getValue()/2.0;
        double hh = h.getValue()/2.0;
        t.jumpTo(-ww,-hh);
        t.moveTo( ww,-hh);
        t.moveTo( ww, hh);
        t.moveTo(-ww, hh);
        t.moveTo(-ww,-hh);
        t.penUp();
        contents.setValue(t);
    }
}
