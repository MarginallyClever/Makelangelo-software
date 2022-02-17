package com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes;

import com.marginallyclever.makelangelo.nodeBasedEditor.model.Node;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeVariable;

public class ReportToStdOut extends Node {
    private final NodeVariable<Object> a = NodeVariable.newInstance("A",Object.class,null,true,false);

    public ReportToStdOut() {
        super("Report to StdOut");
        addVariable(a);
    }

    @Override
    public void update() {
        if(!isDirty()) return;
        Object var = a.getValue();
        String output = (var!=null) ? var.toString() : "null";
        System.out.println(getUniqueID()+": "+output);
    }
}
