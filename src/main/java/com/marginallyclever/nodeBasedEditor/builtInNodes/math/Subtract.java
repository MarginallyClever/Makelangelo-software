package com.marginallyclever.nodeBasedEditor.builtInNodes.math;

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

    public Subtract(double a,double b) {
        this();
        this.a.setValue(a);
        this.b.setValue(b);
    }

    @Override
    public Node create() {
        return new Subtract();
    }

    @Override
    public void update() {
        double av = a.getValue().doubleValue();
        double bv = b.getValue().doubleValue();
        c.setValue(av - bv);
        cleanAllInputs();
    }
}
