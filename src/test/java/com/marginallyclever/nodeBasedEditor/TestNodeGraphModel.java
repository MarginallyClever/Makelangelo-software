package com.marginallyclever.nodeBasedEditor;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.nodeBasedEditor.model.NodeGraphModel;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.Add;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.Constant;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.Multiply;
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
        Node add = model.add(new Add());
        add.getVariable(0).setValue(1);
        add.getVariable(1).setValue(2);
        add.update();
        assertEquals( 3.0, add.getVariable(2).getValue() );
    }

    @Test
    public void testAddTwoConstants() {
        Node constant0 = model.add(new Constant(1));
        Node constant1 = model.add(new Constant(2));
        Node add = model.add(new Add());
        model.add(new NodeConnection(constant0,0,add,0));
        model.add(new NodeConnection(constant1,0,add,1));
        model.update();
        assertEquals( 3.0, add.getVariable(2).getValue() );
    }

    @Test
    public void testAddTwoConstantsAndReport() throws Exception {
        Node constant0 = model.add(new Constant(1));
        Node constant1 = model.add(new Constant(2));
        Node add = model.add(new Add());
        Node report = model.add(new ReportToStdOut());
        model.add(new NodeConnection(constant0,0,add,0));
        model.add(new NodeConnection(constant1,0,add,1));
        model.add(new NodeConnection(add,2,report,0));

        model.update();
        model.update();

        assertEquals( 3.0, report.getVariable(0).getValue() );
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
        testAddTwoConstants();

        JSONObject a = model.toJSON();
        NodeGraphModel modelB = new NodeGraphModel();
        modelB.parseJSON(a);
        assertEquals(model.toString(),modelB.toString());
    }

    @Test
    public void testModelClears() {
        testAddTwoConstants();
        model.clear();
        assertEquals(0,model.getNodes().size());
        assertEquals(0,model.getConnections().size());
    }

    @Test
    public void testAddTwoModelsTogether() {
        testAddTwoConstants();

        NodeGraphModel modelB = new NodeGraphModel();
        modelB.add(model);
        modelB.add(model.deepCopy());

        assertEquals(2,modelB.countNodesOfClass(Add.class));
        assertEquals(4,modelB.countNodesOfClass(Constant.class));

        // connect the Adds with a Multiply, update, and check the results.
        Node m = modelB.add(new Multiply());
        int a0index = modelB.indexOfNode(Add.class);
        int a1index = modelB.indexOfNode(Add.class,a0index+1);
        assertNotEquals(a0index,a1index);
        Node a0 = modelB.getNodes().get(a0index);
        Node a1 = modelB.getNodes().get(a1index);
        assertNotEquals(a0,a1);
        modelB.add(new NodeConnection(a0,2,m,0));
        modelB.add(new NodeConnection(a1,2,m,1));
        modelB.update();
        assertEquals(9.0,m.getVariable(2).getValue());
    }
}
