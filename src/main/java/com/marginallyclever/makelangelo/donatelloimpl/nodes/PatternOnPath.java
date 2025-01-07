package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.dock.Input;
import com.marginallyclever.nodegraphcore.dock.Output;
import com.marginallyclever.nodegraphcore.Node;


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
            double pDistance = myPath.getDrawDistance();
            double step = pDistance/(double)c;
            if(pDistance==0) {
                pDistance=c;
                step=1;
            }
            double n=0;
            for(int i=0;i<c;++i) {
                Point2D p = myPath.interpolate(n);
                n+=step;
                Turtle stamp = new Turtle(myPattern);
                stamp.translate(p.x,p.y);
                sum.add(stamp);
            }
        }
        output.send(sum);
    }
}
