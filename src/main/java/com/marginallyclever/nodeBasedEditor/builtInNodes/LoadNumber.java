package com.marginallyclever.nodeBasedEditor.builtInNodes;

import com.marginallyclever.nodeBasedEditor.SupergraphInput;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class LoadNumber extends Node implements SupergraphInput {
    private final NodeVariable<Number> v = NodeVariable.newInstance("value",Number.class,0,false,true);

    public LoadNumber() {
        super("LoadNumber");
        addVariable(v);
    }

    public LoadNumber(Number startingValue) {
        this();
        v.setValue(startingValue);
    }

    @Override
    public Node create() {
        return new LoadNumber();
    }

    @Override
    public void update() {}
}
