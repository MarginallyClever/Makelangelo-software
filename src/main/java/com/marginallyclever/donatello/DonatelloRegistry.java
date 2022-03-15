package com.marginallyClever.donatello;

import com.marginallyClever.donatello.nodes.*;
import com.marginallyClever.donatello.nodes.shapes.TurtleCircle;
import com.marginallyClever.donatello.nodes.shapes.TurtleLine;
import com.marginallyClever.donatello.nodes.shapes.TurtleRectangle;
import com.marginallyClever.makelangelo.turtle.Turtle;
import com.marginallyClever.nodeGraphCore.DAORegistry;
import com.marginallyClever.nodeGraphCore.JSON_DAO_Factory;
import com.marginallyClever.nodeGraphCore.NodeFactory;
import com.marginallyClever.nodeGraphCore.NodeRegistry;

/**
 * Register custom nodes for {@link Turtle}s.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class DonatelloRegistry implements DAORegistry, NodeRegistry {
    @Override
    public void registerDAO() {
        JSON_DAO_Factory.registerDAO(Turtle.class,new TurtleDAO4JSON());
    }

    @Override
    public void registerNodes() {
        NodeFactory.registerNode(new LoadTurtle());
        NodeFactory.registerNode(new SaveTurtle());
        NodeFactory.registerNode(new PrintTurtle());
        NodeFactory.registerNode(new TurtleLine());
        NodeFactory.registerNode(new TurtleRectangle());
        NodeFactory.registerNode(new TurtleCircle());
        NodeFactory.registerNode(new PatternOnPath());
        NodeFactory.registerNode(new PathImageMask());
        NodeFactory.registerNode(new TransformTurtle());
        NodeFactory.registerNode(new ColorTurtle());
        NodeFactory.registerNode(new TurtleToBufferedImage());
    }
}
