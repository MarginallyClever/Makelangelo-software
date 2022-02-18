package com.marginallyclever.nodeBasedEditor;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.nodeBasedEditor.model.NodeGraphModel;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.Add;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.Constant;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.ReportToStdOut;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestNodeGraphModel {
    @Test
    public void testAdd() {
        Node add = new Add();
        add.getVariable(0).setValue(Integer.valueOf(5));
        add.getVariable(1).setValue(Integer.valueOf(10));
        add.update();
        assertEquals( 15.0, add.getVariable(2).getValue() );
    }

    @Test
    public void testAddAndReport() throws Exception {
        NodeGraphModel model = new NodeGraphModel();
        Node constant0 = model.addNode(new Constant(1));
        Node constant1 = model.addNode(new Constant(2));
        Node add = model.addNode(new Add());
        Node report = model.addNode(new ReportToStdOut());
        NodeConnection c0 = model.addConnection(new NodeConnection(constant0,0,add,0));
        NodeConnection c1 = model.addConnection(new NodeConnection(constant1,0,add,1));
        NodeConnection c2 = model.addConnection(new NodeConnection(add,2,report,0));

        for(int i=0;i<3;++i) {
            model.update();
        }

        System.out.println("Done");
        System.out.println(model);
    }
}
