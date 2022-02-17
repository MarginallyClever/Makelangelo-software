package com.marginallyclever.makelangelo.nodeBasedEditor.basicNodes;

import com.marginallyclever.makelangelo.nodeBasedEditor.Node;
import com.marginallyclever.makelangelo.nodeBasedEditor.NodeVariable;

public class Add extends Node {
    private final NodeVariable<Number> a = new NodeVariable<>("A",0,true,false);
    private final NodeVariable<Number> b = new NodeVariable<>("B",0,true,false);
    private final NodeVariable<Number> c = new NodeVariable<>("output",0,false,true);

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
