package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.fractals;

import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Gosper fractal
 */
public class Gosper extends Node {
    private final InputInt order = new InputInt("order", 7);
    private final OutputTurtle output = new OutputTurtle("turtle");

    public Gosper() {
        super("Gosper");
        addPort(order);
        addPort(output);
    }

    @Override
    public void update() {
        setComplete(0);
        int count = Math.max(1,order.getValue());

        Turtle turtle = new Turtle();
        turtle.penDown();
        gosperA(turtle,count);

        // center the turtle
        var b = turtle.getBounds();
        turtle.translate(-b.x-b.width/2, -b.y-b.height/2);

        output.setValue(turtle);
        setComplete(100);
    }

    // Gosper curve A = A-B--B+A++AA+B-
    private void gosperA(Turtle turtle,int n) {
        if (n == 0) {
            gosperForward(turtle);
            return;
        }
        gosperA(turtle,n-1);
        turtle.turn(-60);
        gosperB(turtle,n-1);
        turtle.turn(-60);
        turtle.turn(-60);
        gosperB(turtle,n-1);
        turtle.turn(60);
        gosperA(turtle,n-1);
        turtle.turn(60);
        turtle.turn(60);
        gosperA(turtle,n-1);
        gosperA(turtle,n-1);
        turtle.turn(60);
        gosperB(turtle,n-1);
        turtle.turn(-60);
    }

    // Gosper curve B = +A-BB--B-A++A+B
    public void gosperB(Turtle turtle,int n) {
        if (n == 0) {
            gosperForward(turtle);
            return;
        }
        turtle.turn(60);
        gosperA(turtle,n-1);
        turtle.turn(-60);
        gosperB(turtle,n-1);
        gosperB(turtle,n-1);
        turtle.turn(-60);
        turtle.turn(-60);
        gosperB(turtle,n-1);
        turtle.turn(-60);
        gosperA(turtle,n-1);
        turtle.turn(60);
        turtle.turn(60);
        gosperA(turtle,n-1);
        turtle.turn(60);
        gosperB(turtle,n-1);
    }

    public void gosperForward(Turtle turtle) {
        turtle.forward(1.0);
    }
}
