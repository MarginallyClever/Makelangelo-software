package com.marginallyclever.makelangelo.nodeBasedEditor;

import com.marginallyclever.makelangelo.nodeBasedEditor.basicNodes.Constant;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link NodeBasedEditorModel} contains the {@link Node}s, and {@link NodeConnection}s
 */
public class NodeBasedEditorModel {
    private List<Node> nodes = new ArrayList<>();
    private List<NodeConnection> connections = new ArrayList<>();

    public NodeBasedEditorModel() {
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

    public NodeConnection addNodeConnection(NodeConnection c) {
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
