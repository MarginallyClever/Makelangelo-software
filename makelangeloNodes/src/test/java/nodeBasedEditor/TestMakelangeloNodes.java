package nodeBasedEditor;

import com.marginallyClever.donatelloNodes.MakelangeloNodeRegistry;
import com.marginallyClever.donatelloNodes.nodes.TurtleJSON_DAO;
import com.marginallyClever.makelangelo.turtle.Turtle;
import com.marginallyClever.nodeGraphCore.JSON_DAO_Factory;
import com.marginallyClever.nodeGraphCore.NodeFactory;
import com.marginallyClever.nodeGraphCore.NodeGraph;
import com.marginallyClever.nodeGraphCore.NodeVariable;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test custom {@link com.marginallyClever.nodeGraphCore.Node}s for Makelangelo.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class TestMakelangeloNodes {
    private static NodeGraph model = new NodeGraph();

    @BeforeAll
    public static void beforeAll() {
        MakelangeloNodeRegistry r = new MakelangeloNodeRegistry();
        r.registerDAO();
        r.registerNodes();
    }

    @AfterAll
    public static void afterAll() {
        NodeFactory.clear();
        JSON_DAO_Factory.clear();
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
            System.out.println(s);
            assertNotNull(NodeFactory.createNode(s));
        }
    }

    /**
     * Test {@link Turtle}.
     */
    @Test
    public void testTurtleDAO() {
        TurtleJSON_DAO dao = new TurtleJSON_DAO();
        Turtle r1 = new Turtle();
        Turtle r2=dao.fromJSON(dao.toJSON(r1));
        Assertions.assertEquals(r1,r2);
    }
}
