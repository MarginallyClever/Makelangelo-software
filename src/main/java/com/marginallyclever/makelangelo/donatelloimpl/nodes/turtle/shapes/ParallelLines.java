package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.shapes;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * <p>ParallelLines is a class that represents a shape made of parallel lines.</p>
 * <h4>Ports</h4>
 * <ul>
 *     <li>numLines - number of lines to generate</li>
 *     <li>length - length of each line on the x axis</li>
 *     <li>spacing - distance between lines</li>
 *     <li>angle - degrees CCW rotation of the entire set</li>
 *     <li>output - where the resulting {@link Turtle} will appear</li>
 * </ul>
 */
public class ParallelLines extends Node {
    private final InputInt numLines = new InputInt("numLines", 50);
    private final InputDouble lineLength = new InputDouble("length", 500.0);
    private final InputDouble spacing = new InputDouble("spacing", 10.0);
    private final InputDouble angle = new InputDouble("angle", 0.0);
    private final OutputTurtle output = new OutputTurtle("output");

    public ParallelLines() {
        super("ParallelLines");
        addPort(numLines);
        addPort(lineLength);
        addPort(spacing);
        addPort(angle);
        addPort(output);
    }

    @Override
    public void update() {
        int count = numLines.getValue();
        double length = lineLength.getValue();
        double space = spacing.getValue();
        double angleValue = angle.getValue();

        // Draw the parallel lines
        Turtle t2 = new Turtle();
        for (int i = 0; i < count; i++) {
            t2.penDown();
            t2.forward(length);
            t2.penUp();
            var turn = (i % 2 == 0) ? 90 : -90;
            t2.turn(turn);
            t2.forward(space);
            t2.turn(turn);
        }
        var bounds = t2.getBounds();
        t2.translate(-bounds.width/2, -bounds.height/2);
        t2.rotate(angleValue);
        output.setValue(t2);
    }
}
