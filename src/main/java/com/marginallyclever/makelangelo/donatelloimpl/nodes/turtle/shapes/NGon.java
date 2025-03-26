package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.shapes;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Draw a regular polygon.  A polygon with equal sides and angles.
 */
public class NGon extends Node {
    private final InputDouble radius = new InputDouble("radius", 10.0);
    private final InputInt steps = new InputInt("subdivisions", 4);
    private final InputDouble angle = new InputDouble("angle", 45.0);
    private final OutputTurtle contents = new OutputTurtle("contents");

    public NGon() {
        super("NGon");
        addPort(radius);
        addPort(steps);
        addPort(angle);
        addPort(contents);
    }

    @Override
    public void update() {
        Turtle t = new Turtle();
        double r = radius.getValue();
        double s = Math.max(3,steps.getValue());
        double startAngle = Math.toRadians(angle.getValue());

        for(int i=0;i<s;++i) {
            double v = startAngle + 2.0*Math.PI * (double)i / s;
            t.moveTo(Math.cos(v)*r, Math.sin(v)*r);
            t.penDown();
        }
        t.moveTo(Math.cos(startAngle)*r, Math.sin(startAngle)*r);
        t.penUp();
        contents.setValue(t);
    }
}
