package com.marginallyclever.makelangelo.nodeBasedEditor;

import com.marginallyclever.makelangelo.nodeBasedEditor.basicNodes.Add;
import com.marginallyclever.makelangelo.nodeBasedEditor.basicNodes.Constant;
import com.marginallyclever.makelangelo.nodeBasedEditor.basicNodes.ReportToStdOut;
import org.junit.jupiter.api.Test;

public class TestNodeBasedEditorModel {
    @Test
    public void testAddAndReport() throws Exception {
        NodeBasedEditorModel model = new NodeBasedEditorModel();
        Node constant0 = model.addNode(new Constant(1));
        Node constant1 = model.addNode(new Constant(2));
        Node add = model.addNode(new Add());
        Node report = model.addNode(new ReportToStdOut());
        NodeConnection c0 = model.addNodeConnection(new NodeConnection());
        c0.setInput(constant0,0);
        c0.setOutput(add,0);
        NodeConnection c1 = model.addNodeConnection(new NodeConnection());
        c1.setInput(constant1,0);
        c1.setOutput(add,1);
        NodeConnection c2 = model.addNodeConnection(new NodeConnection());
        c2.setInput(add,2);
        c2.setOutput(report,0);

        for(int i=0;i<3;++i) {
            model.update();
        }
        System.out.println("Done");
        System.out.println(model);
    }
}
