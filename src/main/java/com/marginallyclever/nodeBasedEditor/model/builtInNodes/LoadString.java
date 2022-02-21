package com.marginallyclever.nodeBasedEditor.model.builtInNodes;

import com.marginallyclever.nodeBasedEditor.SupergraphInput;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class LoadString extends Node implements SupergraphInput {
    private final NodeVariable<String> v = NodeVariable.newInstance("value",String.class,"",false,true);

    public LoadString(String startingValue) {
        super("LoadString");
        addVariable(v);
        v.setValue(startingValue);
    }

    public LoadString() {
        this("");
    }

    @Override
    public Node create() {
        return new LoadString();
    }

    @Override
    public void update() {}
}
