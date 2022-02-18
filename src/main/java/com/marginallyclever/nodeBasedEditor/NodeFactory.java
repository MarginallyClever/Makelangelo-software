package com.marginallyclever.nodeBasedEditor;

import com.marginallyclever.nodeBasedEditor.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
         return nodeRegistry.keySet().toArray(new String[0]);
    }
}
