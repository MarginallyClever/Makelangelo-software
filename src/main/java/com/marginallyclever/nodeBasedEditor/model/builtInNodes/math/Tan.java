package com.marginallyclever.nodeBasedEditor.model.builtInNodes.math;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class Tan extends Node {
    private final NodeVariable<Number> a = NodeVariable.newInstance("A",Number.class,0,true,false);
    private final NodeVariable<Number> b = NodeVariable.newInstance("output",Number.class,0,false,true);

    public Tan() {
        super("Tan");
        addVariable(a);
        addVariable(b);
    }

    @Override
    public Node create() {
        return new Tan();
    }

    @Override
    public void update() {
        if(!isDirty()) return;
        double av = a.getValue().doubleValue();
        b.setValue(Math.tan(av));
        alwaysBeCleaning();
    }
}
