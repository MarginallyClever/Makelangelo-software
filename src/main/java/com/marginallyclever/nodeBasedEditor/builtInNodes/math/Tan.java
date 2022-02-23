package com.marginallyclever.nodeBasedEditor.builtInNodes.math;

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

    public Tan(double a) {
        this();
        this.a.setValue(a);
    }

    @Override
    public Node create() {
        return new Tan();
    }

    @Override
    public void update() {
        double av = a.getValue().doubleValue();
        b.setValue(Math.tan(av));
        cleanAllInputs();
    }
}
