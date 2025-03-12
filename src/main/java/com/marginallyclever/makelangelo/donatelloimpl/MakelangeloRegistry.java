package com.marginallyclever.makelangelo.donatelloimpl;

import com.marginallyclever.nodegraphcore.DAO4JSONFactory;
import com.marginallyclever.nodegraphcore.DAORegistry;
import com.marginallyclever.nodegraphcore.NodeFactory;
import com.marginallyclever.nodegraphcore.NodeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Registers Swing {@link com.marginallyclever.nodegraphcore.Node}s to the {@link NodeFactory}.
 * Registers Swing types with the JSON DAO factory.</p>
 * <p>Do not instantiate this class or call these directly.  Instead call <code>NodeFactory.loadRegistries()</code> and <code>DAO4JSONFactory.loadRegistries()</code></p>
 * @author Dan Royer
 * @since 2022-02-01
 */
public class MakelangeloRegistry implements DAORegistry, NodeRegistry {
    private static final Logger logger = LoggerFactory.getLogger(MakelangeloRegistry.class);

    public String getName() {
        return "Makelangelo";
    }

    @Override
    public void registerNodes() {
        logger.info("Registering makelangelo-software nodes");
        NodeFactory.registerAllNodesInPackage("com.marginallyclever.makelangelo.donatelloimpl.nodes");
    }

    @Override
    public void registerDAO() {
        logger.info("Registering makelangelo-software DAOs");
        DAO4JSONFactory.registerAllDAOInPackage("com.marginallyclever.makelangelo.donatelloimpl.nodes");
    }
}
