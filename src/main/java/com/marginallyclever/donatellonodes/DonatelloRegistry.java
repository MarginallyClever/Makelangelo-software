package com.marginallyclever.donatellonodes;

import com.marginallyclever.donatellonodes.nodes.*;
import com.marginallyclever.donatellonodes.nodes.shapes.TurtleCircle;
import com.marginallyclever.donatellonodes.nodes.shapes.TurtleLine;
import com.marginallyclever.donatellonodes.nodes.shapes.TurtleRectangle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DAO4JSONFactory;
import com.marginallyclever.nodegraphcore.DAORegistry;
import com.marginallyclever.nodegraphcore.NodeFactory;
import com.marginallyclever.nodegraphcore.NodeRegistry;

/**
 * Register custom nodes for {@link Turtle}s.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class DonatelloRegistry implements DAORegistry, NodeRegistry {
    @Override
    public void registerDAO() {
        DAO4JSONFactory.registerDAO(Turtle.class,new TurtleDAO4JSON());
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
