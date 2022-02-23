package com.marginallyclever.nodeBasedEditor.builtInNodes.math;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class Random extends Node {
    private final NodeVariable<Number> vMax = NodeVariable.newInstance("max",Number.class,0,true,false);
    private final NodeVariable<Number> vMin = NodeVariable.newInstance("min",Number.class,0,true,false);
    private final NodeVariable<Number> v = NodeVariable.newInstance("value",Number.class,0,false,true);

    public Random(double top,double bottom) {
        super("Random");
        addVariable(vMax);
        addVariable(vMin);
        addVariable(v);
        vMax.setValue(top);
        vMin.setValue(bottom);
    }

    public Random() {
        this(20,0);
    }

    @Override
    public Node create() {
        return new Random();
    }

    @Override
    public void update() {
        double a = vMin.getValue().doubleValue();
        double b = vMax.getValue().doubleValue();
        v.setValue(Math.random()*(b-a) + a);
        cleanAllInputs();
    }
}
