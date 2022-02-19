package com.marginallyclever.nodeBasedEditor;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.nodeBasedEditor.model.NodeGraphModel;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.Add;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.Constant;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.ReportToStdOut;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestNodeGraphModel {
    private static NodeGraphModel model;

    @BeforeAll
    static void beforeAll() {
        model = new NodeGraphModel();
    }

    @BeforeEach
    public void beforeEach() {
        model.clear();
    }

    @Test
    public void testAdd() {
        Node add = new Add();
        add.getVariable(0).setValue(5);
        add.getVariable(1).setValue(10);
        add.update();
        assertEquals( 15.0, add.getVariable(2).getValue() );
    }

    @Test
    public void testAddAndReport() throws Exception {
        Node constant0 = model.addNode(new Constant(1));
        Node constant1 = model.addNode(new Constant(2));
        Node add = model.addNode(new Add());
        Node report = model.addNode(new ReportToStdOut());
        model.addConnection(new NodeConnection(constant0,0,add,0));
        model.addConnection(new NodeConnection(constant1,0,add,1));
        model.addConnection(new NodeConnection(add,2,report,0));

        for(int i=0;i<3;++i) {
            model.update();
        }

        System.out.println("Done");
        System.out.println(model);
    }

    @Test
    public void testFactoryFailsOnBadRequests() {
        assertThrows(IllegalArgumentException.class, ()->{
            NodeFactory.createNode("");
        });

        assertThrows(IllegalArgumentException.class, ()->{
            NodeFactory.createNode(null);
        });
    }

    @Test
    public void testFactoryCreatesAllDefaultTypes() {
        int count=0;
        for(String s : NodeFactory.getNames()) {
            assertNotNull(NodeFactory.createNode(s));
            ++count;
        }
        assertNotEquals(0,count);
    }

    @Test
    public void testOneNodeToJSONAndBack() {
        Node nodeA = new Add();
        JSONObject a = nodeA.toJSON();
        Node nodeB = new Add();
        nodeB.parseJSON(a);
        assertEquals(nodeA.toString(), nodeB.toString());
    }

    @Test
    public void testModelToJSONAndBack() {
        Node constant0 = model.addNode(new Constant(1));
        Node constant1 = model.addNode(new Constant(2));
        Node add = model.addNode(new Add());
        Node report = model.addNode(new ReportToStdOut());
        model.addConnection(new NodeConnection(constant0,0,add,0));
        model.addConnection(new NodeConnection(constant1,0,add,1));
        model.addConnection(new NodeConnection(add,2,report,0));

        for(int i=0;i<3;++i) {
            model.update();
        }

        JSONObject a = model.toJSON();
        NodeGraphModel modelB = new NodeGraphModel();
        modelB.parseJSON(a);
        assertEquals(model.toString(),modelB.toString());
    }
}
