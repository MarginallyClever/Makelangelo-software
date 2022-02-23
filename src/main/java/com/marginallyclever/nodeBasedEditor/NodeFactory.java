package com.marginallyclever.nodeBasedEditor;

import com.marginallyclever.nodeBasedEditor.model.builtInNodes.math.*;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.LoadNumber;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.LoadString;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.PrintToStdOut;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.math.Random;

import java.util.*;

/**
 * Maintains a map of {@link Node}s and their names.  Can create nodes on request, by name.
 * Can deliver a list of names.
 */
public class NodeFactory {
    private static Map<String,Node> nodeRegistry = new HashMap<>();

    /**
     * Does not allow nodes to be registered more than once.
     * @param n one instance of the node.
     */
    public static void registerNode(Node n) {
        if(!nodeRegistry.containsKey(n.getName())) {
            nodeRegistry.put(n.getName(),n);
        }
    }

    /**
     * Call this once to register all the built-in nodes that are included in the package.
     */
    public static void registerBuiltInNodes() {
        NodeFactory.registerNode(new LoadNumber());
        NodeFactory.registerNode(new Random());
        NodeFactory.registerNode(new Add());
        NodeFactory.registerNode(new Subtract());
        NodeFactory.registerNode(new Multiply());
        NodeFactory.registerNode(new Divide());
        NodeFactory.registerNode(new PrintToStdOut());
        NodeFactory.registerNode(new Cos());
        NodeFactory.registerNode(new Sin());
        NodeFactory.registerNode(new Tan());
        NodeFactory.registerNode(new ATan2());
        NodeFactory.registerNode(new Min());
        NodeFactory.registerNode(new Max());

        NodeFactory.registerNode(new LoadString());
    }
    /**
     *
     * @param name The {@link Node} you want.
     * @return The {@link Node}
     * @throws IllegalArgumentException if the matchine {@link Node} cannot be found.
     */
    public static Node createNode(String name) throws IllegalArgumentException {
        if(nodeRegistry.containsKey(name)) {
            return nodeRegistry.get(name).create();
        }
        throw new IllegalArgumentException("Node type not found: "+name);
    }

    /**
     *
     * @return an array containing the unique names of every {@link Node} registered.
     */
    public static String [] getNames() {
        Set<String> list = nodeRegistry.keySet();
        return list.stream().sorted().toArray(String[]::new);
    }
}
