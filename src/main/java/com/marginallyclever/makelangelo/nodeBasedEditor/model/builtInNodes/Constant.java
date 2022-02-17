package com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes;

import com.marginallyclever.makelangelo.nodeBasedEditor.model.Node;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeVariable;

public class Constant extends Node {
    private final NodeVariable<Number> v = new NodeVariable<>("value",0,false,true);

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
