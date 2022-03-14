package com.marginallyClever.donatello.nodes;

import com.marginallyClever.convenience.Point2D;
import com.marginallyClever.makelangelo.turtle.Turtle;
import com.marginallyClever.nodeGraphCore.Node;
import com.marginallyClever.nodeGraphCore.NodeVariable;

public class PatternOnPath extends Node {
    private final NodeVariable<Turtle> pattern = NodeVariable.newInstance("pattern", Turtle.class, new Turtle(),true,false);
    private final NodeVariable<Turtle> path = NodeVariable.newInstance("path", Turtle.class, new Turtle(),true,false);
    private final NodeVariable<Number> count = NodeVariable.newInstance("count", Number.class, 10,true,false);
    private final NodeVariable<Turtle> output = NodeVariable.newInstance("output", Turtle.class, new Turtle(),false,true);

    public PatternOnPath() {
        super("PatternOnPath");
        addVariable(pattern);
        addVariable(path);
        addVariable(count);
        addVariable(output);
        pattern.setIsDirty(true);
    }

    public PatternOnPath(Turtle pattern, Turtle path, int count) {
        this();
        this.pattern.setValue(pattern);
        this.path.setValue(path);
        this.count.setValue(count);
    }

    @Override
    public Node create() {
        return new PatternOnPath();
    }

    @Override
    public void update() {
        try {
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
                for(double n=0;n<=pDistance;n+=step) {
                    Point2D i = myPath.interpolate(n);
                    Turtle stamp = new Turtle(myPattern);
                    stamp.translate(i.x,i.y);
                    sum.add(stamp);
                }
            }
            output.setValue(sum);
            cleanAllInputs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
