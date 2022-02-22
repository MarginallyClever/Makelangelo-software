package com.marginallyclever.nodeBasedEditor.model.builtInNodes.turtle;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

import java.awt.*;

public class TurtlePatternOnPath extends Node {
    private final NodeVariable<Turtle> pattern = NodeVariable.newInstance("pattern", Turtle.class, new Turtle(),true,false);
    private final NodeVariable<Turtle> path = NodeVariable.newInstance("path", Turtle.class, new Turtle(),true,false);
    private final NodeVariable<Number> count = NodeVariable.newInstance("count", Number.class, 10,true,false);
    private final NodeVariable<Turtle> output = NodeVariable.newInstance("output", Turtle.class, new Turtle(),false,true);

    public TurtlePatternOnPath() {
        super("PatternOnPath");
        addVariable(pattern);
        addVariable(path);
        addVariable(count);
        addVariable(output);
        pattern.setIsDirty(true);
    }

    public TurtlePatternOnPath(Turtle pattern,Turtle path,int count) {
        this();
        this.pattern.setValue(pattern);
        this.path.setValue(path);
        this.count.setValue(count);
    }

    @Override
    public Node create() {
        return new TurtlePatternOnPath();
    }

    @Override
    public void update() {
        if(!isDirty()) return;
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
