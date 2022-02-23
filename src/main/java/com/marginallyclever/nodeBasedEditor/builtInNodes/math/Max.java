package com.marginallyclever.nodeBasedEditor.builtInNodes.math;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class Max extends Node {
    private final NodeVariable<Number> a = NodeVariable.newInstance("A",Number.class,0,true,false);
    private final NodeVariable<Number> b = NodeVariable.newInstance("B",Number.class,0,true,false);
    private final NodeVariable<Number> c = NodeVariable.newInstance("output",Number.class,0,false,true);

    public Max() {
        super("Max");
        addVariable(a);
        addVariable(b);
        addVariable(c);
    }

    public Max(double a,double b) {
        this();
        this.a.setValue(a);
        this.b.setValue(b);
    }

    @Override
    public Node create() {
        return new Max();
    }

    @Override
    public void update() {
        double av = a.getValue().doubleValue();
        double bv = b.getValue().doubleValue();
        c.setValue(Math.max(av,bv));
        cleanAllInputs();
    }
}
