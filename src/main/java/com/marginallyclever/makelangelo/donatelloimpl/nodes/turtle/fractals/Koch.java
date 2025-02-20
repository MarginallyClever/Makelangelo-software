package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.fractals;

import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Koch fractal.  <a href="https://en.wikipedia.org/wiki/Koch_curve">...</a>
 * L System tree A = A-A++A-A
 */
public class Koch extends Node {
    private final InputInt order = new InputInt("order", 5);
    private final OutputTurtle output = new OutputTurtle("turtle");
    private final int TURTLE_STEP = 2;

    public Koch() {
        super("Koch");
        addPort(order);
        addPort(output);
    }

    @Override
    public void update() {
        setComplete(0);
        int count = Math.max(1,order.getValue());

        Turtle turtle = new Turtle();
        turtle.penDown();

        drawTriangle(turtle,count);

        // center the turtle
        var b = turtle.getBounds();
        turtle.translate(-b.x-b.width/2, -b.y-b.height/2);

        output.setValue(turtle);
        setComplete(100);
    }

    // L System tree A = A-A++A-A
    private void drawTriangle(Turtle turtle,int n) {
        if (n == 0) {
            turtle.forward(TURTLE_STEP);
            return;
        }
        drawTriangle(turtle,n-1);
        if(n>1) {
            turtle.turn(-60);
            drawTriangle(turtle,n-1);
            turtle.turn(120);
            drawTriangle(turtle,n-1);
            turtle.turn(-60);
        } else {
            turtle.forward(TURTLE_STEP);
        }
        drawTriangle(turtle,n-1);
    }
}
