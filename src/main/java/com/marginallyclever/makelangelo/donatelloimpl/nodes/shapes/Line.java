package com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Line extends Node {
    private static final Logger logger = LoggerFactory.getLogger(Line.class);

    private final InputDouble x0 = new InputDouble("x0", 0.0);
    private final InputDouble y0 = new InputDouble("y0", 0.0);
    private final InputDouble x1 = new InputDouble("x1", 1.0);
    private final InputDouble y1 = new InputDouble("y1", 0.0);
    private final OutputTurtle contents = new OutputTurtle("contents");

    public Line() {
        super("Line");
        addVariable(x0);
        addVariable(y0);
        addVariable(x1);
        addVariable(y1);
        addVariable(contents);
    }

    @Override
    public void update() {
        try {
            Turtle t = new Turtle();
            t.jumpTo(x0.getValue().doubleValue(),y0.getValue().doubleValue());
            t.moveTo(x1.getValue().doubleValue(),y1.getValue().doubleValue());
            t.penUp();
            contents.send(t);
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
