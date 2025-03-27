package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.shapes;

import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

public class Line extends Node {
    private final InputDouble x0 = new InputDouble("x0", 0.0);
    private final InputDouble y0 = new InputDouble("y0", 0.0);
    private final InputDouble x1 = new InputDouble("x1", 1.0);
    private final InputDouble y1 = new InputDouble("y1", 0.0);
    private final InputInt steps = new InputInt("subdivisions", 1);
    private final OutputTurtle contents = new OutputTurtle("contents");

    public Line() {
        super("Line");
        addPort(x0);
        addPort(y0);
        addPort(x1);
        addPort(y1);
        addPort(steps);
        addPort(contents);
    }

    @Override
    public void update() {
        double count = Math.max(1,steps.getValue());
        double px0 = x0.getValue();
        double py0 = y0.getValue();
        double px1 = x1.getValue();
        double py1 = y1.getValue();

        Turtle turtle = new Turtle();
        turtle.jumpTo(px0,py0);
        for (int i = 1; i < count; i++) {
            var x = MathHelper.lerp(i/count,px0,px1);
            var y = MathHelper.lerp(i/count,py0,py1);
            turtle.moveTo(x,y);
            turtle.penDown();
        }
        turtle.moveTo(px1,py1);
        contents.setValue(turtle);
    }
}
