package com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes;

import com.marginallyclever.makelangelo.nodeBasedEditor.model.Node;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeVariable;

public class Add extends Node {
    private final NodeVariable<Number> a = NodeVariable.newInstance("A",Number.class,0,true,false);
    private final NodeVariable<Number> b = NodeVariable.newInstance("B",Number.class,0,true,false);
    private final NodeVariable<Number> c = NodeVariable.newInstance("output",Number.class,0,false,true);

    public Add() {
        super("Add");
        addVariable(a);
        addVariable(b);
        addVariable(c);
    }

    @Override
    public void update() {
        if(!isDirty()) return;
        Number an = a.getValue();
        double av = an.doubleValue();
        Number bn = b.getValue();
        double bv = bn.doubleValue();
        c.setValue(av + bv);
        alwaysBeCleaning();
    }
}
