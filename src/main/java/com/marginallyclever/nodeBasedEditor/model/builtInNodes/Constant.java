package com.marginallyclever.nodeBasedEditor.model.builtInNodes;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class Constant extends Node {
    private final NodeVariable<Number> v = NodeVariable.newInstance("value",Number.class,0,false,true);

    public Constant(Number startingValue) {
        super("Constant");
        addVariable(v);
        v.setValue(startingValue);
    }

    public Constant() {
        this(0);
    }

    @Override
    public void update() {}
}
