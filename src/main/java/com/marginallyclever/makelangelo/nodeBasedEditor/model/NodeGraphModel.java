package com.marginallyclever.makelangelo.nodeBasedEditor.model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link NodeGraphModel} contains the {@link Node}s, and {@link NodeConnection}s
 */
public class NodeGraphModel {
    private List<Node> nodes = new ArrayList<>();
    private List<NodeConnection> connections = new ArrayList<>();

    public NodeGraphModel() {
        super();
    }

    public void update() throws Exception {
        for(Node n : nodes) n.update();
        for(NodeConnection c : connections) c.apply();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<NodeConnection> getConnections() {
        return connections;
    }

    public Node addNode(Node n) {
        nodes.add(n);
        return n;
    }

    public Node createNode(String classType) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Node n = (Node)Class.forName(classType).getDeclaredConstructor().newInstance();
        return addNode(n);
    }

    public NodeConnection createNodeConnection() {
        NodeConnection c = new NodeConnection();
        connections.add(c);
        return c;
    }

    @Override
    public String toString() {
        return "NodeBasedEditorModel{" +
                "nodes=" + nodes +
                ", connections=" + connections +
                '}';
    }
}
