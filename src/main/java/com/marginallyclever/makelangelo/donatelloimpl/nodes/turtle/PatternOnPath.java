package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;


import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtlePathWalker;
import com.marginallyclever.nodegraphcore.Node;

import javax.vecmath.Point2d;

/**
 * Place a pattern on a path.
 */
public class PatternOnPath extends Node {
    private final InputTurtle pattern = new InputTurtle("pattern");
    private final InputTurtle path = new InputTurtle("path");
    private final InputInt count = new InputInt("count", 10);
    private final OutputTurtle output = new OutputTurtle("output");

    public PatternOnPath() {
        super("PatternOnPath");
        addPort(pattern);
        addPort(path);
        addPort(count);
        addPort(output);
    }

    @Override
    public void update() {
        Turtle sum = new Turtle();
        Turtle myPattern = pattern.getValue();
        Turtle myPath = path.getValue();
        int c = count.getValue();
        if(c>0) {
            TurtlePathWalker walker = new TurtlePathWalker(myPath);
            double pDistance = walker.getDrawDistance();
            double step = (pDistance==0) ? 1 : pDistance/(double)c;
            while(!walker.isDone()) {
                Point2d p = walker.walk(step);
                Turtle stamp = new Turtle(myPattern);
                stamp.translate(p.x,p.y);
                sum.add(stamp);
                setComplete((int)(100*walker.getTSum()/pDistance));
            }
        }
        setComplete(100);
        output.setValue(sum);
    }
}
