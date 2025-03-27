package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.fractals;

import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  https://en.wikipedia.org/wiki/Dragon_curve
 *  L System tree A = A-A++A-A
 */
public class Dragon extends Node {
    private final InputInt order = new InputInt("order", 12);
    private final OutputTurtle output = new OutputTurtle("turtle");

    public Dragon() {
        super("Dragon");
        addPort(order);
        addPort(output);
    }

    @Override
    public void update() {
        setComplete(0);

        // create the sequence of moves
        int count = Math.max(1,order.getValue());
        List<Integer> sequence = new ArrayList<>();
        for (int i=0; i<count; i++) {
            List<Integer> copy = new ArrayList<>(sequence);
            Collections.reverse(copy);
            sequence.add(1);
            for (Integer turn : copy) {
                sequence.add(-turn);
            }
        }

        int sequenceSize = sequence.size();
        int i=0;
        // move to starting position
        Turtle turtle = new Turtle();
        turtle.jumpTo(0,0);
        turtle.penDown();
        // draw the fractal
        for (Integer turn : sequence) {
            turtle.turn(turn * 90);
            turtle.forward(1);
            setComplete(99 * i++ / sequenceSize);
        }

        // center the turtle
        var b = turtle.getBounds();
        turtle.translate(-b.x-b.width/2, -b.y-b.height/2);

        output.setValue(turtle);
        setComplete(100);
    }
}
