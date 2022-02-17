package com.marginallyclever.makelangelo.nodeBasedEditor;

import com.marginallyclever.makelangelo.nodeBasedEditor.model.Node;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeGraphModel;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes.Add;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes.Constant;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes.ReportToStdOut;
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
        NodeConnection c0 = model.createNodeConnection();   c0.setInput(constant0,0);   c0.setOutput(add,0);
        NodeConnection c1 = model.createNodeConnection();   c1.setInput(constant1,0);   c1.setOutput(add,1);
        NodeConnection c2 = model.createNodeConnection();   c2.setInput(add,2);         c2.setOutput(report,0);

        for(int i=0;i<3;++i) {
            model.update();
        }

        System.out.println("Done");
        System.out.println(model);
    }
}
