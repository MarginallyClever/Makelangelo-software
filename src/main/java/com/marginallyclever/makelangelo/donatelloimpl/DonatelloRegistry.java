package com.marginallyclever.makelangelo.donatelloimpl;

import com.marginallyclever.makelangelo.donatelloimpl.nodes.TurtleDAO4JSON;
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
        NodeFactory.registerAllNodesInPackage("com.marginallyclever.makelangelo.donatelloimpl");
    }
}
