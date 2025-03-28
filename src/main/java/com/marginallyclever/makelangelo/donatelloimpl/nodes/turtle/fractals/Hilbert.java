package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.fractals;

import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Hilbert fractal
 */
public class Hilbert extends Node {
    private final InputInt order = new InputInt("order", 7);
    private final OutputTurtle output = new OutputTurtle("turtle");
    private final int TURTLE_STEP = 10;

    public Hilbert() {
        super("Hilbert");
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
        hilbert(turtle,count);

        // center the turtle
        var b = turtle.getBounds();
        turtle.translate(-b.x-b.width/2, -b.y-b.height/2);

        output.setValue(turtle);
        setComplete(100);
    }

    // Hilbert curve A = -BF+AFA+FB-
    private void hilbert(Turtle turtle,int n) {
        if (n == 0) return;
        turtle.turn(90);
        treblih(turtle, n - 1);
        turtle.forward(TURTLE_STEP);
        turtle.turn(-90);
        hilbert(turtle, n - 1);
        turtle.forward(TURTLE_STEP);
        hilbert(turtle, n - 1);
        turtle.turn(-90);
        turtle.forward(TURTLE_STEP);
        treblih(turtle, n - 1);
        turtle.turn(90);
    }

    // Hilbert curve reverse B = +AF-BFB-FA+
    public void treblih(Turtle turtle,int n) {
        if (n == 0) return;
        turtle.turn(-90);
        hilbert(turtle, n - 1);
        turtle.forward(TURTLE_STEP);
        turtle.turn(90);
        treblih(turtle, n - 1);
        turtle.forward(TURTLE_STEP);
        treblih(turtle, n - 1);
        turtle.turn(90);
        turtle.forward(TURTLE_STEP);
        hilbert(turtle, n - 1);
        turtle.turn(-90);
    }
}
