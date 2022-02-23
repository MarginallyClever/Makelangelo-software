package com.marginallyclever.nodeBasedEditor.builtInNodes;

import com.marginallyclever.nodeBasedEditor.SupergraphInput;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class LoadString extends Node implements SupergraphInput {
    private final NodeVariable<String> v = NodeVariable.newInstance("value",String.class,"",false,true);

    public LoadString() {
        super("LoadString");
        addVariable(v);
    }

    public LoadString(String startingValue) {
        this();
        v.setValue(startingValue);
    }

    @Override
    public Node create() {
        return new LoadString();
    }

    @Override
    public void update() {}
}
