package com.marginallyclever.nodeBasedEditor;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodeBasedEditor.model.NodeGraph;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestNodeGraphMakeangelo {
    private static NodeGraph model;

    @BeforeAll
    static void beforeAll() {
        model = new NodeGraph();
        NodeFactory.registerBuiltInNodes();
    }

    @BeforeEach
    public void beforeEach() {
        model.clear();
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
        Turtle t = new Turtle();
        t.jumpTo(10,20);
        t.moveTo(30,40);
        testNodeVariableToJSONAndBack(Turtle.class, t,new Turtle());
    }
}
