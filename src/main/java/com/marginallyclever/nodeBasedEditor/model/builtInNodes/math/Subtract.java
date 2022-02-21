package com.marginallyclever.nodeBasedEditor.model.builtInNodes.math;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class Subtract extends Node {
    private final NodeVariable<Number> a = NodeVariable.newInstance("A",Number.class,0,true,false);
    private final NodeVariable<Number> b = NodeVariable.newInstance("B",Number.class,0,true,false);
    private final NodeVariable<Number> c = NodeVariable.newInstance("output",Number.class,0,false,true);

    public Subtract() {
        super("Subtract");
        addVariable(a);
        addVariable(b);
        addVariable(c);
    }

    @Override
    public Node create() {
        return new Subtract();
    }

    @Override
    public void update() {
        if(!isDirty()) return;
        double av = a.getValue().doubleValue();
        double bv = b.getValue().doubleValue();
        c.setValue(av - bv);
        cleanAllInputs();
    }
}
