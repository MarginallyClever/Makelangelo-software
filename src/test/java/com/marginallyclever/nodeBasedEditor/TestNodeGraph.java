package com.marginallyclever.nodeBasedEditor;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.nodeBasedEditor.model.NodeGraph;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.math.Add;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.LoadNumber;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.math.Multiply;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.PrintToStdOut;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.math.Subtract;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class TestNodeGraph {
    private static NodeGraph model;

    @BeforeAll
    static void beforeAll() {
        model = new NodeGraph();
    }

    @BeforeEach
    public void beforeEach() {
        model.clear();
    }

    @Test
    public void testSaveEmptyGraph() {
        assertEquals("{\"nodes\":[],\"nodeConnections\":[]}",model.toJSON());
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
        Node constant0 = model.add(new LoadNumber(1));
        Node constant1 = model.add(new LoadNumber(2));
        Node add = model.add(new Add());
        model.add(new NodeConnection(constant0,0,add,0));
        model.add(new NodeConnection(constant1,0,add,1));
        model.update();
        assertEquals( 3.0, add.getVariable(2).getValue() );
    }

    @Test
    public void testAddTwoConstantsAndReport() throws Exception {
        Node constant0 = model.add(new LoadNumber(1));
        Node constant1 = model.add(new LoadNumber(2));
        Node add = model.add(new Add());
        Node report = model.add(new PrintToStdOut());
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
        assertNotEquals(0,NodeFactory.getNames().length);
        for(String s : NodeFactory.getNames()) {
            assertNotNull(NodeFactory.createNode(s));
        }
    }

    @Test
    public void testNodesAreNotEqual() {
        Node nodeA = new Add();
        Node nodeB = new Subtract();
        assertThrows(JSONException.class,()->nodeB.parseJSON(nodeA.toJSON()));
        assertNotEquals(nodeA.toString(), nodeB.toString());
    }

    @Test
    public void testAllNodesToJSONAndBack() {
        for(String s : NodeFactory.getNames()) {
            Node a = NodeFactory.createNode(s);
            Node b = NodeFactory.createNode(s);
            b.parseJSON(a.toJSON());
            assertEquals(a.toString(),b.toString());
        }
    }

    @Test
    public void testModelToJSONAndBack() {
        testAddTwoConstants();

        JSONObject a = model.toJSON();
        NodeGraph modelB = new NodeGraph();
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

    private <T> void testNodeVariableToJSONAndBack(Class<T> myClass,T instA,T instB) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        NodeVariable<?> a = NodeVariable.newInstance(myClass.getSimpleName(),myClass,instA,false,false);
        NodeVariable<?> b = NodeVariable.newInstance(myClass.getSimpleName(),myClass,instB,false,false);

        JSONObject obj = a.toJSON();
        b.parseJSON(obj);
        assertEquals(a.toString(),b.toString());
        assertEquals(a.getValue(),b.getValue());
    }

    @Test
    public void testNodeVariablesToJSONAndBack() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        testNodeVariableToJSONAndBack(Object.class, new Object(),new Object());
        testNodeVariableToJSONAndBack(String.class, "hello",new String());
        testNodeVariableToJSONAndBack(Number.class, 1.0,0.0);
        testNodeVariableToJSONAndBack(Number.class, 1,0);
        Turtle t = new Turtle();
        t.jumpTo(10,20);
        t.moveTo(30,40);
        testNodeVariableToJSONAndBack(Turtle.class, t,new Turtle());
    }

        @Test
    public void testAddTwoModelsTogether() {
        testAddTwoConstants();

        NodeGraph modelB = new NodeGraph();
        modelB.add(model);
        modelB.add(model.deepCopy());

        assertEquals(2,modelB.countNodesOfClass(Add.class));
        assertEquals(4,modelB.countNodesOfClass(LoadNumber.class));

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
