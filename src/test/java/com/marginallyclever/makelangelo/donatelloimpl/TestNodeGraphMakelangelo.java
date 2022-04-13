package com.marginallyclever.makelangelo.donatelloimpl;

import com.marginallyclever.makelangelo.donatelloimpl.nodes.TurtleDAO4JSON;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DAO4JSONFactory;
import com.marginallyclever.nodegraphcore.NodeFactory;
import com.marginallyclever.nodegraphcore.NodeGraph;
import com.marginallyclever.nodegraphcore.NodeVariable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test custom {@link com.marginallyclever.nodegraphcore.Node}s for Makelangelo.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class TestNodeGraphMakelangelo {
    private static final Logger logger = LoggerFactory.getLogger(TestNodeGraphMakelangelo.class);

    private static NodeGraph model = new NodeGraph();

    @BeforeAll
    public static void beforeAll() throws Exception {
        NodeFactory.loadRegistries();
        DAO4JSONFactory.loadRegistries();

        assertNotEquals(0,NodeFactory.getNames().length);
        logger.info("NodeFactory.getNames().length="+NodeFactory.getNames().length);
        for(String s : NodeFactory.getNames()) {
            logger.info("  found="+s);
        }
    }

    public static void afterAll() {
        NodeFactory.clear();
        DAO4JSONFactory.clear();
    }

    @BeforeEach
    public void beforeEach() {
        model.clear();
    }

    private <T> void testNodeVariableToJSONAndBack(Class<T> myClass,T instA,T instB) throws Exception {
        NodeVariable<?> a = NodeVariable.newInstance(myClass.getSimpleName(),myClass,instA,false,false);
        NodeVariable<?> b = NodeVariable.newInstance(myClass.getSimpleName(),myClass,instB,false,false);

        b.parseJSON(a.toJSON());
        assertEquals(a.toString(),b.toString());
        assertEquals(a.getValue(),b.getValue());
    }

    @Test
    public void testNodeVariablesToJSONAndBack() throws Exception {
        Turtle t = new Turtle();
        //t.jumpTo(10,20);
        //t.moveTo(30,40);
        testNodeVariableToJSONAndBack(Turtle.class, t,new Turtle());
    }

    @Test
    public void testFactoryCreatesAllSwingTypes() {
        assertNotEquals(0,NodeFactory.getNames().length);
        for(String s : NodeFactory.getNames()) {
            assertNotNull(NodeFactory.createNode(s));
        }
    }

    /**
     * Test {@link Turtle}.
     */
    @Test
    public void testTurtleDAO() {
        TurtleDAO4JSON dao = new TurtleDAO4JSON();
        Turtle r1 = new Turtle();
        Turtle r2=dao.fromJSON(dao.toJSON(r1));
        assertEquals(r1,r2);
    }
}
