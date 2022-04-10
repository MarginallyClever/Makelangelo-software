package com.marginallyclever.makelangelo.donatelloimpl;

import com.marginallyclever.makelangelo.donatelloimpl.nodes.*;
import com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes.Circle;
import com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes.Line;
import com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes.NGon;
import com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes.Rectangle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DAO4JSONFactory;
import com.marginallyclever.nodegraphcore.DAORegistry;
import com.marginallyclever.nodegraphcore.NodeFactory;
import com.marginallyclever.nodegraphcore.NodeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Register custom nodes for {@link Turtle}s.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class DonatelloRegistry implements DAORegistry, NodeRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DonatelloRegistry.class);

    public String getName() {
        return "Makelangelo";
    }

    @Override
    public void registerDAO() {
        logger.info("Registering makelangelo-software DAOs");
        DAO4JSONFactory.registerDAO(Turtle.class,new TurtleDAO4JSON());
    }

    @Override
    public void registerNodes() {
        logger.info("Registering makelangelo-software nodes");
        //NodeFactory.registerAllNodesInPackage("com.marginallyclever.makelangelo.donatelloimpl");

        NodeFactory.registerNode(Circle.class);
        NodeFactory.registerNode(Line.class);
        NodeFactory.registerNode(Rectangle.class);
        NodeFactory.registerNode(AddTurtles.class);
        NodeFactory.registerNode(ColorTurtle.class);
        NodeFactory.registerNode(LoadTurtle.class);
        NodeFactory.registerNode(PathImageMask.class);
        NodeFactory.registerNode(PatternOnPath.class);
        NodeFactory.registerNode(PointOnPath.class);
        NodeFactory.registerNode(PrintTurtle.class);
        NodeFactory.registerNode(SaveTurtle.class);
        NodeFactory.registerNode(TransformTurtle.class);
        NodeFactory.registerNode(TurtleToBufferedImage.class);
        NodeFactory.registerNode(NGon.class);
    }
}
