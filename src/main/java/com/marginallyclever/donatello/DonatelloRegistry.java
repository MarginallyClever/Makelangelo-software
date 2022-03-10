package com.marginallyclever.donatello;

import com.marginallyClever.nodeGraphCore.JSON_DAO_Factory;
import com.marginallyClever.nodeGraphCore.NodeFactory;
import com.marginallyclever.donatello.nodes.*;
import com.marginallyclever.donatello.nodes.shapes.TurtleCircle;
import com.marginallyclever.donatello.nodes.shapes.TurtleLine;
import com.marginallyclever.donatello.nodes.shapes.TurtleRectangle;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Register custom nodes for {@link Turtle}s.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class DonatelloRegistry {
    public static void register() {
        NodeFactory.registerNode(new LoadTurtle());
        NodeFactory.registerNode(new SaveTurtle());
        NodeFactory.registerNode(new PrintTurtle());
        NodeFactory.registerNode(new TurtleLine());
        NodeFactory.registerNode(new TurtleRectangle());
        NodeFactory.registerNode(new TurtleCircle());
        NodeFactory.registerNode(new PatternOnPath());
        NodeFactory.registerNode(new PathImageMask());
        NodeFactory.registerNode(new TransformTurtle());

        JSON_DAO_Factory.registerDAO(Turtle.class,new TurtleJSON_DAO());
    }
}
