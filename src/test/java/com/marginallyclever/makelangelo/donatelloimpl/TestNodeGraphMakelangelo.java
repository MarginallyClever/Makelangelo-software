package com.marginallyclever.makelangelo.donatelloimpl;

import com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.LoadTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.SaveTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.TurtleDAO4JSON;
import com.marginallyclever.makelangelo.turtle.ListOfLines;
import com.marginallyclever.makelangelo.turtle.ListOfPoints;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Connection;
import com.marginallyclever.nodegraphcore.DAO4JSONFactory;
import com.marginallyclever.nodegraphcore.Graph;
import com.marginallyclever.nodegraphcore.NodeFactory;
import com.marginallyclever.nodegraphcore.port.Input;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test custom {@link com.marginallyclever.nodegraphcore.Node}s for Makelangelo.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class TestNodeGraphMakelangelo {
    private static final Logger logger = LoggerFactory.getLogger(TestNodeGraphMakelangelo.class);

    private static final Graph model = new Graph();

    @BeforeAll
    public static void beforeAll() throws Exception {
        NodeFactory.loadRegistries();
        DAO4JSONFactory.loadRegistries();

        assertNotEquals(0,NodeFactory.getNames().length);
        logger.info("NodeFactory.getNames()="+ Arrays.toString(NodeFactory.getNames()));
    }

    public static void afterAll() {
        NodeFactory.clear();
        DAO4JSONFactory.clear();
    }

    @BeforeEach
    public void beforeEach() {
        model.clear();
    }

    // TODO figure out why this test fails in Maven.
    @Disabled("This works in IntelliJ, not in Maven.  Why?")
    @Test
    public void testDAOsRegistered() {
        Assertions.assertTrue(DAO4JSONFactory.isRegistered(Turtle.class),"Turtle not found in DAO4JSONFactory");
        Assertions.assertTrue(DAO4JSONFactory.isRegistered(ListOfPoints.class),"ListOfPoints not found in DAO4JSONFactory");
        Assertions.assertTrue(DAO4JSONFactory.isRegistered(ListOfLines.class),"ListOfLines not found in DAO4JSONFactory");
    }

    private <T> void testPortToJSONAndBack(Class<T> myClass, T instA, T instB) throws Exception {
        Input<?> a = new Input<>(myClass.getSimpleName(),myClass,instA);
        Input<?> b = new Input<>(myClass.getSimpleName(),myClass,instB);

        b.fromJSON(a.toJSON());
        Assertions.assertEquals(a.toString(),b.toString());
        Assertions.assertEquals(a.getValue(),b.getValue());
    }

    // TODO figure out why this test fails in Maven.
    @Disabled("This works in IntelliJ, not in Maven.  Why?")
    @Test
    public void testPortToJSONAndBack() throws Exception {
        Assertions.assertNotEquals(0,DAO4JSONFactory.getNames().length);
        logger.info("ports: "+Arrays.toString(DAO4JSONFactory.getNames()));
        Turtle t = new Turtle();
        //t.jumpTo(10,20);
        //t.moveTo(30,40);
        testPortToJSONAndBack(Turtle.class, t,new Turtle());
    }

    @Test
    public void testFactoryCreatesAllSwingTypes() {
        Assertions.assertNotEquals(0,NodeFactory.getNames().length);
        for(String s : NodeFactory.getNames()) {
            Assertions.assertNotNull(NodeFactory.createNode(s));
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
        Assertions.assertEquals(r1,r2);
    }

    /**
     * Create a donatello panel;
     * connect a LoadTurtle node and a SaveTurtle node;
     * load a test file;
     * save the test file;
     * confirm the test file was saved.
     */
    @Test
    public void testLoadAndSaveTurtle() throws IOException {
        Graph graph = new Graph();

        // Create and connect LoadTurtle and SaveTurtle nodes
        LoadTurtle loadNode = new LoadTurtle();
        SaveTurtle saveNode = new SaveTurtle();
        graph.add(loadNode);
        graph.add(saveNode);
        graph.add(new Connection(loadNode,1, saveNode,1));

        // get a filename for a non-existent temporary file
        File tempFile = File.createTempFile("testTurtle", ".dxf");
        tempFile.deleteOnExit();
        String tempFilename = tempFile.getAbsolutePath();

        // Load a test file
        loadNode.getPort("filename").setValue("src/test/resources/dxf/circle.dxf");
        saveNode.getPort("filename").setValue(tempFilename);
        loadNode.update();
        saveNode.update();

        // confirm the file saved.
        Assertions.assertTrue(tempFile.exists());
        Assertions.assertTrue(tempFile.length()>0);
    }
}