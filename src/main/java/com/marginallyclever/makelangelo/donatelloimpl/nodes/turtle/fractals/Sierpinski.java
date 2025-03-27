package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.fractals;

import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

public class Sierpinski extends Node {
    private final InputInt order = new InputInt("order", 5);
    private final OutputTurtle output = new OutputTurtle("turtle");
    private final int TURTLE_STEP = 2;

    public Sierpinski() {
        super("Sierpinski");
        addPort(order);
        addPort(output);
    }

    @Override
    public void update() {
        setComplete(0);
        int count = Math.max(1,order.getValue());

        Turtle turtle = new Turtle();
        turtle.jumpTo(0,0);
        turtle.penDown();

        // do the curve
        if( (count&1) == 0 ) {
            drawCurve(turtle, count, -60);
        } else {
            turtle.turn(60);
            drawCurve(turtle, count, -60);
        }

        // center the turtle
        var b = turtle.getBounds();
        turtle.translate(-b.x-b.width/2, -b.y-b.height/2);

        output.setValue(turtle);
        setComplete(100);
    }

    private void drawCurve(Turtle turtle,int n, double angle) {
        if (n == 0) {
            turtle.forward(TURTLE_STEP);
            return;
        }

        drawCurve(turtle, n-1, -angle);
        turtle.turn(angle);
        drawCurve(turtle, n-1, angle);
        turtle.turn(angle);
        drawCurve(turtle, n-1, -angle);
    }
}
