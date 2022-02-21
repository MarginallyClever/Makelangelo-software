package com.marginallyclever.nodeBasedEditor.model.builtInNodes;

import com.marginallyclever.nodeBasedEditor.SupergraphInput;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class LoadNumber extends Node implements SupergraphInput {
    private final NodeVariable<Number> v = NodeVariable.newInstance("value",Number.class,0,false,true);

    public LoadNumber(Number startingValue) {
        super("LoadNumber");
        addVariable(v);
        v.setValue(startingValue);
    }

    public LoadNumber() {
        this(0);
    }

    @Override
    public Node create() {
        return new LoadNumber();
    }

    @Override
    public void update() {}
}
