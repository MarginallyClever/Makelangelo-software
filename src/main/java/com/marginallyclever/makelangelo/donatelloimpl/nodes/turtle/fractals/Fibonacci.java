package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.fractals;

import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

import java.util.Stack;

/**
 * Fibonacci fractal
 */
public class Fibonacci extends Node {
    private final InputInt order = new InputInt("order", 7);
    private final OutputTurtle output = new OutputTurtle("turtle");

    public Fibonacci() {
        super("Fibonacci");
        addPort(order);
        addPort(output);
    }

    @Override
    public void update() {
        setComplete(0);
        int count = Math.max(1,order.getValue());
        var sequence = buildFibonacciSequence(count);

        Turtle turtle = new Turtle();
        turtle.jumpTo(0,0);
        turtle.penDown();

        // do the curve, one square at a time.
        while(!sequence.isEmpty()) {
            int o = sequence.pop();
            fibonacciCell(turtle,o);
        }

        // center the turtle
        var b = turtle.getBounds();
        turtle.translate(-b.x-b.width/2, -b.y-b.height/2);

        output.setValue(turtle);
        setComplete(100);
    }

    private Stack<Integer> buildFibonacciSequence(int order) {
        Stack<Integer> fibonacciSequence = new Stack<>();
        fibonacciSequence.add(1);
        fibonacciSequence.add(1);
        int a = 1;
        int b = 1;
        int c;

        while(order>2) {
            c = a+b;
            fibonacciSequence.add(c);
            a=b;
            b=c;
            order--;
        }

        return fibonacciSequence;
    }

    private void fibonacciCell(Turtle turtle, double size) {
        // make the square around the cell
        turtle.forward(size);
        turtle.turn(90);
        turtle.forward(size);
        turtle.turn(90);
        double x2 = turtle.getX();
        double y2 = turtle.getY();
        turtle.forward(size);
        turtle.turn(90);
        double x0 = turtle.getX();
        double y0 = turtle.getY();
        turtle.forward(size);
        turtle.turn(90);

        // make the curve
        double x1 = turtle.getX();
        double y1 = turtle.getY();

        double dx, dy, px, py, len;
        final int steps = (int)(size/2);
        for(int i=0; i<steps; ++i) {
            px = (x2-x1) * ((double)i/steps) + x1;
            py = (y2-y1) * ((double)i/steps) + y1;
            dx = px - x0;
            dy = py - y0;
            len = Math.sqrt(dx*dx+dy*dy);
            px = dx*size/len + x0;
            py = dy*size/len + y0;
            turtle.moveTo(px, py);
        }
        turtle.moveTo(x2, y2);
        turtle.turn(90);
    }
}
