package com.marginallyclever.nodeBasedEditor.model.builtInNodes;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class SubGraph extends Node {
    private final NodeVariable<NodeGraph> a = NodeVariable.newInstance("A",Object.class,null,true,false);

    public SubGraph() {
        super("Report to StdOut");
        addVariable(a);
    }

    @Override
    public Node create() {
        return new SubGraph();
    }

    @Override
    public void update() {
        if(!isDirty()) return;
        Object var = a.getValue();
        String output = (var!=null) ? var.toString() : "null";
        System.out.println(getUniqueID()+": "+output);
        alwaysBeCleaning();
    }
}
