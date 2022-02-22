package com.marginallyclever.nodeBasedEditor.model.builtInNodes.turtle;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

import java.awt.*;

public class TurtlePatternOnPath extends Node {
    private final NodeVariable<Turtle> pattern = NodeVariable.newInstance("pattern", Turtle.class, new Turtle(),true,true);
    private final NodeVariable<Turtle> path = NodeVariable.newInstance("path", Turtle.class, new Turtle(),false,true);
    private final NodeVariable<Number> count = NodeVariable.newInstance("count", Number.class, 10,true,true);
    private final NodeVariable<Turtle> output = NodeVariable.newInstance("output", Turtle.class, new Turtle(),false,true);

    public TurtlePatternOnPath() {
        super("PatternOnPath");
        addVariable(pattern);
        addVariable(path);
        addVariable(count);
        addVariable(output);
        pattern.setIsDirty(true);
    }

    @Override
    public Node create() {
        return new TurtlePatternOnPath();
    }

    @Override
    public void update() {
        if(!isDirty()) return;
        try {
            Turtle t = new Turtle();
            Turtle p = pattern.getValue();
            int c = count.getValue().intValue();
            if(c>0) {
                double pDistance = p.getDrawDistance();
                double step = pDistance/(double)c;
                for(double n=0;n<pDistance;n+=step) {
                    Point2D i = p.interpolate(n);
                    Turtle stamp = new Turtle(p);
                    stamp.translate(i.x,i.y);
                    t.add(stamp);
                }
            }
            output.setValue(t);
            cleanAllInputs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
