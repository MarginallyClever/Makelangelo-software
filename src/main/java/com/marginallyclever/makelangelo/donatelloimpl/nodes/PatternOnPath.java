package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtlePathWalker;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;

/**
 * Place a pattern on a path.
 */
public class PatternOnPath extends Node {
    private final Input<Turtle> pattern = new Input<>("pattern", Turtle.class, new Turtle());
    private final Input<Turtle> path = new Input<>("path", Turtle.class, new Turtle());
    private final Input<Number> count = new Input<>("count", Number.class, 10);
    private final Output<Turtle> output = new Output<>("output", Turtle.class, new Turtle());

    public PatternOnPath() {
        super("PatternOnPath");
        addVariable(pattern);
        addVariable(path);
        addVariable(count);
        addVariable(output);
    }

    @Override
    public void update() {
        Turtle sum = new Turtle();
        Turtle myPattern = pattern.getValue();
        Turtle myPath = path.getValue();
        int c = count.getValue().intValue();
        if(c>0) {
            TurtlePathWalker walker = new TurtlePathWalker(myPath);
            double pDistance = walker.getDrawDistance();
            double step = (pDistance==0) ? 1 : pDistance/(double)c;
            while(!walker.isDone()) {
                Point2D p = walker.walk(step);
                Turtle stamp = new Turtle(myPattern);
                stamp.translate(p.x,p.y);
                sum.add(stamp);
                setComplete((int)(100*walker.getTSum()/pDistance));
            }
        }
        setComplete(100);
        output.send(sum);
    }
}
