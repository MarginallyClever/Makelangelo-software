package com.marginallyclever.nodeBasedEditor.model.builtInNodes.math;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class ATan2 extends Node {
    private final NodeVariable<Number> a = NodeVariable.newInstance("X",Number.class,0,true,false);
    private final NodeVariable<Number> b = NodeVariable.newInstance("Y",Number.class,0,true,false);
    private final NodeVariable<Number> c = NodeVariable.newInstance("output",Number.class,0,false,true);

    public ATan2() {
        super("ATan2");
        addVariable(a);
        addVariable(b);
        addVariable(c);
    }

    @Override
    public Node create() {
        return new ATan2();
    }

    @Override
    public void update() {
        if(!isDirty()) return;
        double x = a.getValue().doubleValue();
        double y = a.getValue().doubleValue();
        c.setValue(Math.atan2(y,x));
        cleanAllInputs();
    }
}