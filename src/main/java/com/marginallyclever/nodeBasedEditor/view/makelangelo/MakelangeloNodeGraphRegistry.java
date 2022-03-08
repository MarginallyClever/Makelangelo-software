package com.marginallyclever.nodeBasedEditor.view.makelangelo;

import com.marginallyClever.nodeGraphCore.JSON_DAO_Factory;
import com.marginallyClever.nodeGraphCore.NodeFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodeBasedEditor.view.makelangelo.nodes.*;
import com.marginallyclever.nodeBasedEditor.view.makelangelo.nodes.shapes.TurtleCircle;
import com.marginallyclever.nodeBasedEditor.view.makelangelo.nodes.shapes.TurtleLine;
import com.marginallyclever.nodeBasedEditor.view.makelangelo.nodes.shapes.TurtleRectangle;

/**
 * Create custom {@link com.marginallyClever.nodeGraphCore.Node}s for Makleangelo.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class MakelangeloNodeGraphRegistry {
    public static void register() {
        NodeFactory.registerNode(new LoadTurtle());
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
