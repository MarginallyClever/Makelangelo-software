package com.marginallyclever.makelangelo.nodeBasedEditor.basicNodes;

import com.marginallyclever.makelangelo.nodeBasedEditor.Node;
import com.marginallyclever.makelangelo.nodeBasedEditor.NodeVariable;

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
