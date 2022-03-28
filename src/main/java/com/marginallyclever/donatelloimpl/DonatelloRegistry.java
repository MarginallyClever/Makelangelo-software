package com.marginallyclever.donatelloimpl;

import com.marginallyclever.donatelloimpl.nodes.TurtleDAO4JSON;
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
    public String getName() {
        return "Makelangelo software";
    }

    @Override
    public void registerDAO() {
        DAO4JSONFactory.registerDAO(Turtle.class,new TurtleDAO4JSON());
    }

    @Override
    public void registerNodes() {
        NodeFactory.registerAllNodesInPackage("com.marginallyclever.donatelloimpl.nodes");
    }
}
