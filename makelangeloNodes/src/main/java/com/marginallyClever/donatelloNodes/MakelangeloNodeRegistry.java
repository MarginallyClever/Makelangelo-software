package com.marginallyClever.donatello;

import com.marginallyClever.nodeGraphCore.DAORegistry;
import com.marginallyClever.nodeGraphCore.JSON_DAO_Factory;
import com.marginallyClever.nodeGraphCore.NodeFactory;
import com.marginallyClever.nodeGraphCore.NodeRegistry;
import com.marginallyClever.donatello.nodes.*;
import com.marginallyClever.donatello.nodes.shapes.TurtleCircle;
import com.marginallyClever.donatello.nodes.shapes.TurtleLine;
import com.marginallyClever.donatello.nodes.shapes.TurtleRectangle;
import com.marginallyClever.makelangelo.turtle.Turtle;

/**
 * Service to register custom {@link com.marginallyClever.nodeGraphCore.Node}s for {@link Turtle}s.<br/>
 * Service to register JSON Data Access Object for class {@link Turtle}.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class MakelangeloNodeRegistry implements NodeRegistry, DAORegistry {
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

    @Override
    public void registerDAO() {
        JSON_DAO_Factory.registerDAO(Turtle.class,new TurtleJSON_DAO());
    }
}
