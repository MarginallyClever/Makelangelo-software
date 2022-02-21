package com.marginallyclever.nodeBasedEditor.model.builtInNodes;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

public class PrintToStdOut extends Node {
    private final NodeVariable<Object> a = NodeVariable.newInstance("A",Object.class,null,true,false);

    public PrintToStdOut() {
        super("Report to StdOut");
        addVariable(a);
    }

    @Override
    public Node create() {
        return new PrintToStdOut();
    }

    @Override
    public void update() {
        if(!isDirty()) return;
        Object var = a.getValue();
        String output = (var!=null) ? var.toString() : "null";
        System.out.println(getUniqueID()+": "+output);
        cleanAllInputs();
    }
}
