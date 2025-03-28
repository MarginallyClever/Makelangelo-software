package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.makeart.turtletool.ReorderHelper;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Reorder a {@link com.marginallyclever.makelangelo.turtle.Turtle} for efficient drawing by minimizing travel.
 */
public class Reorder extends Node {
    private final InputTurtle inputTurtle = new InputTurtle("input");
    private final OutputTurtle outputTurtle = new OutputTurtle("output");

    public Reorder() {
        super("Reorder");
        addPort(inputTurtle);
        addPort(outputTurtle);
    }

    @Override
    public void update() {
        Turtle in = inputTurtle.getValue();
        if(!in.hasDrawing()) return;

        setComplete(0);
        var output = (new ReorderHelper()).splitAndReorderTurtle(in);
        outputTurtle.setValue(output);
        setComplete(100);
    }
}
