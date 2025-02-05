package com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Draw a regular polygon.  A polygon with equal sides and angles.
 */
public class NGon extends Node {
    private static final Logger logger = LoggerFactory.getLogger(NGon.class);

    private final InputDouble radius = new InputDouble("radius", 10.0);
    private final InputInt steps = new InputInt("subdivisions", 4);
    private final OutputTurtle contents = new OutputTurtle("contents");

    public NGon() {
        super("NGon");
        addVariable(radius);
        addVariable(steps);
        addVariable(contents);
    }

    @Override
    public void update() {
        try {
            Turtle t = new Turtle();
            double r = radius.getValue();
            double s = Math.max(3,steps.getValue());

            for(int i=0;i<s;++i) {
                double v = 2.0*Math.PI * (double)i / s;
                t.moveTo(Math.cos(v)*r, Math.sin(v)*r);
                t.penDown();
            }
            t.moveTo(Math.cos(0)*r, Math.sin(0)*r);
            t.penUp();
            contents.send(t);
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }
}
